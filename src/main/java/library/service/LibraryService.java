package library.service;

import library.domain.User;
import library.domain.Loan;
import library.domain.media.Book;
import library.domain.media.CD;
import library.domain.media.Media;
import library.exception.BusinessRuleException;
import library.exception.ResourceNotFoundException;
import library.repository.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Main service class for the library.
 * Handles users, media, loans, fines, and basic reporting.
 */
public class LibraryService {

    private final UserRepository userRepo;
    private final MediaRepository mediaRepo;
    private final LoanRepository loanRepo;
    private final ReminderService reminderService;
    private final TimeProvider timeProvider;
    private final library.strategy.FineStrategy bookFine;
    private final library.strategy.FineStrategy cdFine;
    private final AuthService authService;

    /**
     * Creates a new LibraryService with all required dependencies.
     *
     * @param userRepo        repository for users
     * @param mediaRepo       repository for media (books, CDs)
     * @param loanRepo        repository for loans
     * @param reminderService reminder service used for overdue notifications
     * @param timeProvider    provider for current date
     * @param bookFine        fine strategy used for books
     * @param cdFine          fine strategy used for CDs
     * @param authService     auth service for admin checks
     */
    public LibraryService(UserRepository userRepo, MediaRepository mediaRepo, LoanRepository loanRepo,
                          ReminderService reminderService, TimeProvider timeProvider,
                          library.strategy.FineStrategy bookFine, library.strategy.FineStrategy cdFine,
                          AuthService authService) {
        this.userRepo = userRepo;
        this.mediaRepo = mediaRepo;
        this.loanRepo = loanRepo;
        this.reminderService = reminderService;
        this.timeProvider = timeProvider;
        this.bookFine = bookFine;
        this.cdFine = cdFine;
        this.authService = authService;
    }

    /**
     * Registers a new user in the system.
     *
     * @param name  user name
     * @param email user email
     * @return the created User instance
     */
    public User registerUser(String name, String email) {
        User u = new User(name, email);
        userRepo.save(u);
        return u;
    }

    /**
     * Adds a new book to the media repository.
     *
     * @param title  book title
     * @param author author name
     * @param isbn   book ISBN
     */
    public void addBook(String title, String author, String isbn) {
        Book b = new Book(title, author, isbn);
        mediaRepo.save(b);
    }

    /**
     * Adds a new CD to the media repository.
     *
     * @param title  CD title
     * @param artist CD artist or band
     */
    public void addCD(String title, String artist) {
        CD cd = new CD(title, artist);
        mediaRepo.save(cd);
    }

    /**
     * Generic search by query. Delegates to MediaRepository.search.
     *
     * @param q search text (matched against title, author, artist, ISBN, or id)
     * @return list of matching media
     */
    public List<Media> search(String q) {
        return mediaRepo.search(q == null ? "" : q);
    }

    /**
     * Searches media items by title.
     *
     * @param title partial or full title
     * @return list of media with matching titles
     */
    public List<Media> searchByTitle(String title) {
        String q = title == null ? "" : title.toLowerCase();
        return mediaRepo.findAll().stream()
                .filter(m -> m.getTitle() != null
                        && m.getTitle().toLowerCase().contains(q))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Searches media items by author (for books) or artist (for CDs).
     *
     * @param author name or partial name of author/artist
     * @return list of matching media
     */
    public List<Media> searchByAuthor(String author) {
        String q = author == null ? "" : author.toLowerCase();
        return mediaRepo.findAll().stream()
                .filter(m -> {
                    if (m instanceof Book) {
                        String a = ((Book) m).getAuthor();
                        return a != null && a.toLowerCase().contains(q);
                    } else if (m instanceof CD) {
                        String art = ((CD) m).getArtist();
                        return art != null && art.toLowerCase().contains(q);
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Searches media items by ISBN (books only, exact match).
     *
     * @param isbn ISBN string
     * @return list of books with that ISBN, or empty list
     */
    public List<Media> searchByIsbn(String isbn) {
        if (isbn == null) return java.util.Collections.emptyList();
        String target = isbn.trim().toLowerCase();
        return mediaRepo.findAll().stream()
                .filter(m -> m instanceof Book
                        && ((Book) m).getIsbn() != null
                        && ((Book) m).getIsbn().trim().toLowerCase().equals(target))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Borrows a media item for a user.
     * <ul>
     *     <li>Checks for outstanding fines</li>
     *     <li>Checks for overdue loans</li>
     *     <li>Ensures media is available</li>
     *     <li>Creates a loan and sets due date based on media type</li>
     * </ul>
     *
     * @param userId  id of the user
     * @param mediaId id of the media item
     * @throws ResourceNotFoundException if user or media is not found
     * @throws BusinessRuleException     if rules are violated (fine, overdue, not available)
     */
    public void borrow(String userId, String mediaId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));
        Media media = mediaRepo.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("media not found"));

        if (user.getOutstandingFine() > 0)
            throw new BusinessRuleException("User has outstanding fines");

        List<Loan> userLoans = loanRepo.findByUserId(userId);
        for (Loan l : userLoans) {
            if (l.isOverdue(timeProvider.today()))
                throw new BusinessRuleException("User has overdue loans");
        }

        if (!media.isAvailable())
            throw new BusinessRuleException("Media not available");

        LocalDate now = timeProvider.today();
        LocalDate due;

        if ("BOOK".equals(media.getMediaType())) {
            due = now.plusDays(28);
        } else if ("CD".equals(media.getMediaType())) {
            due = now.plusDays(7);
        } else {
            due = now.plusDays(28);
        }

        Loan loan = new Loan(userId, media.getId(), now, due);
        loanRepo.save(loan);
        media.setAvailable(false);
    }

    /**
     * Returns a borrowed media item and applies fines if overdue.
     * <ul>
     *     <li>Marks the loan as returned</li>
     *     <li>Makes the media item available again</li>
     *     <li>Calculates overdue fine using the proper strategy</li>
     * </ul>
     *
     * @param loanId id of the loan
     * @throws ResourceNotFoundException if loan is not found
     * @throws BusinessRuleException     if the loan was already returned
     */
    public void returnMedia(String loanId) {
        Loan loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("loan not found"));

        if (loan.isReturned())
            throw new BusinessRuleException("Already returned");

        LocalDate today = timeProvider.today();

        // Calculate overdue days before marking as returned
        int overdueDays = loan.overdueDays(today);

        // Mark returned
        loan.setReturnedDate(today);

        // Make media available
        Media media = mediaRepo.findById(loan.getMediaId()).orElse(null);
        if (media != null)
            media.setAvailable(true);

        // If overdue → apply fine
        if (overdueDays > 0) {
            int fine = 0;

            if (media != null) {
                if ("BOOK".equals(media.getMediaType()))
                    fine = bookFine.calculateFine(overdueDays);
                else if ("CD".equals(media.getMediaType()))
                    fine = cdFine.calculateFine(overdueDays);
            }

            if (fine > 0) {
                User user = userRepo.findById(loan.getUserId()).orElse(null);
                if (user != null)
                    user.addFine(fine);
            }
        }
    }

    /**
     * Applies a payment to a user's outstanding fine.
     *
     * @param userId id of the user
     * @param amount amount to pay
     * @throws ResourceNotFoundException if user is not found
     */
    public void payFine(String userId, int amount) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));
        u.payFine(amount);
    }

    /**
     * Unregisters a user from the system.
     * <ul>
     *     <li>Requires admin to be logged in</li>
     *     <li>User must not have active (unreturned) loans</li>
     *     <li>User must not have unpaid fines</li>
     * </ul>
     *
     * @param adminUser not used, just kept for possible future expansion
     * @param userId    id of the user to remove
     * @throws ResourceNotFoundException if user is not found
     * @throws BusinessRuleException     if user has active loans or unpaid fines
     */
    public void unregisterUser(String adminUser, String userId) {
        authService.requireAdmin();

        User u = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        List<Loan> loans = loanRepo.findByUserId(userId);
        boolean hasActive = loans.stream().anyMatch(l -> !l.isReturned());

        if (hasActive)
            throw new BusinessRuleException("User cannot be unregistered while having active loans");

        if (u.getOutstandingFine() > 0)
            throw new BusinessRuleException("User cannot be unregistered while having unpaid fines");

        userRepo.delete(u);
    }

    /**
     * @return the ReminderService associated with the library
     */
    public ReminderService getReminderService() {
        return reminderService;
    }

    /**
     * Finds all loans that belong to a user.
     *
     * @param userId id of the user
     * @return list of loans
     */
    public List<Loan> findLoansByUser(String userId) {
        return loanRepo.findByUserId(userId);
    }

    /**
     * Builds a simple text-based report of all active (not returned) loans.
     * Each line contains loan id, media, user, due date, status, and fine estimate.
     * The last line shows the total fine across all active overdue loans.
     *
     * @return list of report lines
     */
    public List<String> getBorrowedMediaReport() {
        LocalDate today = timeProvider.today();
        List<String> report = new java.util.ArrayList<>();
        int totalFine = 0;

        for (Loan loan : loanRepo.findAll()) {
            if (loan.isReturned()) continue;

            String loanId = loan.getId();
            String uid = loan.getUserId();
            String mid = loan.getMediaId();

            User user = userRepo.findById(uid).orElse(null);
            Media media = mediaRepo.findById(mid).orElse(null);

            String userPart = user != null ? user.getId() + " (" + user.getEmail() + ")" : uid;
            String mediaPart = media != null ? media.getId() + " - " + media.getTitle() : mid;

            String status;
            int fine = 0;

            if (loan.isOverdue(today)) {
                int days = loan.overdueDays(today);
                status = "OVERDUE by " + days + " day(s)";

                if (media != null) {
                    if ("BOOK".equals(media.getMediaType()))
                        fine = bookFine.calculateFine(days);
                    else if ("CD".equals(media.getMediaType()))
                        fine = cdFine.calculateFine(days);
                }
            } else {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, loan.getDueDate());
                status = "DUE in " + daysLeft + " day(s)";
            }

            totalFine += fine;
            String finePart = fine > 0 ? " | fine=" + fine : "";

            String line = String.format(
                    "%s | %s | %s | due=%s | %s%s",
                    loanId, mediaPart, userPart, loan.getDueDate(), status, finePart
            );

            report.add(line);
        }

        report.add("TOTAL OUTSTANDING FINE (for active loans): " + totalFine);
        return report;
    }
}

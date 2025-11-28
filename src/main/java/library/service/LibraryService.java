package library.service;

import library.domain.User;
import library.domain.Loan;
import library.domain.media.Book;
import library.domain.media.CD;
import library.domain.media.Media;
import library.exception.BusinessRuleException;
import library.exception.NotAuthorizedException;
import library.exception.ResourceNotFoundException;
import library.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * LibraryService: core operations.
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

    // User registration helper for tests/demo
    public User registerUser(String name, String email) {
        User u = new User(name, email);
        userRepo.save(u);
        return u;
    }

    public void addBook(String title, String author, String isbn) {
        Book b = new Book(title, author, isbn);
        mediaRepo.save(b);
    }

    public void addCD(String title, String artist) {
        CD cd = new CD(title, artist);
        mediaRepo.save(cd);
    }

    /**
     * Generic search (keeps previous behavior).
     */
    public List<Media> search(String q) {
        return mediaRepo.search(q == null ? "" : q);
    }

    /**
     * Search by title (partial match, case-insensitive).
     * Returns all media whose title contains the query.
     */
    public List<Media> searchByTitle(String title) {
        String q = title == null ? "" : title.toLowerCase();
        return mediaRepo.findAll().stream()
                .filter(m -> m.getTitle() != null && m.getTitle().toLowerCase().contains(q))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Search by author:
     * - For Book: match author field (case-insensitive, partial)
     * - For CD: match artist field (optional) — currently includes CDs whose artist matches the query
     *
     * Returns all matching media (books & optionally CDs) for the given author/artist.
     */
    public List<Media> searchByAuthor(String author) {
        String q = author == null ? "" : author.toLowerCase();
        return mediaRepo.findAll().stream()
                .filter(m -> {
                    if (m instanceof library.domain.media.Book) {
                        String a = ((library.domain.media.Book) m).getAuthor();
                        return a != null && a.toLowerCase().contains(q);
                    } else if (m instanceof library.domain.media.CD) {
                        String art = ((library.domain.media.CD) m).getArtist();
                        return art != null && art.toLowerCase().contains(q);
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Search by ISBN: exact match for Book.isbn (case-insensitive).
     * Returns a list (0 or 1 elements) to keep return type consistent with other searches.
     */
    public List<Media> searchByIsbn(String isbn) {
        if (isbn == null) return java.util.Collections.emptyList();
        String target = isbn.trim().toLowerCase();
        return mediaRepo.findAll().stream()
                .filter(m -> (m instanceof library.domain.media.Book) &&
                        ((library.domain.media.Book) m).getIsbn() != null &&
                        ((library.domain.media.Book) m).getIsbn().trim().toLowerCase().equals(target))
                .collect(java.util.stream.Collectors.toList());
    }


    public void borrow(String userId, String mediaId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user not found"));
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media not found"));
        // business rules
        // block if unpaid fines
        if (user.getOutstandingFine() > 0) throw new BusinessRuleException("User has outstanding fines");
        // block if user has overdue loans
        List<Loan> userLoans = loanRepo.findByUserId(userId);
        for (Loan l : userLoans) {
            if (l.isOverdue(timeProvider.today())) throw new BusinessRuleException("User has overdue loans");
        }
        if (!media.isAvailable()) throw new BusinessRuleException("Media not available");

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

    public void returnMedia(String loanId) {
        Loan loan = loanRepo.findById(loanId).orElseThrow(() -> new ResourceNotFoundException("loan not found"));
        if (loan.isReturned()) throw new BusinessRuleException("Already returned");
        LocalDate today = timeProvider.today();
        loan.setReturnedDate(today);
        Media media = mediaRepo.findById(loan.getMediaId()).orElse(null);
        if (media != null) media.setAvailable(true);
        // calculate fine if overdue
        int overdueDays = loan.overdueDays(today);
        if (overdueDays > 0) {
            int fine = 0;
            if (media != null) {
                if ("BOOK".equals(media.getMediaType())) fine = bookFine.calculateFine(overdueDays);
                else if ("CD".equals(media.getMediaType())) fine = cdFine.calculateFine(overdueDays);
            }
            User user = userRepo.findById(loan.getUserId()).orElse(null);
            if (user != null) user.addFine(fine);
        }
    }

    public void payFine(String userId, int amount) {
        User u = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user not found"));
        u.payFine(amount);
    }

    public void unregisterUser(String adminUser, String userId) {
        // admin-only: require auth
        if (!authService.isLoggedIn()) throw new NotAuthorizedException("Admin required");
        User u = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user not found"));
        // check active loans
        List<Loan> loans = loanRepo.findByUserId(userId);
        boolean hasActive = loans.stream().anyMatch(l -> !l.isReturned());
        if (hasActive) throw new BusinessRuleException("User has active loans");
        if (u.getOutstandingFine() > 0) throw new BusinessRuleException("User has unpaid fines");
        userRepo.delete(u);
    }

    public ReminderService getReminderService() { return reminderService; }

    // helper to find user's loans
    public List<Loan> findLoansByUser(String userId) { return loanRepo.findByUserId(userId); }
    
    /**
     * Builds a human-readable report of all currently borrowed media (non-returned loans).
     * Each line contains: LoanId | MediaId - Title | UserId (email) | dueDate | status (DUE in N days / OVERDUE by N days)
     */
    public java.util.List<String> getBorrowedMediaReport() {
        java.time.LocalDate today = timeProvider.today();
        java.util.List<String> report = new java.util.ArrayList<>();
        // iterate all loans
        for (library.domain.Loan loan : loanRepo.findAll()) {
            if (loan.isReturned()) continue; // only active loans
            String loanId = loan.getId();
            String uid = loan.getUserId();
            String mid = loan.getMediaId();
            java.util.Optional<library.domain.User> maybeUser = userRepo.findById(uid);
            java.util.Optional<library.domain.media.Media> maybeMedia = mediaRepo.findById(mid);

            String userPart = maybeUser.map(u -> u.getId() + " (" + u.getEmail() + ")").orElse(uid);
            String mediaPart = maybeMedia.map(m -> m.getId() + " - " + m.getTitle()).orElse(mid);

            String status;
            if (loan.isOverdue(today)) {
                int days = loan.overdueDays(today);
                status = "OVERDUE by " + days + " day(s)";
            } else {
                // days until due = dueDate - today
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, loan.getDueDate());
                status = "DUE in " + daysLeft + " day(s)";
            }

            String line = String.format("%s | %s | %s | due=%s | %s",
                    loanId, mediaPart, userPart, loan.getDueDate().toString(), status);
            report.add(line);
        }
        return report;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
}

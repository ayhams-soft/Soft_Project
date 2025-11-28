package library.service;

import library.domain.Admin;
import library.domain.Loan;
import library.domain.User;
import library.domain.media.Book;
import library.domain.media.CD;
import library.domain.media.Media;
import library.exception.BusinessRuleException;
import library.exception.NotAuthorizedException;
import library.exception.ResourceNotFoundException;
import library.repository.InMemoryAdminRepository;
import library.repository.InMemoryLoanRepository;
import library.repository.InMemoryMediaRepository;
import library.repository.InMemoryUserRepository;
import library.strategy.FineStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link LibraryService}.
 *
 * This class exercises most branches and business rules:
 * - user registration
 * - media management (books, CDs)
 * - search methods
 * - borrowing and returning items
 * - fine calculation
 * - unregistering users
 * - reporting borrowed media
 */
public class LibraryServiceTest {

    // ======= Fakes ثابتة للوقت والغرامات =======

    /**
     * Fixed implementation of {@link TimeProvider} that always returns
     * the same date. Useful for predictable tests.
     */
    static class FixedTimeProvider implements TimeProvider {
        private final LocalDate fixed;

        FixedTimeProvider(LocalDate fixed) {
            this.fixed = fixed;
        }

        @Override
        public LocalDate today() {
            return fixed;
        }
    }

    /**
     * Simple {@link FineStrategy} that always returns a constant amount,
     * regardless of overdue days. Used to simplify fine assertions in tests.
     */
    static class ConstantFineStrategy implements FineStrategy {
        private final int amount;

        ConstantFineStrategy(int amount) {
            this.amount = amount;
        }

        @Override
        public int calculateFine(int overdueDays) {
            return amount;
        }
    }

    private InMemoryUserRepository userRepo;
    private InMemoryMediaRepository mediaRepo;
    private InMemoryLoanRepository loanRepo;
    private InMemoryAdminRepository adminRepo;
    private ReminderService reminderService;
    private FixedTimeProvider timeProvider;
    private AuthService authService;
    private LibraryService libraryService;

    private User user;
    private Book book;
    private CD cd;

    /**
     * Initializes repositories, fixed time provider, strategies,
     * and a LibraryService instance before each test.
     */
    @BeforeEach
    void setUp() {
        userRepo  = new InMemoryUserRepository();
        mediaRepo = new InMemoryMediaRepository();
        loanRepo  = new InMemoryLoanRepository();
        adminRepo = new InMemoryAdminRepository();

        // نثبّت التاريخ على 2025-01-01
        timeProvider   = new FixedTimeProvider(LocalDate.of(2025, 1, 1));
        reminderService = new ReminderService(timeProvider);

        // نضيف أدمن عادي مثل AppConfig
        adminRepo.save(new Admin("admin", "admin"));
        authService = new AuthService(adminRepo);

        // نستخدم FineStrategy ثابتة عشان نعرف كم الغرامة بدقة
        FineStrategy bookFine = new ConstantFineStrategy(10); // 10 للكتاب
        FineStrategy cdFine   = new ConstantFineStrategy(20); // 20 للـ CD

        libraryService = new LibraryService(
                userRepo,
                mediaRepo,
                loanRepo,
                reminderService,
                timeProvider,
                bookFine,
                cdFine,
                authService
        );

        // نضيف يوزر و media للتجارب
        user = new User("demo", "demo@example.com");
        userRepo.save(user);

        book = new Book("Clean Code", "Robert C. Martin", "  ISBN-100 ");
        mediaRepo.save(book);

        cd = new CD("Greatest Hits", "MJ");
        mediaRepo.save(cd);
    }

    /**
     * Helper method to log in as admin using default credentials.
     */
    private void loginAsAdmin() {
        // مثال متوقع:
        authService.login("admin", "admin");
    }

    // =========================================================
    // 1) registerUser / addBook / addCD
    // =========================================================

    /**
     * Verifies that registerUser() creates a user and saves it to the repository.
     */
    @Test
    void registerUser_createsAndSavesUser() {
        User u = libraryService.registerUser("Ayham", "ayham@example.com");

        assertNotNull(u.getId());
        assertTrue(userRepo.findById(u.getId()).isPresent(), "User should be saved in repo");
    }

    /**
     * Ensures that addBook() adds a new Book to the media repository.
     */
    @Test
    void addBook_addsBookToMediaRepo() {
        libraryService.addBook("Refactoring", "Martin Fowler", "ISBN-200");

        List<Media> all = mediaRepo.findAll();
        assertTrue(all.stream().anyMatch(m ->
                        m instanceof Book && "Refactoring".equals(m.getTitle())),
                "Book 'Refactoring' should exist in repo");
    }

    /**
     * Ensures that addCD() adds a new CD to the media repository.
     */
    @Test
    void addCd_addsCdToMediaRepo() {
        libraryService.addCD("Thriller", "Michael Jackson");

        List<Media> all = mediaRepo.findAll();
        assertTrue(all.stream().anyMatch(m ->
                        m instanceof CD && "Thriller".equals(m.getTitle())),
                "CD 'Thriller' should exist in repo");
    }

    // =========================================================
    // 2) search / searchByTitle / searchByAuthor / searchByIsbn
    // =========================================================

    /**
     * Ensures that calling search() with null does not throw an exception.
     */
    @Test
    void search_withNullQuery_doesNotThrow() {
        List<Media> result = libraryService.search(null);
        assertNotNull(result);
        // حسب تنفيذ InMemoryMediaRepository ممكن يكون فيها عناصر أو لا،
        // المهم إن الميثود تم استدعاؤها بدون استثناء.
    }

    /**
     * Verifies that searchByTitle() matches titles case-insensitively
     * and by substring.
     */
    @Test
    void searchByTitle_findsBySubstringIgnoreCase() {
        libraryService.addBook("CLEAN ARCHITECTURE", "Bob", "ISBN-201");

        List<Media> result = libraryService.searchByTitle("clean");

        assertTrue(result.stream().anyMatch(m -> m.getTitle().equals("Clean Code")));
        assertTrue(result.stream().anyMatch(m -> m.getTitle().equals("CLEAN ARCHITECTURE")));
    }

    /**
     * Ensures that searchByAuthor() matches both Book authors and CD artists.
     */
    @Test
    void searchByAuthor_matchesBookAndCd() {
        Book b2 = new Book("Test Book", "Robbie Williams", "ISBN-300");
        mediaRepo.save(b2);

        CD cd2 = new CD("Hits", "Rob Stark");
        mediaRepo.save(cd2);

        List<Media> result = libraryService.searchByAuthor("rob");

        // Robert C. Martin, Robbie Williams, Rob Stark
        assertTrue(result.size() >= 2, "Should match at least book and cd with 'rob' in author/artist");
        assertTrue(result.stream().anyMatch(m -> m instanceof Book && ((Book)m).getAuthor().toLowerCase().contains("rob")));
        assertTrue(result.stream().anyMatch(m -> m instanceof CD && ((CD)m).getArtist().toLowerCase().contains("rob")));
    }

    /**
     * Ensures that searchByIsbn() trims and ignores case on ISBN string.
     */
    @Test
    void searchByIsbn_exactMatchIgnoreCaseAndTrim() {
        List<Media> result = libraryService.searchByIsbn("isbn-100");
        assertEquals(1, result.size());
        assertEquals(book.getId(), result.get(0).getId());
    }

    /**
     * Verifies that searchByIsbn(null) returns an empty list.
     */
    @Test
    void searchByIsbn_nullInputReturnsEmptyList() {
        List<Media> result = libraryService.searchByIsbn(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================
    // 3) borrow
    // =========================================================

    /**
     * For books: borrow() should set a due date of +28 days and mark the book unavailable.
     */
    @Test
    void borrowBook_setsDueDate28Days_andMarksBookUnavailable() {
        libraryService.borrow(user.getId(), book.getId());

        List<Loan> loans = loanRepo.findByUserId(user.getId());
        assertEquals(1, loans.size(), "Expected one loan");

        Loan loan = loans.get(0);
        assertEquals(timeProvider.today(), loan.getBorrowDate(), "borrow date should be today");
        assertEquals(timeProvider.today().plusDays(28), loan.getDueDate(), "book due date should be +28 days");

        Media storedBook = mediaRepo.findById(book.getId()).orElseThrow();
        assertFalse(storedBook.isAvailable(), "book should be not available after borrow");
    }

    /**
     * For CDs: borrow() should set a due date of +7 days.
     */
    @Test
    void borrowCd_setsDueDate7Days() {
        libraryService.borrow(user.getId(), cd.getId());

        List<Loan> loans = loanRepo.findByUserId(user.getId());
        assertEquals(1, loans.size());

        Loan loan = loans.get(0);
        assertEquals(timeProvider.today().plusDays(7), loan.getDueDate(),
                "CD due date should be +7 days");
    }

    /**
     * Borrowing should fail when the user has outstanding fines.
     */
    @Test
    void borrow_whenUserHasOutstandingFine_throwsBusinessRuleException() {
        user.addFine(50); // نضيف غرامة للمستخدم
        userRepo.save(user);

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> libraryService.borrow(user.getId(), book.getId())
        );

        assertEquals("User has outstanding fines", ex.getMessage());
    }

    /**
     * Borrowing should fail when the media is not available.
     */
    @Test
    void borrow_whenMediaNotAvailable_throwsBusinessRuleException() {
        book.setAvailable(false);
        mediaRepo.save(book);

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> libraryService.borrow(user.getId(), book.getId())
        );

        assertEquals("Media not available", ex.getMessage());
    }

    /**
     * Borrowing should fail when the user already has an overdue loan.
     */
    @Test
    void borrow_whenUserHasOverdueLoan_throwsBusinessRuleException() {
        // نعمل قرض قديم ومتأخر لنفس اليوزر
        LocalDate borrowDate = timeProvider.today().minusDays(10);
        LocalDate dueDate    = timeProvider.today().minusDays(5); // متأخر
        Loan oldLoan = new Loan(user.getId(), book.getId(), borrowDate, dueDate);
        loanRepo.save(oldLoan);

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> libraryService.borrow(user.getId(), cd.getId())
        );

        assertEquals("User has overdue loans", ex.getMessage());
    }

    /**
     * Borrowing should throw ResourceNotFoundException when the user is unknown.
     */
    @Test
    void borrow_unknownUser_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> libraryService.borrow("unknown-user", book.getId()));
    }

    /**
     * Borrowing should throw ResourceNotFoundException when the media is unknown.
     */
    @Test
    void borrow_unknownMedia_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> libraryService.borrow(user.getId(), "unknown-media"));
    }

    // =========================================================
    // 4) returnMedia
    // =========================================================

    /**
     * Returning an overdue book should:
     * - mark it as available
     * - add book fine (10 in this test setup)
     * - set returned date on the loan
     */
    @Test
    void returnMedia_overdueBook_addsBookFineAndMarksAvailable() {
        // نجهز loan متأخر (due قبل اليوم)
        LocalDate borrowDate = timeProvider.today().minusDays(30);
        LocalDate dueDate    = timeProvider.today().minusDays(2); // متأخر يومين مثلاً

        Loan loan = new Loan(user.getId(), book.getId(), borrowDate, dueDate);
        loanRepo.save(loan);

        // نخلي الكتاب غير متاح كأنه مستعار
        book.setAvailable(false);
        mediaRepo.save(book);

        libraryService.returnMedia(loan.getId());

        // الكتاب يرجع متاح
        Media storedBook = mediaRepo.findById(book.getId()).orElseThrow();
        assertTrue(storedBook.isAvailable(), "Book should be available after return");

        // الغرامة = 10 (حسب ConstantFineStrategy للكتاب)
        User storedUser = userRepo.findById(user.getId()).orElseThrow();
        assertEquals(10, storedUser.getOutstandingFine(), "User should have 10 fine");
        assertNotNull(loanRepo.findById(loan.getId()).get().getReturnedDate());
    }

    /**
     * Returning a non-overdue book should not add any fine and should mark it available.
     */
    @Test
    void returnMedia_notOverdueBook_noFineAndMarksAvailable() {
        LocalDate borrowDate = timeProvider.today().minusDays(2);
        LocalDate dueDate    = timeProvider.today().plusDays(5); // مش متأخر

        Loan loan = new Loan(user.getId(), book.getId(), borrowDate, dueDate);
        loanRepo.save(loan);

        book.setAvailable(false);
        mediaRepo.save(book);

        libraryService.returnMedia(loan.getId());

        Media storedBook = mediaRepo.findById(book.getId()).orElseThrow();
        assertTrue(storedBook.isAvailable());

        User storedUser = userRepo.findById(user.getId()).orElseThrow();
        assertEquals(0, storedUser.getOutstandingFine(), "Should not add fine when not overdue");
    }

    /**
     * Returning an overdue CD should add the CD fine (20 in this setup).
     */
    @Test
    void returnMedia_overdueCd_addsCdFine() {
        LocalDate borrowDate = timeProvider.today().minusDays(10);
        LocalDate dueDate    = timeProvider.today().minusDays(3); // متأخر

        Loan loan = new Loan(user.getId(), cd.getId(), borrowDate, dueDate);
        loanRepo.save(loan);

        cd.setAvailable(false);
        mediaRepo.save(cd);

        libraryService.returnMedia(loan.getId());

        User storedUser = userRepo.findById(user.getId()).orElseThrow();
        // cdFine ثابت 20
        assertEquals(20, storedUser.getOutstandingFine(), "CD fine should be 20");
    }

    /**
     * Returning a loan that is already returned should throw BusinessRuleException.
     */
    @Test
    void returnMedia_alreadyReturned_throwsBusinessRuleException() {
        LocalDate borrowDate = timeProvider.today().minusDays(10);
        LocalDate dueDate    = timeProvider.today().minusDays(3);

        Loan loan = new Loan(user.getId(), book.getId(), borrowDate, dueDate);
        loan.setReturnedDate(timeProvider.today().minusDays(1)); // already returned
        loanRepo.save(loan);

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> libraryService.returnMedia(loan.getId())
        );

        assertEquals("Already returned", ex.getMessage());
    }

    /**
     * Returning with an unknown loan id should throw ResourceNotFoundException.
     */
    @Test
    void returnMedia_unknownLoanId_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> libraryService.returnMedia("unknown-loan-id"));
    }

    // =========================================================
    // 5) payFine
    // =========================================================

    /**
     * payFine() should reduce the user's outstanding fine accordingly.
     */
    @Test
    void payFine_reducesOutstandingFine() {
        // نضيف غرامة 50
        user.addFine(50);
        userRepo.save(user);

        libraryService.payFine(user.getId(), 20);

        User stored = userRepo.findById(user.getId()).orElseThrow();
        assertEquals(30, stored.getOutstandingFine());
    }

    /**
     * Paying fine for an unknown user should throw ResourceNotFoundException.
     */
    @Test
    void payFine_forUnknownUser_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> libraryService.payFine("unknown-id", 10));
    }

    // =========================================================
    // 6) unregisterUser
    // =========================================================

    /**
     * A user with no loans and no fines can be unregistered (deleted).
     */
    @Test
    void unregisterUser_withNoLoansAndNoFines_deletesUser() {
        loginAsAdmin();

        libraryService.unregisterUser("admin", user.getId());

        assertFalse(userRepo.findById(user.getId()).isPresent(),
                "User should be removed from repo");
    }

    /**
     * unregisterUser() should fail if the user has an active (not returned) loan.
     */
    @Test
    void unregisterUser_withActiveLoan_throwsBusinessRuleException() {
        loginAsAdmin();

        Loan loan = new Loan(user.getId(), book.getId(),
                timeProvider.today().minusDays(1),
                timeProvider.today().plusDays(5));
        loanRepo.save(loan); // active (returnedDate null)

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> libraryService.unregisterUser("admin", user.getId())
        );

        assertEquals("User cannot be unregistered while having active loans", ex.getMessage());
    }

    /**
     * unregisterUser() should fail if the user still has unpaid fines.
     */
    @Test
    void unregisterUser_withOutstandingFine_throwsBusinessRuleException() {
        loginAsAdmin();

        user.addFine(40);
        userRepo.save(user);

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> libraryService.unregisterUser("admin", user.getId())
        );

        assertEquals("User cannot be unregistered while having unpaid fines", ex.getMessage());
    }

    /**
     * unregisterUser() with unknown user id should throw ResourceNotFoundException.
     */
    @Test
    void unregisterUser_unknownUser_throwsResourceNotFoundException() {
        loginAsAdmin();

        assertThrows(ResourceNotFoundException.class,
                () -> libraryService.unregisterUser("admin", "unknown-user"));
    }

    /**
     * unregisterUser() should throw NotAuthorizedException when called
     * without an active admin login.
     */
    @Test
    void unregisterUser_withoutAdminLogin_throwsNotAuthorizedException() {
        // ما نعمل loginAsAdmin
        assertThrows(NotAuthorizedException.class,
                () -> libraryService.unregisterUser("admin", user.getId()));
    }

    // =========================================================
    // 7) getReminderService / findLoansByUser
    // =========================================================

    /**
     * getReminderService() should return the same ReminderService instance
     * that was injected into the LibraryService.
     */
    @Test
    void getReminderService_returnsSameInstance() {
        assertSame(reminderService, libraryService.getReminderService());
    }

    /**
     * findLoansByUser() should return only the loans that belong to the given user.
     */
    @Test
    void findLoansByUser_returnsOnlyLoansOfThatUser() {
        Loan l1 = new Loan(user.getId(), book.getId(),
                timeProvider.today(), timeProvider.today().plusDays(10));
        loanRepo.save(l1);

        Loan l2 = new Loan(user.getId(), cd.getId(),
                timeProvider.today(), timeProvider.today().plusDays(5));
        loanRepo.save(l2);

        User other = new User("other", "other@example.com");
        userRepo.save(other);
        Loan otherLoan = new Loan(other.getId(), book.getId(),
                timeProvider.today(), timeProvider.today().plusDays(3));
        loanRepo.save(otherLoan);

        List<Loan> userLoans = libraryService.findLoansByUser(user.getId());

        assertEquals(2, userLoans.size());
        assertTrue(userLoans.stream().allMatch(l -> l.getUserId().equals(user.getId())));
    }

    // =========================================================
    // 8) getBorrowedMediaReport
    // =========================================================

    /**
     * For a single non-overdue loan, the report should contain a DUE line
     * and total fine of 0.
     */
    @Test
    void getBorrowedMediaReport_withOneDueLoan_returnsDueLineAndZeroTotalFine() {
        // Loan غير متأخر
        Loan loan = new Loan(user.getId(), book.getId(),
                timeProvider.today(), timeProvider.today().plusDays(5));
        loanRepo.save(loan);

        List<String> report = libraryService.getBorrowedMediaReport();

        // آخر سطر هو مجموع الغرامات
        assertFalse(report.isEmpty());
        String totalLine = report.get(report.size() - 1);
        assertEquals("TOTAL OUTSTANDING FINE (for active loans): 0", totalLine);

        // نتحقق إنه في سطر يحتوي DUE in 5 day(s)
        assertTrue(report.stream().anyMatch(line -> line.contains("DUE in 5 day(s)")));
    }

    /**
     * When there is an overdue book and an overdue CD, the report should
     * compute total fine = bookFine + cdFine (10 + 20 = 30 in this setup).
     */
    @Test
    void getBorrowedMediaReport_withOverdueBookAndCd_computesTotalFine() {
        // Book overdue → fine 10
        Loan bookLoan = new Loan(user.getId(), book.getId(),
                timeProvider.today().minusDays(10),
                timeProvider.today().minusDays(2));
        loanRepo.save(bookLoan);

        // CD overdue → fine 20
        Loan cdLoan = new Loan(user.getId(), cd.getId(),
                timeProvider.today().minusDays(5),
                timeProvider.today().minusDays(1));
        loanRepo.save(cdLoan);

        List<String> report = libraryService.getBorrowedMediaReport();

        assertFalse(report.isEmpty());
        String totalLine = report.get(report.size() - 1);
        assertEquals("TOTAL OUTSTANDING FINE (for active loans): 30", totalLine);

        // لازم نلاقي OVERDUE مرتين
        long overdueLines = report.stream().filter(line -> line.contains("OVERDUE by")).count();
        assertEquals(2, overdueLines);
    }
}

package library.service;

import library.domain.Loan;
import library.domain.User;
import library.domain.media.Book;
import library.domain.media.CD;
import library.domain.media.Media;
import library.dto.OverdueReport;
import library.repository.InMemoryLoanRepository;
import library.repository.InMemoryMediaRepository;
import library.repository.InMemoryUserRepository;
import library.strategy.FineStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReminderService}.
 * <p>
 * This class checks different reminder and report cases
 * using in-memory repositories and simple test helpers.
 */
public class ReminderServiceTest {

    /**
     * Simple {@link TimeProvider} used in tests to always return the same date.
     */
    static class FixedTimeProvider implements TimeProvider {
        /** Fixed date used as "today" in tests. */
        private final LocalDate fixed;

        /**
         * Creates a new fixed time provider.
         *
         * @param fixed the date that will be returned as today
         */
        FixedTimeProvider(LocalDate fixed) {
            this.fixed = fixed;
        }

        /**
         * Returns the fixed date instead of the real current date.
         *
         * @return the fixed test date
         */
        @Override
        public LocalDate today() {
            return fixed;
        }
    }

    /**
     * Test notifier that records all messages instead of sending real emails.
     */
    static class RecordingNotifier implements ReminderService.Notifier {

        /**
         * Small data holder for one recorded notification.
         */
        static class Recorded {
            /** ID of the user who received the message. */
            final String userId;
            /** Email address of the user. */
            final String email;
            /** Reminder message that was sent. */
            final String message;

            /**
             * Creates a new recorded notification.
             *
             * @param userId  user ID
             * @param email   email of the user
             * @param message reminder text
             */
            Recorded(String userId, String email, String message) {
                this.userId = userId;
                this.email = email;
                this.message = message;
            }
        }

        /** List of all recorded notifications. */
        private final java.util.List<Recorded> records = new java.util.ArrayList<>();

        /**
         * Stores a reminder in memory instead of sending it.
         *
         * @param user    the user that should receive the reminder
         * @param message the reminder message
         */
        @Override
        public void notify(User user, String message) {
            records.add(new Recorded(user.getId(), user.getEmail(), message));
        }

        /**
         * Returns all recorded notifications.
         *
         * @return list of recorded notifications
         */
        public java.util.List<Recorded> getRecords() {
            return records;
        }

        /**
         * Clears all previous notifications.
         */
        public void clear() {
            records.clear();
        }
    }

    /**
     * Dummy {@link Media} implementation with a custom media type (like DVD).
     */
    static class DummyMedia extends Media {

        /**
         * Creates a new dummy media instance.
         *
         * @param title the title of the media
         */
        public DummyMedia(String title) {
            super(title, "DVD");   // لازم mediaType إجباري
            setAvailable(true);
        }

        /**
         * Returns the custom media type used in tests.
         *
         * @return always "DVD"
         */
        @Override
        public String getMediaType() {
            return "DVD";  // مهم لأن ReminderService يعتمد على هذا
        }
    }

    /**
     * Simple {@link FineStrategy} that always returns the same fine value,
     * independent from the number of overdue days.
     */
    static class ConstantFineStrategy implements FineStrategy {
        /** Constant fine amount returned for any overdue loan. */
        private final int amount;

        /**
         * Creates a new constant fine strategy.
         *
         * @param amount the fixed fine amount
         */
        ConstantFineStrategy(int amount) {
            this.amount = amount;
        }

        /**
         * Returns the same fine amount for any number of overdue days.
         *
         * @param overdueDays number of overdue days (ignored)
         * @return fixed fine amount
         */
        @Override
        public int calculateFine(int overdueDays) {
            return amount;
        }
    }

    /** In-memory user repository used in tests. */
    private InMemoryUserRepository userRepo;
    /** In-memory media repository used in tests. */
    private InMemoryMediaRepository mediaRepo;
    /** In-memory loan repository used in tests. */
    private InMemoryLoanRepository loanRepo;
    /** Time provider with a fixed test date. */
    private FixedTimeProvider timeProvider;
    /** Service under test. */
    private ReminderService reminderService;
    /** Notifier that records all sent messages. */
    private RecordingNotifier recordingNotifier;

    /** User with a valid email address. */
    private User userWithEmail;
    /** User without an email address. */
    private User userWithoutEmail;
    /** Book used in several test cases. */
    private Book book;
    /** CD used in several test cases. */
    private CD cd;

    /**
     * Initializes repositories, test data and the {@link ReminderService}
     * before each test method is executed.
     */
    @BeforeEach
    void setUp() {
        userRepo  = new InMemoryUserRepository();
        mediaRepo = new InMemoryMediaRepository();
        loanRepo  = new InMemoryLoanRepository();

        timeProvider    = new FixedTimeProvider(LocalDate.of(2025, 1, 1));
        reminderService = new ReminderService(timeProvider);

        recordingNotifier = new RecordingNotifier();
        reminderService.registerNotifier(recordingNotifier);

        // Users
        userWithEmail = new User("WithEmail", "with@example.com");
        userRepo.save(userWithEmail);

        userWithoutEmail = new User("NoEmail", null);
        userRepo.save(userWithoutEmail);

        // Media
        book = new Book("Clean Code", "Robert C. Martin", "ISBN-100");
        mediaRepo.save(book);

        cd = new CD("Greatest Hits", "MJ");
        mediaRepo.save(cd);
    }

    // =========================================================
    // 1) sendReminders: حالات أساسية
    // =========================================================

    /**
     * Verifies that no reminder messages are sent
     * when there are no overdue loans in the repository.
     */
    @Test
    void sendReminders_whenNoOverdueLoans_sendsNoMessages() {
        reminderService.sendReminders(loanRepo, userRepo, mediaRepo);

        assertTrue(recordingNotifier.getRecords().isEmpty(),
                "No messages should be sent when there are no overdue loans");
    }

    /**
     * Verifies that a single user with one overdue book and one overdue CD
     * receives exactly one combined reminder message.
     */
    @Test
    void sendReminders_oneUserWithOverdueBookAndCd_sendsCombinedMessage() {
        LocalDate borrowDate = timeProvider.today().minusDays(30);
        LocalDate overdueDueDate = timeProvider.today().minusDays(3); // متأخر

        Loan loanBook = new Loan(userWithEmail.getId(), book.getId(), borrowDate, overdueDueDate);
        Loan loanCd   = new Loan(userWithEmail.getId(), cd.getId(),   borrowDate, overdueDueDate);

        loanRepo.save(loanBook);
        loanRepo.save(loanCd);

        reminderService.sendReminders(loanRepo, userRepo, mediaRepo);

        List<RecordingNotifier.Recorded> records = recordingNotifier.getRecords();
        assertEquals(1, records.size(), "Should send exactly one message for the user");

        RecordingNotifier.Recorded rec = records.get(0);
        assertEquals(userWithEmail.getId(), rec.userId);
        assertEquals("with@example.com", rec.email);

        String expectedMessage = "You have 1 overdue book(s) and 1 overdue CD(s).";
        assertEquals(expectedMessage, rec.message);
    }

    /**
     * Verifies that a user without an email address
     * does not receive any reminder messages.
     */
    @Test
    void sendReminders_userWithoutEmail_isSkipped() {
        LocalDate borrowDate = timeProvider.today().minusDays(20);
        LocalDate overdueDueDate = timeProvider.today().minusDays(5);

        Loan loanNoEmail = new Loan(userWithoutEmail.getId(), book.getId(), borrowDate, overdueDueDate);
        loanRepo.save(loanNoEmail);

        reminderService.sendReminders(loanRepo, userRepo, mediaRepo);

        assertTrue(recordingNotifier.getRecords().isEmpty(),
                "User without email should not receive reminders");
    }

    // =========================================================
    // 2) sendReminders: فروع إضافية (حواف)
    // =========================================================

    /**
     * Verifies that calling {@link ReminderService#registerNotifier(ReminderService.Notifier)}
     * with {@code null} does not crash the application.
     */
    @Test
    void registerNotifier_null_isIgnored() {
        reminderService.registerNotifier(null);

        // ما في overdue → ما في رسائل، المهم ما ينهار
        reminderService.sendReminders(loanRepo, userRepo, mediaRepo);
        assertTrue(recordingNotifier.getRecords().isEmpty());
    }

    /**
     * Verifies that overdue loans with missing media in the repository
     * are simply ignored by {@link ReminderService#sendReminders}.
     */
    @Test
    void sendReminders_whenMediaNotFound_skipsThatLoan() {
        LocalDate borrowDate = timeProvider.today().minusDays(10);
        LocalDate dueDate    = timeProvider.today().minusDays(3);

        Loan loan = new Loan(userWithEmail.getId(), "unknown-media", borrowDate, dueDate);
        loanRepo.save(loan);

        reminderService.sendReminders(loanRepo, userRepo, mediaRepo);

        assertTrue(recordingNotifier.getRecords().isEmpty(),
                "Loan with missing media should be ignored");
    }

    /**
     * Verifies that media with an unknown type (not book or CD)
     * does not produce any reminder messages.
     */
    @Test
    void sendReminders_unknownMediaType_isIgnored() {
        Media dvd = new DummyMedia("Some DVD");
        mediaRepo.save(dvd);

        LocalDate borrowDate = timeProvider.today().minusDays(10);
        LocalDate dueDate    = timeProvider.today().minusDays(2);

        Loan loan = new Loan(userWithEmail.getId(), dvd.getId(), borrowDate, dueDate);
        loanRepo.save(loan);

        reminderService.sendReminders(loanRepo, userRepo, mediaRepo);

        assertTrue(recordingNotifier.getRecords().isEmpty(),
                "Unknown media type should not produce reminder");
    }

    /**
     * Verifies that if one notifier throws an exception,
     * the other notifiers are still executed.
     */
    @Test
    void sendReminders_whenOneNotifierFails_othersStillRun() {
        // Notifier أول يرمي Exception
        ReminderService.Notifier failing = (user, msg) -> { throw new RuntimeException("boom"); };
        reminderService.registerNotifier(failing);

        LocalDate borrowDate = timeProvider.today().minusDays(15);
        LocalDate dueDate    = timeProvider.today().minusDays(5);

        Loan loan = new Loan(userWithEmail.getId(), book.getId(), borrowDate, dueDate);
        loanRepo.save(loan);

        reminderService.sendReminders(loanRepo, userRepo, mediaRepo);

        // لازم recordingNotifier يشتغل رغم فشل الثاني
        assertEquals(1, recordingNotifier.getRecords().size());
    }

    // =========================================================
    // 3) buildReport
    // =========================================================

    /**
     * Verifies that {@link ReminderService#buildReport} calculates
     * correct overdue counts and fine sums for books and CDs.
     */
    @Test
    void buildReport_countsAndFinesAreCorrect() {
        // userWithEmail: 2 كتب متأخرة + 1 CD متأخر
        LocalDate borrowDate = timeProvider.today().minusDays(40);
        LocalDate overdueDueDate = timeProvider.today().minusDays(10);

        Loan loan1 = new Loan(userWithEmail.getId(), book.getId(), borrowDate, overdueDueDate);
        Loan loan2 = new Loan(userWithEmail.getId(), book.getId(), borrowDate, overdueDueDate);
        Loan loan3 = new Loan(userWithEmail.getId(), cd.getId(),   borrowDate, overdueDueDate);

        loanRepo.save(loan1);
        loanRepo.save(loan2);
        loanRepo.save(loan3);

        FineStrategy bookFine = new ConstantFineStrategy(10);
        FineStrategy cdFine   = new ConstantFineStrategy(20);

        OverdueReport report = reminderService.buildReport(
                loanRepo, userRepo, mediaRepo, bookFine, cdFine
        );

        Map<String, Integer> counts = report.getOverdueCounts();
        Map<String, Integer> sums   = report.getFineTotals();

        // 2 كتب + 1 CD = 3 عناصر متأخرة
        assertEquals(3, counts.get(userWithEmail.getId()).intValue());

        // المجموع: 2 * 10 (كتب) + 1 * 20 (CD) = 40
        assertEquals(40, sums.get(userWithEmail.getId()).intValue());
    }

    /**
     * Verifies that missing media are counted as overdue in the report,
     * but they do not add any fine amount.
     */
    @Test
    void buildReport_whenMediaMissing_stillCountsButNoFine() {
        LocalDate borrowDate = timeProvider.today().minusDays(10);
        LocalDate dueDate    = timeProvider.today().minusDays(3);

        Loan loan = new Loan(userWithEmail.getId(), "missing-media", borrowDate, dueDate);
        loanRepo.save(loan);

        FineStrategy bookFine = new ConstantFineStrategy(10);
        FineStrategy cdFine   = new ConstantFineStrategy(20);

        OverdueReport report = reminderService.buildReport(
                loanRepo, userRepo, mediaRepo, bookFine, cdFine
        );

        assertEquals(1, report.getOverdueCounts().get(userWithEmail.getId()));
        assertEquals(0, report.getFineTotals().get(userWithEmail.getId()));
    }

    /**
     * Verifies that media with an unknown type are still counted as overdue,
     * but no fine is added for them in the report.
     */
    @Test
    void buildReport_unknownMediaType_countsButNoFine() {
        Media dvd = new DummyMedia("Some DVD");
        mediaRepo.save(dvd);

        LocalDate borrowDate = timeProvider.today().minusDays(8);
        LocalDate dueDate    = timeProvider.today().minusDays(2);

        Loan loan = new Loan(userWithEmail.getId(), dvd.getId(), borrowDate, dueDate);
        loanRepo.save(loan);

        FineStrategy bookFine = new ConstantFineStrategy(10);
        FineStrategy cdFine   = new ConstantFineStrategy(20);

        OverdueReport report = reminderService.buildReport(
                loanRepo, userRepo, mediaRepo, bookFine, cdFine
        );

        assertEquals(1, report.getOverdueCounts().get(userWithEmail.getId()));
        assertEquals(0, report.getFineTotals().get(userWithEmail.getId()));
    }
}

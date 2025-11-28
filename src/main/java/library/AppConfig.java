package library;

import library.domain.Admin;
import library.domain.User;
import library.domain.media.Book;
import library.domain.media.CD;
import library.notifications.FakeEmailClient;
import library.repository.*;
import library.service.*;
import library.strategy.BookFineStrategy;
import library.strategy.CDFineStrategy;

/**
 * Application configuration — dependency wiring + seed data.
 * Registers both Console Notifier and a FakeEmailClient for test-mode recording.
 */
public class AppConfig {

    private final InMemoryAdminRepository adminRepo = new InMemoryAdminRepository();
    private final InMemoryUserRepository userRepo = new InMemoryUserRepository();
    private final InMemoryMediaRepository mediaRepo = new InMemoryMediaRepository();
    private final InMemoryLoanRepository loanRepo = new InMemoryLoanRepository();
    private final TimeProvider timeProvider = new SystemTimeProvider();

    // ReminderService constructed with timeProvider
    private final ReminderService reminderService = new ReminderService(timeProvider);

    // Fake email client for test mode (records sent messages)
    private final FakeEmailClient fakeEmailClient = new FakeEmailClient();

    private final AuthService authService = new AuthService(adminRepo);
    private final LibraryService libraryService =
            new LibraryService(userRepo, mediaRepo, loanRepo, reminderService, timeProvider,
                    new BookFineStrategy(), new CDFineStrategy(), authService);

    public AppConfig() {
        seedAdmins();
        seedUsers();
        seedMedia();
        registerNotifiers(); // register notifiers: console + fake email
    }

    /** Seed admin accounts */
    private void seedAdmins() {
        adminRepo.save(new Admin("admin", "admin"));   // default
        adminRepo.save(new Admin("ayham", "1234"));    // NEW admin requested
    }

    /** Seed demo users */
    private void seedUsers() {
        userRepo.save(new User("demo", "demo@example.com"));
        userRepo.save(new User("ahmad", "ahmad@gmail.com"));
        userRepo.save(new User("mona", "mona@yahoo.com"));
        userRepo.save(new User("yazan", "yazan@hotmail.com"));
    }

    /** Seed books + CDs */
    private void seedMedia() {
        // Books
        mediaRepo.save(new Book("Clean Code", "Robert C. Martin", "ISBN-100"));
        mediaRepo.save(new Book("Effective Java", "Joshua Bloch", "ISBN-200"));
        mediaRepo.save(new Book("Design Patterns", "GoF", "ISBN-300"));
        mediaRepo.save(new Book("Refactoring", "Martin Fowler", "ISBN-400"));

        // CDs
        mediaRepo.save(new CD("Greatest Hits", "Michael Jackson"));
        mediaRepo.save(new CD("Classical Collection", "Mozart"));
        mediaRepo.save(new CD("Rock Legends", "Pink Floyd"));
    }

    /** Register console notifier + fake email notifier */
    private void registerNotifiers() {
        // Console notifier: prints reminders to standard output; helpful for manual testing by admin
        reminderService.registerNotifier((user, message) -> {
            if (user != null) {
                System.out.println("Reminder -> " + user.getEmail() + " : " + message);
            } else {
                System.out.println("Reminder -> unknown user : " + message);
            }
        });

        // FakeEmail notifier: delegates to FakeEmailClient (records messages)
        reminderService.registerNotifier((user, message) -> {
            if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                fakeEmailClient.send(user.getEmail(), "Library reminder", message);
            }
        });
    }

    // Getters for DI and for Main usage
    public AdminRepository adminRepository() { return adminRepo; }
    public UserRepository userRepository() { return userRepo; }
    public MediaRepository mediaRepository() { return mediaRepo; }
    public LoanRepository loanRepository() { return loanRepo; }
    public TimeProvider timeProvider() { return timeProvider; }
    public ReminderService reminderService() { return reminderService; }
    public AuthService authService() { return authService; }
    public LibraryService libraryService() { return libraryService; }

    // Expose fakeEmailClient for manual inspection in tests or from Main if needed
    public FakeEmailClient fakeEmailClient() { return fakeEmailClient; }
}

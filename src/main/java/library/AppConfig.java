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
 * AppConfig: sets up the main components of the library system.
 * This class wires the services and adds some initial sample data.
 */
public class AppConfig {

    private final InMemoryAdminRepository adminRepo = new InMemoryAdminRepository();
    private final InMemoryUserRepository userRepo = new InMemoryUserRepository();
    private final InMemoryMediaRepository mediaRepo = new InMemoryMediaRepository();
    private final InMemoryLoanRepository loanRepo = new InMemoryLoanRepository();
    private final TimeProvider timeProvider = new SystemTimeProvider();

    // Reminder service uses the time provider
    private final ReminderService reminderService = new ReminderService(timeProvider);

    // Fake email client (mainly for testing and checking sent messages)
    private final FakeEmailClient fakeEmailClient = new FakeEmailClient();

    private final AuthService authService = new AuthService(adminRepo);
    private final LibraryService libraryService =
            new LibraryService(userRepo, mediaRepo, loanRepo, reminderService, timeProvider,
                    new BookFineStrategy(), new CDFineStrategy(), authService);

    /**
     * Constructor: loads the seed data and registers the notifiers.
     */
    public AppConfig() {
        seedAdmins();
        seedUsers();
        seedMedia();
        registerNotifiers();
    }

    /**
     * Adds some default admin accounts.
     */
    private void seedAdmins() {
        adminRepo.save(new Admin("admin", "admin"));
        adminRepo.save(new Admin("ayham", "1234"));
    }

    /**
     * Adds some sample users to start with.
     */
    private void seedUsers() {
        userRepo.save(new User("ahmad", "ahmad@gmail.com"));
        userRepo.save(new User("mona", "mona@yahoo.com"));
        userRepo.save(new User("yazan", "yazan@hotmail.com"));
    }

    /**
     * Adds some sample books and CDs.
     */
    private void seedMedia() {
        mediaRepo.save(new Book("Clean Code", "Robert C. Martin", "ISBN-100"));
        mediaRepo.save(new Book("Effective Java", "Joshua Bloch", "ISBN-200"));
        mediaRepo.save(new Book("Design Patterns", "GoF", "ISBN-300"));
        mediaRepo.save(new Book("Refactoring", "Martin Fowler", "ISBN-400"));

        mediaRepo.save(new CD("Greatest Hits", "Michael Jackson"));
        mediaRepo.save(new CD("Classical Collection", "Mozart"));
        mediaRepo.save(new CD("Rock Legends", "Pink Floyd"));
    }

    /**
     * Registers two notifiers:
     * 1) Console output
     * 2) Fake email sender
     */
    private void registerNotifiers() {

        // Console notifier
        reminderService.registerNotifier((user, message) -> {
            if (user != null) {
                System.out.println("Reminder -> " + user.getEmail() + " : " + message);
            } else {
                System.out.println("Reminder -> unknown user : " + message);
            }
        });

        // Fake email notifier
        reminderService.registerNotifier((user, message) -> {
            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                fakeEmailClient.send(user.getEmail(), "Library reminder", message);
            }
        });
    }

    /** @return admin repository */
    public AdminRepository adminRepository() { return adminRepo; }

    /** @return user repository */
    public UserRepository userRepository() { return userRepo; }

    /** @return media repository */
    public MediaRepository mediaRepository() { return mediaRepo; }

    /** @return loan repository */
    public LoanRepository loanRepository() { return loanRepo; }

    /** @return time provider used in the system */
    public TimeProvider timeProvider() { return timeProvider; }

    /** @return reminder service */
    public ReminderService reminderService() { return reminderService; }

    /** @return authentication service */
    public AuthService authService() { return authService; }

    /** @return library service */
    public LibraryService libraryService() { return libraryService; }

    /** @return fake email client (used for tests) */
    public FakeEmailClient fakeEmailClient() { return fakeEmailClient; }
}

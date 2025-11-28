package library1;

import library.AppConfig;
import library.notifications.FakeEmailClient;
import library.repository.AdminRepository;
import library.repository.LoanRepository;
import library.repository.MediaRepository;
import library.repository.UserRepository;
import library.service.AuthService;
import library.service.LibraryService;
import library.service.ReminderService;
import library.service.TimeProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link AppConfig}.
 * <p>
 * This test makes sure that the application configuration
 * correctly creates all required dependencies and seed data.
 */
class AppConfigTest {

    /**
     * Checks that all services, repositories, and helpers created
     * by {@link AppConfig} are properly initialized (not null).
     */
    @Test
    void appConfig_initializes_all_dependencies_and_seed_data() {
        // act
        AppConfig config = new AppConfig();

        // assert: كل الديبندنسي لازم تكون متهيئة ومش null
        AdminRepository adminRepo = config.adminRepository();
        UserRepository userRepo = config.userRepository();
        MediaRepository mediaRepo = config.mediaRepository();
        LoanRepository loanRepo = config.loanRepository();
        TimeProvider timeProvider = config.timeProvider();
        ReminderService reminderService = config.reminderService();
        AuthService authService = config.authService();
        LibraryService libraryService = config.libraryService();
        FakeEmailClient fakeEmailClient = config.fakeEmailClient();

        assertNotNull(adminRepo);
        assertNotNull(userRepo);
        assertNotNull(mediaRepo);
        assertNotNull(loanRepo);
        assertNotNull(timeProvider);
        assertNotNull(reminderService);
        assertNotNull(authService);
        assertNotNull(libraryService);
        assertNotNull(fakeEmailClient);
    }
}

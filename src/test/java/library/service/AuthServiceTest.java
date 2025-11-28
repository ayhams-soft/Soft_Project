package library.service;

import library.domain.Admin;
import library.exception.NotAuthorizedException;
import library.repository.InMemoryAdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AuthService}.
 *
 * Tests cover login, logout, authorization checks,
 * and different paths for valid/invalid credentials.
 */
class AuthServiceTest {

    private InMemoryAdminRepository repo;
    private AuthService auth;

    /**
     * Prepare a fresh repository and AuthService before each test.
     * Adds a sample admin account for login tests.
     */
    @BeforeEach
    void setUp() {
        repo = new InMemoryAdminRepository();
        auth = new AuthService(repo);

        // add test admin user
        repo.save(new Admin("ayham", "1234"));
    }

    /**
     * Verifies that login succeeds when correct credentials are provided.
     */
    @Test
    void login_success_whenCredentialsAreCorrect() {
        boolean result = auth.login("ayham", "1234");

        assertTrue(result);
        assertTrue(auth.isLoggedIn());
    }

    /**
     * Checks that login fails when the username does not exist.
     */
    @Test
    void login_fails_whenUsernameNotFound() {
        boolean result = auth.login("wrongUser", "1234");

        assertFalse(result);
        assertFalse(auth.isLoggedIn());
    }

    /**
     * Ensures that login fails when the password is incorrect.
     */
    @Test
    void login_fails_whenPasswordIsWrong() {
        boolean result = auth.login("ayham", "wrongpass");

        assertFalse(result);
        assertFalse(auth.isLoggedIn());
    }

    /**
     * Confirms that logout clears the current admin session.
     */
    @Test
    void logout_clearsCurrentAdmin() {
        auth.login("ayham", "1234");
        auth.logout();

        assertFalse(auth.isLoggedIn());
    }

    /**
     * Ensures that requireAdmin() throws an exception
     * when no admin session exists.
     */
    @Test
    void requireAdmin_throwsException_whenNotLoggedIn() {
        assertThrows(NotAuthorizedException.class, () -> auth.requireAdmin());
    }

    /**
     * Ensures that requireAdmin() does not throw an exception
     * when an admin is logged in.
     */
    @Test
    void requireAdmin_allowsAccess_whenLoggedIn() {
        auth.login("ayham", "1234");

        assertDoesNotThrow(() -> auth.requireAdmin());
    }
}

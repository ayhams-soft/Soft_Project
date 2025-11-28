package library.repository;

import library.domain.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InMemoryAdminRepository}.
 *
 * These tests verify that the repository:
 *  - Stores admins correctly.
 *  - Retrieves admins by username.
 *  - Returns an empty result when no match is found.
 */
class InMemoryAdminRepositoryTest {

    private InMemoryAdminRepository repo;

    /**
     * Sets up a fresh repository before each test run
     * and loads a couple of sample admin accounts.
     */
    @BeforeEach
    void setup() {
        repo = new InMemoryAdminRepository();
        repo.save(new Admin("ayham", "1234"));
        repo.save(new Admin("admin", "admin"));
    }

    /**
     * Ensures that save() correctly stores admin entries.
     */
    @Test
    void save_storesAdminsCorrectly() {
        Optional<Admin> found = repo.findByUsername("ayham");
        assertTrue(found.isPresent());
        assertEquals("ayham", found.get().getUsername());
    }

    /**
     * Verifies that findByUsername() returns the correct admin
     * when the username exists.
     */
    @Test
    void findByUsername_returnsCorrectAdmin() {
        Optional<Admin> found = repo.findByUsername("admin");
        assertTrue(found.isPresent());
        assertEquals("admin", found.get().getUsername());
    }

    /**
     * Ensures that findByUsername() returns an empty Optional
     * when the username is not stored.
     */
    @Test
    void findByUsername_returnsEmptyWhenNotFound() {
        assertTrue(repo.findByUsername("notexists").isEmpty());
    }
}

package library.repository;

import library.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InMemoryUserRepository}.
 *
 * These tests verify storing, retrieving, searching,
 * and deleting users inside the in-memory repository.
 */
class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repo;

    /**
     * Initializes the repository with sample users before each test.
     */
    @BeforeEach
    void setup() {
        repo = new InMemoryUserRepository();
        repo.save(new User("ayham", "ayham@test.com"));
        repo.save(new User("mona", "mona@test.com"));
    }

    /**
     * Ensures that save() stores users and findAll() returns all of them.
     */
    @Test
    void save_and_findAll_shouldReturnAllUsers() {
        List<User> all = repo.findAll();
        assertEquals(2, all.size());
    }

    /**
     * Confirms that findById() returns the correct user.
     */
    @Test
    void findById_shouldReturnCorrectUser() {
        User u = repo.findAll().get(0);
        Optional<User> found = repo.findById(u.getId());

        assertTrue(found.isPresent());
        assertEquals(u.getEmail(), found.get().getEmail());
    }

    /**
     * Verifies that findById() returns empty for an unknown ID.
     */
    @Test
    void findById_shouldReturnEmptyForUnknownId() {
        assertTrue(repo.findById("unknown").isEmpty());
    }

    /**
     * Confirms that findByEmail() finds the correct user.
     */
    @Test
    void findByEmail_shouldReturnCorrectUser() {
        Optional<User> found = repo.findByEmail("mona@test.com");

        assertTrue(found.isPresent());
        assertEquals("mona", found.get().getName());
    }

    /**
     * Ensures that findByEmail() returns empty if no user matches the email.
     */
    @Test
    void findByEmail_shouldReturnEmptyForUnknownEmail() {
        assertTrue(repo.findByEmail("nothing@test.com").isEmpty());
    }

    /**
     * Ensures that delete() removes the user from the repository.
     */
    @Test
    void delete_shouldRemoveUser() {
        User u = repo.findAll().get(0);
        repo.delete(u);

        assertEquals(1, repo.findAll().size());
        assertTrue(repo.findById(u.getId()).isEmpty());
    }
}

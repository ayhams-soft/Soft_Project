package library.repository;

import library.domain.media.Book;
import library.domain.media.CD;
import library.domain.media.Media;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InMemoryMediaRepository}.
 *
 * These tests verify that media items (books and CDs) are stored,
 * retrieved, and searched correctly in the in-memory repository.
 */
class InMemoryMediaRepositoryTest {

    private InMemoryMediaRepository repo;

    /**
     * Initializes the repository with sample media items before each test.
     */
    @BeforeEach
    void setup() {
        repo = new InMemoryMediaRepository();
        repo.save(new Book("Clean Code", "Robert Martin", "ISBN-100"));
        repo.save(new CD("Greatest Hits", "Michael Jackson"));
    }

    /**
     * Ensures that save() correctly stores items and findAll() returns them.
     */
    @Test
    void save_and_findAll_shouldWork() {
        List<Media> all = repo.findAll();
        assertEquals(2, all.size());
    }

    /**
     * Confirms that findById() returns the correct stored media.
     */
    @Test
    void findById_shouldReturnCorrectItem() {
        Media m = repo.findAll().get(0);
        assertTrue(repo.findById(m.getId()).isPresent());
    }

    /**
     * Searching by title should return the matching Book.
     */
    @Test
    void search_byTitle_shouldReturnBook() {
        List<Media> results = repo.search("Clean");
        assertEquals(1, results.size());
        assertTrue(results.get(0) instanceof Book);
    }

    /**
     * Searching by author name should return the corresponding Book.
     */
    @Test
    void search_byAuthor_shouldReturnBook() {
        List<Media> results = repo.search("Martin");
        assertEquals(1, results.size());
        assertTrue(results.get(0) instanceof Book);
    }

    /**
     * Searching by ISBN should return the correct Book.
     */
    @Test
    void search_byIsbn_shouldReturnBook() {
        List<Media> results = repo.search("ISBN-100");
        assertEquals(1, results.size());
    }

    /**
     * Searching by CD artist name should return the matching CD.
     */
    @Test
    void search_byArtist_shouldReturnCD() {
        List<Media> results = repo.search("Michael");
        assertEquals(1, results.size());
        assertTrue(results.get(0) instanceof CD);
    }

    /**
     * Searching by ID substring should return matching items.
     */
    @Test
    void search_byId_shouldReturnBoth() {
        Media m = repo.findAll().get(1);
        List<Media> results = repo.search(m.getId().substring(0, 2).toLowerCase());
        assertFalse(results.isEmpty());
    }

    /**
     * Null or empty search query should return all items.
     */
    @Test
    void search_emptyOrNull_shouldReturnAll() {
        assertEquals(2, repo.search(null).size());
        assertEquals(2, repo.search("").size());
    }
}

package library.repository;

import library.domain.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InMemoryLoanRepository}.
 *
 * These tests verify that the repository correctly stores,
 * retrieves, filters, and deletes Loan entities.
 */
class InMemoryLoanRepositoryTest {

    private InMemoryLoanRepository repo;

    private Loan loan1;
    private Loan loan2;

    /**
     * Sets up a repository with sample loan data before each test.
     */
    @BeforeEach
    void setup() {
        repo = new InMemoryLoanRepository();

        loan1 = new Loan("user1", "m1", LocalDate.now(), LocalDate.now().plusDays(7));
        loan2 = new Loan("user1", "m2", LocalDate.now(), LocalDate.now().plusDays(5));
        Loan loan3 = new Loan("user2", "m3", LocalDate.now(), LocalDate.now().plusDays(4));

        repo.save(loan1);
        repo.save(loan2);
        repo.save(loan3);
    }

    /**
     * Ensures that save() correctly stores loan entries
     * and findAll() returns all saved loans.
     */
    @Test
    void save_and_findAll_shouldReturnAllLoans() {
        List<Loan> all = repo.findAll();
        assertEquals(3, all.size());
    }

    /**
     * Verifies that findById() returns the correct loan when it exists.
     */
    @Test
    void findById_shouldReturnCorrectLoan() {
        Optional<Loan> found = repo.findById(loan1.getId());
        assertTrue(found.isPresent());
        assertEquals(loan1.getUserId(), found.get().getUserId());
    }

    /**
     * Ensures that findById() returns an empty Optional
     * when the loan ID does not exist.
     */
    @Test
    void findById_shouldReturnEmptyForUnknownId() {
        assertTrue(repo.findById("unknown").isEmpty());
    }

    /**
     * Verifies that findByUserId() returns all loans
     * associated with the given user.
     */
    @Test
    void findByUserId_shouldReturnAllLoansForUser() {
        List<Loan> loans = repo.findByUserId("user1");
        assertEquals(2, loans.size());
    }

    /**
     * Ensures that findByUserId() returns an empty list
     * when the user has no loans.
     */
    @Test
    void findByUserId_shouldReturnEmptyForUnknownUser() {
        List<Loan> loans = repo.findByUserId("none");
        assertTrue(loans.isEmpty());
    }

    /**
     * Ensures that delete() properly removes the loan
     * from the repository.
     */
    @Test
    void delete_shouldRemoveLoan() {
        repo.delete(loan1);

        assertEquals(2, repo.findAll().size());
        assertTrue(repo.findById(loan1.getId()).isEmpty());
    }
}

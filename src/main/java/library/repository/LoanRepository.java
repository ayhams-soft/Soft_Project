package library.repository;

import library.domain.Loan;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for storing and retrieving loan records.
 */
public interface LoanRepository {

    /**
     * Saves a loan to the repository.
     *
     * @param loan the loan to store
     */
    void save(Loan loan);

    /**
     * Finds a loan by its id.
     *
     * @param id the loan id
     * @return Optional containing the loan if found, otherwise empty
     */
    Optional<Loan> findById(String id);

    /**
     * Returns all loans for a specific user.
     *
     * @param userId the id of the user
     * @return list of loans belonging to that user
     */
    List<Loan> findByUserId(String userId);

    /**
     * Returns all stored loans.
     *
     * @return list of all loans
     */
    List<Loan> findAll();

    /**
     * Deletes the given loan from the repository.
     *
     * @param loan the loan to remove
     */
    void delete(Loan loan);
}

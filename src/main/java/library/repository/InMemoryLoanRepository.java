package library.repository;

import library.domain.Loan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simple in-memory implementation of the LoanRepository.
 * Stores all loans in a list (not persistent).
 */
public class InMemoryLoanRepository implements LoanRepository {

    private final List<Loan> loans = new ArrayList<>();

    /**
     * Saves a new loan to the repository.
     *
     * @param loan the loan to store
     */
    @Override
    public void save(Loan loan) { 
        loans.add(loan); 
    }

    /**
     * Finds a loan by its id.
     *
     * @param id the loan id
     * @return Optional containing the loan if found, otherwise empty
     */
    @Override
    public Optional<Loan> findById(String id) { 
        return loans.stream()
                    .filter(l -> l.getId().equals(id))
                    .findFirst(); 
    }

    /**
     * Returns all loans that belong to a specific user.
     *
     * @param userId the user's id
     * @return list of the user's loans
     */
    @Override
    public List<Loan> findByUserId(String userId) { 
        return loans.stream()
                    .filter(l -> l.getUserId().equals(userId))
                    .collect(Collectors.toList()); 
    }

    /**
     * Returns a list of all loans.
     *
     * @return list of all loans
     */
    @Override
    public List<Loan> findAll() { 
        return new ArrayList<>(loans); 
    }

    /**
     * Removes a loan from the repository.
     *
     * @param loan the loan to delete
     */
    @Override
    public void delete(Loan loan) { 
        loans.remove(loan); 
    }
}

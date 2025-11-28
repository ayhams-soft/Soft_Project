package library.repository;

import library.domain.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    void save(Loan loan);
    Optional<Loan> findById(String id);
    List<Loan> findByUserId(String userId);
    List<Loan> findAll();
    void delete(Loan loan);
}

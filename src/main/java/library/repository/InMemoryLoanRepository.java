package library.repository;

import library.domain.Loan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory loan repository.
 */
public class InMemoryLoanRepository implements LoanRepository {
    private final List<Loan> loans = new ArrayList<>();

    @Override
    public void save(Loan loan) { loans.add(loan); }

    @Override
    public Optional<Loan> findById(String id) { return loans.stream().filter(l -> l.getId().equals(id)).findFirst(); }

    @Override
    public List<Loan> findByUserId(String userId) { return loans.stream().filter(l -> l.getUserId().equals(userId)).collect(Collectors.toList()); }

    @Override
    public List<Loan> findAll() { return new ArrayList<>(loans); }

    @Override
    public void delete(Loan loan) { loans.remove(loan); }
}

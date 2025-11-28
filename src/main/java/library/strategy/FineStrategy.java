package library.strategy;

/**
 * Fine calculation strategy.
 */
public interface FineStrategy {
    /**
     * Calculate fine for given overdue days (int days) returning integer NIS amount.
     */
    int calculateFine(int overdueDays);
}

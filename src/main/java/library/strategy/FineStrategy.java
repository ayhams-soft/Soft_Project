package library.strategy;

/**
 * General strategy interface for calculating fines.
 * 
 * Implementations (such as book or CD strategies) define
 * how the fine should be computed based on overdue days.
 */
public interface FineStrategy {

    /**
     * Calculates the fine for a given number of overdue days.
     *
     * @param overdueDays number of days an item is overdue
     * @return calculated fine in NIS (0 if not overdue)
     */
    int calculateFine(int overdueDays);
}

package library.strategy;

/**
 * Fine calculation strategy for books.
 * 
 * Applies a fixed rate of 10 NIS per overdue day.
 */
public class BookFineStrategy implements FineStrategy {

    private static final int RATE = 10;

    /**
     * Calculates the fine based on the number of overdue days.
     *
     * @param overdueDays number of days the item is overdue
     * @return fine amount (0 if no delay)
     */
    @Override
    public int calculateFine(int overdueDays) {
        if (overdueDays <= 0) return 0;
        return RATE * overdueDays;
    }
}

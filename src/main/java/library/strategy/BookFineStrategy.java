package library.strategy;

/**
 * Book fine: 10 NIS per overdue day.
 */
public class BookFineStrategy implements FineStrategy {
    private static final int RATE = 10;

    @Override
    public int calculateFine(int overdueDays) {
        if (overdueDays <= 0) return 0;
        return RATE * overdueDays;
    }
}

package library.strategy;

/**
 * CD fine: 20 NIS per overdue day.
 */
public class CDFineStrategy implements FineStrategy {
    private static final int RATE = 20;

    @Override
    public int calculateFine(int overdueDays) {
        if (overdueDays <= 0) return 0;
        return RATE * overdueDays;
    }
}

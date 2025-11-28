package library.strategy;

/**
 * Fine calculation strategy for CDs.
 * 
 * CDs have a fixed fine rate of 20 NIS per overdue day.
 */
public class CDFineStrategy implements FineStrategy {

    private static final int RATE = 20;

    /**
     * Calculates the fine based on the number of overdue days.
     *
     * @param overdueDays number of overdue days
     * @return total fine (0 if no delay)
     */
    @Override
    public int calculateFine(int overdueDays) {
        if (overdueDays <= 0) return 0;
        return RATE * overdueDays;
    }
}

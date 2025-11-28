package library.dto;

import java.util.Map;

/**
 * Overdue report mapping userId to (overdueCount, totalFine)
 */
public class OverdueReport {
    private final Map<String, Integer> overdueCounts;
    private final Map<String, Integer> fineSums;

    public OverdueReport(Map<String, Integer> overdueCounts, Map<String, Integer> fineSums) {
        this.overdueCounts = overdueCounts;
        this.fineSums = fineSums;
    }

    public Map<String, Integer> getOverdueCounts() {
        return overdueCounts;
    }

    public Map<String, Integer> getFineSums() {
        return fineSums;
    }
}

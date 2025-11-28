package library.dto;

import java.util.Map;

/**
 * Holds overdue information grouped by user.
 * Used inside ReminderService when building the overdue report.
 *
 * - overdueCounts : number of overdue items per user
 * - fineTotals    : total fine amount per user
 */
public class OverdueReport {

    // userId -> number of overdue items
    private final Map<String, Integer> overdueCounts;

    // userId -> total fine amount
    private final Map<String, Integer> fineTotals;

    /**
     * Creates a new OverdueReport.
     *
     * @param overdueCounts map of userId to overdue item count
     * @param fineTotals    map of userId to total fines
     */
    public OverdueReport(Map<String, Integer> overdueCounts, Map<String, Integer> fineTotals) {
        this.overdueCounts = overdueCounts;
        this.fineTotals = fineTotals;
    }

    /** @return map containing overdue item counts per user */
    public Map<String, Integer> getOverdueCounts() {
        return overdueCounts;
    }

    /** @return map containing total fines per user */
    public Map<String, Integer> getFineTotals() {
        return fineTotals;
    }
}

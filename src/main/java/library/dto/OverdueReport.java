package library.dto;

import java.util.Map;

/**
 * DTO representing aggregated overdue information per user:
 *
 * - overdueCounts : عدد العناصر المتأخرة (كتب + CDs) لكل مستخدم
 * - fineTotals    : مجموع الغرامات لكل مستخدم
 *
 * يستعمل في buildReport داخل ReminderService.
 */
public class OverdueReport {

    // userId -> عدد العناصر المتأخرة
    private final Map<String, Integer> overdueCounts;

    // userId -> مجموع الغرامات
    private final Map<String, Integer> fineTotals;

    public OverdueReport(Map<String, Integer> overdueCounts, Map<String, Integer> fineTotals) {
        this.overdueCounts = overdueCounts;
        this.fineTotals = fineTotals;
    }

    /** إرجاع خريطة عدد العناصر المتأخرة لكل مستخدم */
    public Map<String, Integer> getOverdueCounts() {
        return overdueCounts;
    }

    /** إرجاع خريطة مجموع الغرامات لكل مستخدم */
    public Map<String, Integer> getFineTotals() {
        return fineTotals;
    }
}

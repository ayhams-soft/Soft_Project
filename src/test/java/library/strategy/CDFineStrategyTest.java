package library.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CDFineStrategy}.
 * <p>
 * These tests make sure the CD fine calculation works correctly
 * for different overdue day values.
 */
class CDFineStrategyTest {

    /**
     * Checks that positive overdue days return
     * the daily CD rate multiplied by the number of days.
     */
    @Test
    void calculateFine_positiveDays_returnsRateTimesDays() {
        CDFineStrategy s = new CDFineStrategy();

        int fine = s.calculateFine(3); // 3 أيام تأخير
        assertEquals(60, fine);        // 3 × 20
    }

    /**
     * Checks that one overdue day gives a fine of 20.
     */
    @Test
    void calculateFine_oneDay_returns20() {
        CDFineStrategy s = new CDFineStrategy();

        int fine = s.calculateFine(1); // يوم واحد
        assertEquals(20, fine);
    }

    /**
     * Checks that zero overdue days return zero fine.
     */
    @Test
    void calculateFine_zeroDays_returnsZero() {
        CDFineStrategy s = new CDFineStrategy();

        int fine = s.calculateFine(0);
        assertEquals(0, fine);
    }

    /**
     * Checks that negative overdue days are treated as zero,
     * so the result should be 0.
     */
    @Test
    void calculateFine_negativeDays_returnsZero() {
        CDFineStrategy s = new CDFineStrategy();

        int fine = s.calculateFine(-5);
        assertEquals(0, fine);
    }
}

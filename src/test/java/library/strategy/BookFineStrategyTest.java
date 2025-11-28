package library.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BookFineStrategy}.
 * <p>
 * These tests check how the fine is calculated for books
 * based on the number of overdue days.
 */
class BookFineStrategyTest {

    /**
     * Checks that positive overdue days return
     * the daily rate multiplied by the number of days.
     */
    @Test
    void calculateFine_positiveDays_returnsRateTimesDays() {
        BookFineStrategy s = new BookFineStrategy();

        int fine = s.calculateFine(3); // 3 أيام تأخير
        assertEquals(30, fine);        // 3 × 10
    }

    /**
     * Checks that one overdue day returns a fine of 10.
     */
    @Test
    void calculateFine_oneDay_returns10() {
        BookFineStrategy s = new BookFineStrategy();

        int fine = s.calculateFine(1); // يوم واحد
        assertEquals(10, fine);
    }

    /**
     * Checks that zero overdue days return zero fine.
     */
    @Test
    void calculateFine_zeroDays_returnsZero() {
        BookFineStrategy s = new BookFineStrategy();

        int fine = s.calculateFine(0);
        assertEquals(0, fine);
    }

    /**
     * Checks that negative overdue days are treated as zero,
     * so the fine should be 0.
     */
    @Test
    void calculateFine_negativeDays_returnsZero() {
        BookFineStrategy s = new BookFineStrategy();

        int fine = s.calculateFine(-5);
        assertEquals(0, fine);
    }
}

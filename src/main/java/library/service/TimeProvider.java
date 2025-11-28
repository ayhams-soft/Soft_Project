package library.service;

import java.time.LocalDate;

/**
 * Simple abstraction for providing the current date.
 * 
 * This interface allows replacing the real system date with a
 * fixed or custom date during testing.
 */
public interface TimeProvider {

    /**
     * Returns the current date based on the implementation.
     *
     * @return today's date
     */
    LocalDate today();
}

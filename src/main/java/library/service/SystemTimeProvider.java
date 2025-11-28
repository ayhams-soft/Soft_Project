package library.service;

import java.time.LocalDate;

/**
 * Default implementation of {@link TimeProvider} that simply returns
 * the system's current date.
 *
 * Useful for production usage. During tests, a custom or fixed-time
 * provider can be used instead.
 */
public class SystemTimeProvider implements TimeProvider {

    /**
     * Returns today's date based on the system clock.
     *
     * @return current system date
     */
    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}

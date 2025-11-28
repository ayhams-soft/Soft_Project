package library.service;

import java.time.LocalDate;

/**
 * System time provider.
 */
public class SystemTimeProvider implements TimeProvider {
    @Override
    public LocalDate today() { return LocalDate.now(); }
}

package library.service;

import java.time.LocalDate;

/**
 * Abstraction of current date for testability.
 */
public interface TimeProvider {
    LocalDate today();
}

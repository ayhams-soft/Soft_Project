package library.exception;

/**
 * Exception thrown when a business rule is violated.
 * Used to signal invalid operations based on library rules.
 */
public class BusinessRuleException extends RuntimeException {

    /**
     * Creates a new BusinessRuleException with a message.
     *
     * @param msg the error message
     */
    public BusinessRuleException(String msg) {
        super(msg);
    }
}

package library.exception;

/**
 * Business rule violation.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String msg) { super(msg); }
}

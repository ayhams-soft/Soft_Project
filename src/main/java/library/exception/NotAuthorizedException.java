package library.exception;

/**
 * Thrown when action requires admin privileges.
 */
public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException(String msg) { super(msg); }
}

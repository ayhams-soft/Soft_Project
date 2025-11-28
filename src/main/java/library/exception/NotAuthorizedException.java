package library.exception;

/**
 * Exception thrown when an action requires admin privileges.
 */
public class NotAuthorizedException extends RuntimeException {

    /**
     * Creates a new NotAuthorizedException.
     *
     * @param msg the error message
     */
    public NotAuthorizedException(String msg) {
        super(msg);
    }
}

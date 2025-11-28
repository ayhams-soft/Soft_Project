package library.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new ResourceNotFoundException.
     *
     * @param msg the error message
     */
    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}

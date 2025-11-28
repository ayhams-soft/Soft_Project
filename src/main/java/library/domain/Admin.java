package library.domain;

/**
 * Simple class representing an admin in the system.
 * Each admin gets an auto-generated id, a username, and a stored password.
 */
public class Admin {

    private static long SEQ = 1;
    private final String id;
    private final String username;
    private final String passwordHash;

    /**
     * Creates a new admin.
     *
     * @param username the admin username
     * @param password the password (stored as-is for demo purposes)
     */
    public Admin(String username, String password) {
        this.id = "A" + (SEQ++);
        this.username = username;
        this.passwordHash = password;
    }

    /** @return admin id */
    public String getId() { return id; }

    /** @return username */
    public String getUsername() { return username; }

    /** @return stored password value */
    public String getPasswordHash() { return passwordHash; }
}

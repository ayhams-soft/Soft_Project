package library.domain;

/**
 * Represents a regular user in the library system.
 * A user has an auto-generated id, a name, an email, and an outstanding fine amount.
 */
public class User {

    private static long SEQ = 1;
    private final String id;
    private final String name;
    private final String email;
    private int outstandingFine;

    /**
     * Creates a new user.
     *
     * @param name  the user's name
     * @param email the user's email address
     */
    public User(String name, String email) {
        this.id = "U" + (SEQ++);
        this.name = name;
        this.email = email;
        this.outstandingFine = 0;
    }

    /** @return user id */
    public String getId() { return id; }

    /** @return user name */
    public String getName() { return name; }

    /** @return user email */
    public String getEmail() { return email; }

    /** @return current outstanding fine amount */
    public int getOutstandingFine() { return outstandingFine; }

    /**
     * Adds a fine amount to the user.
     *
     * @param amount the fine to add
     */
    public void addFine(int amount) {
        this.outstandingFine += amount;
    }

    /**
     * Pays part or all of the user's outstanding fine.
     *
     * @param amount the amount paid
     */
    public void payFine(int amount) {
        if (amount <= 0) return;
        this.outstandingFine = Math.max(0, this.outstandingFine - amount);
    }
}

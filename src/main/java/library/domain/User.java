package library.domain;

/**
 * User entity.
 */
public class User {
    private static long SEQ = 1;
    private final String id;
    private final String name;
    private final String email;
    private int outstandingFine; 

    public User(String name, String email) {
        this.id = "U" + (SEQ++);
        this.name = name;
        this.email = email;
        this.outstandingFine = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getOutstandingFine() { return outstandingFine; }

    public void addFine(int amount) {
        this.outstandingFine += amount;
    }

    public void payFine(int amount) {
        if (amount <= 0) return;
        this.outstandingFine = Math.max(0, this.outstandingFine - amount);
    }
}

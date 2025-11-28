package library.domain;


public class Admin {
    private static long SEQ = 1;
    private final String id;
    private final String username;
    private final String passwordHash;

    public Admin(String username, String password) {
        this.id = "A" + (SEQ++);
        this.username = username;
        this.passwordHash = password; // simplistic hash for demo
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}

package library;
//import java.util.ArrayList;
//import java.util.List;

public class User {

    private int id;
    private String username;
    private String passwordHash;
    private String email;
   // private List<Loan> loans;
    //private double unpaidFines;

    public User(int id, String username, String passwordHash, String email) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
       // this.loans = new ArrayList<>();
        //this.unpaidFines = 0.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

   // public List<Loan> getLoans() {
     //   return loans;
   // }

   // public void addLoan(Loan loan) {
   //     this.loans.add(loan);
   // }

   // public double getUnpaidFines() {
     //   return unpaidFines;
    //}

    //public void addFine(double amount) {
      //  this.unpaidFines += amount;
    //}

    //public void payFine(double amount) {
      //  this.unpaidFines -= amount;
     //   if (this.unpaidFines < 0) this.unpaidFines = 0;
    //}
}

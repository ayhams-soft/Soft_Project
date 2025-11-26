package library;

import java.util.*; 

public class Main {
	private static final Login LOGIN = new Login();

    private static final Scanner SCANNER = new Scanner(System.in);
    
    
    
    public static void main(String[] args) {
      
        startApplicationLoop();
        
    }

    private static void startApplicationLoop() {
        while (true) {
            clearScreen();
            println("====== Login ======");
            println("Select role:");
            println("1) Admin");
            println("2) User");
            

            String sel = readLine("\n> ");
            switch (sel) {
                case "1":
                    adminLoginFlow();
                    break;
                case "2":
                    userLoginFlow();
                    break;
                
                default:
                    println("Invalid selection. Press Enter to continue...");
                    waitEnter();
            }
        }
    }

    

    private static void adminLoginFlow() {
        clearScreen();
        println("--- Admin Login ---");
        
        String idText = readLine("ID: ");      
        String password = readLine("Password: ");

        boolean ok = LOGIN.authenticate(idText, password, "ADMIN");

        if (ok) {
            println("Login successful. Welcome, Admin!");
            waitEnter();

           
            adminMenu(null);
        } else {
            println("Login failed: Invalid ID or password.");
            waitEnter();
        }
    }

    private static void userLoginFlow() {
        clearScreen();
        println("--- User Login ---");

        String idText = readLine("ID: ");
        String password = readLine("Password: ");

        boolean ok = LOGIN.authenticate(idText, password, "USER");

        if (ok) {
            println("Login successful. Welcome, User!");
            waitEnter();


            userMenu(null);
        } else {
            println("Login failed: Invalid ID or password.");
            waitEnter();
        }
    }


    

    private static void adminMenu(Admin admin) {
        while (true) {
            clearScreen();
            println("====== Admin Menu ======");
            println("1) Add Book");
            println("2) Search Book");
            println("3) Send Reminders");
            println("4) Unregister User");
            println("5) Logout");

            String sel = readLine("\n> ");
            switch (sel) {
                case "1":

                    clearScreen();
                    println("--- Add Book ---");
                    println("Choose option:");
                    println("1) Add NEW book");
                    println("2) Add MORE copies to existing book");
                    String choice = readLine("\n> ");

                    if ("1".equals(choice)) {
                        // Add NEW book
                        clearScreen();
                        println("--- Add NEW Book ---");
                        String isbn = readLine("ISBN: ").trim();
                        String title = readLine("Title: ").trim();
                        String author = readLine("Author: ").trim();
                        String copiesText = readLine("Available copies (number): ").trim();

                        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || copiesText.isEmpty()) {
                            println("All fields are required. Operation cancelled.");
                            waitEnter();
                            break;
                        }

                        int copies = 0;
                        try {
                            copies = Integer.parseInt(copiesText);
                            if (copies < 0) throw new NumberFormatException();
                        } catch (NumberFormatException ex) {
                            println("Invalid number for copies. Operation cancelled.");
                            waitEnter();
                            break;
                        }

                        boolean added;
                        try {
                            added = admin.addBook(title, author, isbn, copies);
                        } catch (Exception ex) {
                            added = false;
                        }

                        if (added) {
                            println("Book added successfully.");
                        } else {
                            println("Failed to add book. ISBN may already exist or an IO error occurred.");
                            println("If the ISBN exists and you only want to increase copies, choose option 2 next time.");
                        }
                        waitEnter();

                    } else if ("2".equals(choice)) {

                        clearScreen();
                        println("--- Add Copies to Existing Book ---");
                        String isbn = readLine("ISBN of existing book: ").trim();
                        String extraText = readLine("Number of additional copies to add: ").trim();

                        if (isbn.isEmpty() || extraText.isEmpty()) {
                            println("ISBN and number of copies are required. Operation cancelled.");
                            waitEnter();
                            break;
                        }

                        int extra = 0;
                        try {
                            extra = Integer.parseInt(extraText);
                            if (extra <= 0) throw new NumberFormatException();
                        } catch (NumberFormatException ex) {
                            println("Invalid number for extra copies (must be > 0). Operation cancelled.");
                            waitEnter();
                            break;
                        }

                        boolean updated;
                        try {
                            updated = admin.addCopies(isbn, extra);
                        } catch (Exception ex) {
                            updated = false;
                        }

                        if (updated) {
                            println("Copies updated successfully.");
                        } else {
                            println("Failed to update copies. ISBN not found or an IO error occurred.");
                            println("If the ISBN is new, use 'Add NEW book' to create a new record.");
                        }
                        waitEnter();

                    } else {
                        println("Invalid selection. Returning to Admin Menu.");
                        waitEnter();
                    }
                    break;

                case "2":
                    notImplemented("Search Book");
                    break;
                case "3":
                    notImplemented("Send Reminders");
                    break;
                case "4":
                    notImplemented("Unregister User");
                    break;
                case "5":
                    println("Logged out.");
                    waitEnter();
                    return;
                default:
                    println("Invalid selection. Press Enter to continue...");
                    waitEnter();
            }
        }
    }

    private static void userMenu(User user) {
        while (true) {
            clearScreen();
            println("====== User Menu ======");
            println("1) Search Book");
            println("2) Borrow Book");
            println("3) Return Book");
            println("4) View My Loans");
            println("5) Pay Fine");
            println("6) Logout");
            
            String sel = readLine("\n> ");
            switch (sel) {
                case "1":
                    notImplemented("Search Book");
                    break;
                case "2":
                    notImplemented("Borrow Book");
                    break;
                case "3":
                    notImplemented("Return Book");
                    break;
                case "4":
                    notImplemented("View My Loans");
                    break;
                case "5":
                    notImplemented("Pay Fine");
                    break;
                case "6":
                    println("Logged out.");
                    waitEnter();
                    return;
                
                default:
                    println("Invalid selection. Press Enter to continue...");
                    waitEnter();
            }
        }
    }

    

    private static void notImplemented(String feature) {
        println("[NOT IMPLEMENTED] " + feature + " — This is a frontend placeholder.");
        waitEnter();
    }

   

    private static void waitEnter() {
        println("\nPress Enter to continue...");
        SCANNER.nextLine();
    }

    private static void println(String s) {
        System.out.println(s);
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    private static void clearScreen() {
        // Simple spacer rather than terminal control codes for portability
        System.out.println("\n\n\n-------------------------------\n");
    }

    
}


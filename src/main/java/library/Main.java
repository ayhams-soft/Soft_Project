package library;

import library.domain.User;
import library.domain.media.Media;
import library.exception.BusinessRuleException;
import library.exception.NotAuthorizedException;
import library.exception.ResourceNotFoundException;
import library.service.AuthService;
import library.service.LibraryService;


import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AppConfig cfg = new AppConfig();
        AuthService auth = cfg.authService();
        LibraryService lib = cfg.libraryService();

        
        User demoUser = lib.registerUser("demo", "demo@example.com");

        Scanner sc = new Scanner(System.in);

        boolean running = true;
        System.out.println("Welcome to Library Console");
        while (running) {
            System.out.println();
            System.out.println("Select role:");
            System.out.println("1) Admin");
            System.out.println("2) Customer");
            System.out.println("0) Exit");
            System.out.print("Choice: ");
            String roleChoice = sc.nextLine().trim();
            switch (roleChoice) {
                case "1":
                    adminMenu(sc, auth, lib, cfg);
                    break;
                case "2":
                    customerMenu(sc, lib, demoUser);
                    break;
                case "0":
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                default:
                    System.out.println("Unknown selection.");
            }
        }

        sc.close();
    }

    private static void adminMenu(Scanner sc, AuthService auth, LibraryService lib, AppConfig cfg) {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Admin Menu ===");
            System.out.println("1) Login");
            System.out.println("2) Logout");
            System.out.println("3) Add Book");
            System.out.println("4) Add CD");
            System.out.println("5) Search Media");
            System.out.println("6) Send Reminders");
            System.out.println("7) Unregister User");
            System.out.println("8) Show All Users");
            System.out.println("9) Show All Media");
            System.out.println("10) Show Borrowed Media (with overdue info)"); // <-- الجديد
            System.out.println("0) Back to Role Selection");
            System.out.print("Select: ");
            String opt = sc.nextLine().trim();
            try {
                switch (opt) {
                    case "1":
                        System.out.print("username: ");
                        String user = sc.nextLine().trim();
                        System.out.print("password: ");
                        String pass = sc.nextLine().trim();
                        boolean ok = auth.login(user, pass);
                        System.out.println(ok ? "Login success" : "Login failed");
                        break;
                    case "2":
                        auth.logout();
                        System.out.println("Logged out");
                        break;
                    case "3":
                        try {
                            auth.requireAdmin();
                            System.out.print("Title: ");
                            String t = sc.nextLine().trim();
                            System.out.print("Author: ");
                            String a = sc.nextLine().trim();
                            System.out.print("ISBN: ");
                            String isbn = sc.nextLine().trim();
                            lib.addBook(t, a, isbn);
                            System.out.println("Book added.");
                        } catch (Exception e) {
                            System.out.println("Error adding book: " + e.getMessage());
                        }
                        break;
                    case "4":
                        try {
                            auth.requireAdmin();
                            System.out.print("Title: ");
                            String t2 = sc.nextLine().trim();
                            System.out.print("Artist: ");
                            String artist = sc.nextLine().trim();
                            lib.addCD(t2, artist);
                            System.out.println("CD added.");
                        } catch (Exception e) {
                            System.out.println("Error adding CD: " + e.getMessage());
                        }
                        break;
                    case "5":
                        System.out.print("Search query: ");
                        String q = sc.nextLine().trim();
                        java.util.List<library.domain.media.Media> results = lib.search(q);
                        printMediaList(results);
                        break;
                    case "6":
                        try {
                            auth.requireAdmin();

                            // إرسال التذكيرات عبر ReminderService
                            lib.getReminderService().sendReminders(cfg.loanRepository(), cfg.userRepository(), cfg.mediaRepository());
                            System.out.println("Reminders processed.");

                            // --- طباعة سجل FakeEmailClient (إن وُجد) للاختبار ---
                            try {
                                // AppConfig يحتوي على getter fakeEmailClient() حسب التعديل السابق
                                if (cfg.fakeEmailClient() != null) {
                                    System.out.println("FakeEmailClient sent messages:");
                                    cfg.fakeEmailClient().getSent().forEach(s ->
                                        System.out.println(" - to=" + s.to + " | subject=" + s.subject + " | body=" + s.body)
                                    );
                                    System.out.println("Total fake emails sent: " + cfg.fakeEmailClient().getSent().size());
                                }
                            } catch (NoSuchMethodError | NullPointerException ex) {
                                // إذا لم يكن AppConfig يحتوي getter fakeEmailClient() — تجاهل الطباعة بهدوء
                            } catch (Exception ex) {
                                System.out.println("Warning: could not inspect FakeEmailClient: " + ex.getMessage());
                            }

                        } catch (Exception e) {
                            System.out.println("Error sending reminders: " + e.getMessage());
                        }
                        break;


                    case "7":
                        try {
                            System.out.print("User id to unregister: ");
                            String uid = sc.nextLine().trim();
                            // Let the service enforce admin requirement and business rules
                            lib.unregisterUser(null, uid);
                            System.out.println("User unregistered.");
                        } catch (Exception e) {
                            System.out.println("Cannot unregister: " + e.getMessage());
                        }
                        break;

                    case "8":
                        cfg.userRepository().findAll().forEach(u -> System.out.println(" - " + u.getId() + " | " + u.getName() + " | " + u.getEmail() + " | fine=" + u.getOutstandingFine()));
                        break;
                    case "9":
                        cfg.mediaRepository().findAll().forEach(m -> System.out.println(" - " + m.getId() + " | " + m.getTitle() + " | available=" + m.isAvailable() + " | type=" + m.getMediaType()));
                        break;
                    case "10":
                        try {
                            auth.requireAdmin();
                            java.util.List<String> report = lib.getBorrowedMediaReport();
                            if (report.isEmpty()) {
                                System.out.println("No borrowed media currently.");
                            } else {
                                System.out.println("Borrowed Media Report:");
                                report.forEach(line -> System.out.println(" - " + line));
                            }
                        } catch (Exception e) {
                            System.out.println("Error retrieving borrowed media: " + e.getMessage());
                        }
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Unknown option.");
                }
            } catch (Exception ex) {
                System.out.println("Unhandled admin error: " + ex.getMessage());
            }
        }
    }



    private static void customerMenu(Scanner sc, LibraryService lib, User demoUser) {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Customer Menu ===");
            System.out.println("1) Search Media");
            System.out.println("2) Borrow Media");
            System.out.println("3) Return Media (by loan id)");
            System.out.println("4) Pay Fine");
            System.out.println("5) Show My Loans");
            System.out.println("6) Show My Info");
            System.out.println("0) Back to Role Selection");
            System.out.print("Select: ");
            String opt = sc.nextLine().trim();
            try {
                switch (opt) {
                case "1":
                    // Ask for search type
                    System.out.println("Search by:");
                    System.out.println("  a) Title");
                    System.out.println("  b) Author");
                    System.out.println("  c) ISBN");
                    System.out.print("Select (a/b/c): ");
                    String stype = sc.nextLine().trim().toLowerCase();
                    if ("a".equals(stype)) {
                        System.out.print("Enter title (partial allowed): ");
                        String titleQ = sc.nextLine().trim();
                        List<Media> titleResults = lib.searchByTitle(titleQ);
                        printMediaList(titleResults);
                    } else if ("b".equals(stype)) {
                        System.out.print("Enter author name: ");
                        String authorQ = sc.nextLine().trim();
                        List<Media> authorResults = lib.searchByAuthor(authorQ);
                        printMediaList(authorResults);
                    } else if ("c".equals(stype)) {
                        System.out.print("Enter ISBN (exact): ");
                        String isbnQ = sc.nextLine().trim();
                        List<Media> isbnResults = lib.searchByIsbn(isbnQ);
                        printMediaList(isbnResults);
                    } else {
                        System.out.println("Unknown search type.");
                    }
                    break;
                    case "2":
                        System.out.print("Your user id (demo user id: " + demoUser.getId() + "): ");
                        String uid = sc.nextLine().trim();
                        System.out.print("Media id: ");
                        String mid = sc.nextLine().trim();
                        try {
                            lib.borrow(uid, mid);
                            System.out.println("Borrowed successfully.");
                        } catch (BusinessRuleException bre) {
                            System.out.println("Cannot borrow: " + bre.getMessage());
                        } catch (ResourceNotFoundException rnfe) {
                            System.out.println("Resource not found: " + rnfe.getMessage());
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;
                    case "3":
                        System.out.print("Loan id: ");
                        String lid = sc.nextLine().trim();
                        try {
                            lib.returnMedia(lid);
                            System.out.println("Returned. Any overdue fines applied to your account.");
                        } catch (ResourceNotFoundException rnfe) {
                            System.out.println("Loan not found.");
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;
                    case "4":
                        System.out.print("Your user id: ");
                        String pu = sc.nextLine().trim();
                        System.out.print("Amount (integer NIS): ");
                        String amtS = sc.nextLine().trim();
                        try {
                            int amount = Integer.parseInt(amtS);
                            lib.payFine(pu, amount);
                            System.out.println("Payment applied.");
                        } catch (NumberFormatException nfe) {
                            System.out.println("Invalid amount.");
                        } catch (ResourceNotFoundException rnfe) {
                            System.out.println("User not found.");
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;
                    case "5":
                        System.out.print("Your user id (demo user id: " + demoUser.getId() + "): ");
                        String userId = sc.nextLine().trim();
                        List<?> loans = lib.findLoansByUser(userId);
                        System.out.println("Loans count: " + loans.size());
                        loans.forEach(l -> System.out.println(" - " + l));
                        break;
                    case "6":
                        System.out.print("Your user id (demo user id: " + demoUser.getId() + "): ");
                        String uidInfo = sc.nextLine().trim();
                        lib.findLoansByUser(uidInfo); // just to keep examples light
                        lib.getReminderService(); // no-op; just demonstrating calls kept out of business logic
                        // fetch user from AppConfig's repo would be cleaner; demo shows minimal customer features
                        System.out.println("Info: demo user id is " + demoUser.getId() + " email=" + demoUser.getEmail() + " outstandingFine=" + demoUser.getOutstandingFine());
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Unknown option.");
                }
            } catch (Exception ex) {
                System.out.println("Unhandled customer error: " + ex.getMessage());
            }
        }
    }

    private static void printMediaList(List<Media> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("No results.");
            return;
        }
        System.out.println("Results:");
        for (Media m : results) {
            System.out.println(" - " + m.getId() + " | " + m.getTitle() + " | available=" + m.isAvailable() + " | type=" + m.getMediaType());
        }
    }
}

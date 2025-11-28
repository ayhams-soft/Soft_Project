package library1;

import library.Main;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for {@link Main} class.
 * <p>
 * These tests simulate user input to make sure the console
 * menus in the program run without crashing. The goal here
 * is not to test business logic, but to check that the
 * main loop and menu navigation work correctly.
 */
class MainTest {

    /**
     * Checks that the program exits immediately when the user
     * selects "0" from the first role menu. This test uses
     * a small simulated input stream to imitate the user.
     */
    @Test
    void main_exits_immediately_when_user_chooses_zero() {
        // المستخدم من أول منيو Role بختار 0 → خروج مباشر
        String simulatedInput = "0\n";

        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

            assertDoesNotThrow(() -> Main.main(new String[]{}));
        } finally {
            System.setIn(originalIn); // رجّع System.in
        }
    }

    /**
     * Simulates a short customer flow:
     * <ul>
     *   <li>Enter customer menu</li>
     *   <li>Choose Back</li>
     *   <li>Exit from the main role menu</li>
     * </ul>
     * This test just confirms that no exception is thrown.
     */
    @Test
    void main_goes_to_customer_menu_then_back_and_exit() {
        String simulatedInput = String.join("\n",
                "2",  // Role: Customer
                "0",  // Customer: Back
                "0"   // Role: Exit
        ) + "\n";

        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

            assertDoesNotThrow(() -> Main.main(new String[]{}));
        } finally {
            System.setIn(originalIn);
        }
    }

    /**
     * A long end-to-end simulation that goes through most of the
     * admin and customer menu options. The goal is to cover as much
     * of the menu code as possible to improve test coverage.
     * <p>
     * The test includes:
     * <ul>
     *   <li>Wrong inputs in menus</li>
     *   <li>Admin login (wrong then correct)</li>
     *   <li>Adding media</li>
     *   <li>Searching</li>
     *   <li>Sending reminders</li>
     *   <li>Borrow/Return/Pay fine flows for customers</li>
     *   <li>Going back to role menu</li>
     *   <li>Final exit</li>
     * </ul>
     */
    @Test
    void main_full_admin_and_customer_flow_covers_all_menu_options() {

        String simulatedInput = String.join("\n",
                // ---- Main Role Menu: اختيار غلط ثم Admin ----
                "x",          // Unknown selection في الـ Role menu
                "1",          // Role: Admin

                // ---- Admin Menu Flow ----
                // case "1" → Login (غلط)
                "1",          // Admin: Login
                "wrongUser",  // username
                "wrongPass",  // password

                // case "1" → Login (صح)
                "1",          // Admin: Login
                "admin",      // username
                "admin",      // password

                // Add Book
                "3",
                "Test Book",
                "Test Author",
                "ISBN-XYZ",

                // Add CD
                "4",
                "Test CD",
                "Some Artist",

                // Search Media
                "5",
                "",

                // Send Reminders
                "6",

                // Unregister User (ID غلط)
                "7",
                "unknown-user-id",

                // Show Users
                "8",

                // Show Media
                "9",

                // Borrowed report
                "10",

                // Wrong admin option
                "z",

                // Back
                "0",

                // ---- Customer ----
                "2",

                // Search by Title
                "1",
                "a",
                "some title",

                // Search by Author
                "1",
                "b",
                "some author",

                // Search by ISBN
                "1",
                "c",
                "some isbn",

                // Unknown search type
                "1",
                "x",

                // Borrow (wrong user/media)
                "2",
                "unknown-user",
                "unknown-media",

                // Return (wrong loan)
                "3",
                "unknown-loan-id",

                // Pay fine with invalid amount
                "4",
                "unknown-user",
                "abc",

                // Pay fine valid amount, invalid user
                "4",
                "unknown-user2",
                "10",

                // Show loans
                "5",
                "unknown-user",

                // Show info
                "6",
                "unknown-user",

                // Wrong option
                "z",

                // Back
                "0",

                // Exit
                "0"
        ) + "\n";

        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

            assertDoesNotThrow(() -> Main.main(new String[]{}));
        } finally {
            System.setIn(originalIn);
        }
    }
}

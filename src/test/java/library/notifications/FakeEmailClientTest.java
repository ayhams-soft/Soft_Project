package library.notifications;

import library.dto.EmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FakeEmailClient}.
 *
 * This test class verifies the following behaviors:
 *  - Emails sent through the client are stored correctly.
 *  - The returned list of sent emails is unmodifiable.
 *  - The clear() method removes all stored emails.
 */
class FakeEmailClientTest {

    private FakeEmailClient client;

    /**
     * Initializes a fresh FakeEmailClient before each test.
     */
    @BeforeEach
    void setUp() {
        client = new FakeEmailClient();
        client.clear();
    }

    /**
     * Ensures that calling send(...) records an email
     * with the correct recipient, subject, and body.
     */
    @Test
    void send_storesEmailCorrectly() {
        client.send("ayham@test.com", "Hello", "Message body");

        // getSent now returns List<EmailMessage>, not SentEmail
        List<EmailMessage> sent = client.getSent();

        assertEquals(1, sent.size());

        EmailMessage email = sent.get(0);

        assertEquals("ayham@test.com", email.getTo());
        assertEquals("Hello", email.getSubject());
        assertEquals("Message body", email.getBody());
    }

    /**
     * Verifies that getSent() returns an unmodifiable list.
     */
    @Test
    void getSent_returnsUnmodifiableList() {
        client.send("x@test.com", "S1", "B1");

        List<EmailMessage> sent = client.getSent();

        // still unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> sent.add(null));
    }

    /**
     * Ensures that clear() removes all stored emails.
     */
    @Test
    void clear_removesAllEmails() {
        client.send("a@test.com", "S1", "B1");
        client.send("b@test.com", "S2", "B2");

        client.clear();

        List<EmailMessage> sent = client.getSent();
        assertEquals(0, sent.size());
    }
}

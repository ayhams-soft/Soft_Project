package library.notifications;

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

        List<FakeEmailClient.SentEmail> sent = client.getSent();

        assertEquals(1, sent.size());

        FakeEmailClient.SentEmail email = sent.get(0);

        assertEquals("ayham@test.com", email.to);
        assertEquals("Hello", email.subject);
        assertEquals("Message body", email.body);
    }

    /**
     * Verifies that getSent() returns an unmodifiable list.
     */
    @Test
    void getSent_returnsUnmodifiableList() {
        client.send("x@test.com", "S1", "B1");

        List<FakeEmailClient.SentEmail> sent = client.getSent();

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

        List<FakeEmailClient.SentEmail> sent = client.getSent();
        assertEquals(0, sent.size());
    }
}

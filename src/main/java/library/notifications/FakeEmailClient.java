package library.notifications;

import library.dto.EmailMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fake implementation of EmailClient used for testing.
 * Instead of sending real emails, it stores all sent messages
 * so tests or admin tools can inspect them later.
 */
public class FakeEmailClient implements EmailClient {

    // List of sent email messages
    private final List<EmailMessage> sent = new ArrayList<>();

    @Override
    public void send(String to, String subject, String body) {
        sent.add(new EmailMessage(to, subject, body));
    }

    /**
     * Returns an unmodifiable list of all sent emails.
     * Useful for checking results in tests or via admin console.
     *
     * @return list of EmailMessage items
     */
    public List<EmailMessage> getSent() {
        return Collections.unmodifiableList(sent);
    }

    /**
     * Clears the stored email list.
     * Tests usually call this before each run.
     */
    public void clear() {
        sent.clear();
    }
}

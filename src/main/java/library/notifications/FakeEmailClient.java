package library.notifications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Fake implementation of EmailClient used for testing.
 * Instead of sending real emails, it stores all sent messages
 * so tests can inspect them later.
 */
public class FakeEmailClient implements EmailClient {

    /**
     * Simple record holding a sent email.
     */
    public static class SentEmail {
        public final String to;
        public final String subject;
        public final String body;

        public SentEmail(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        @Override
        public String toString() {
            return "SentEmail[to=" + to + ", subject=" + subject + ", body=" + body + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SentEmail)) return false;
            SentEmail s = (SentEmail) o;
            return Objects.equals(to, s.to)
                    && Objects.equals(subject, s.subject)
                    && Objects.equals(body, s.body);
        }
    }

    private final List<SentEmail> sent = new ArrayList<>();

    @Override
    public void send(String to, String subject, String body) {
        sent.add(new SentEmail(to, subject, body));
    }

    /**
     * Returns an unmodifiable list of all sent emails.
     * Useful for checking results in tests.
     *
     * @return list of SentEmail items
     */
    public List<SentEmail> getSent() {
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

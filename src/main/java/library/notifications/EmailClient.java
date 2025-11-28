package library.notifications;

/**
 * Simple interface for sending email messages.
 * A real implementation may use SMTP or another provider.
 * For testing purposes, FakeEmailClient is used.
 */
public interface EmailClient {

    /**
     * Sends a simple text email.
     *
     * @param to      receiver email address
     * @param subject email subject
     * @param body    email body text
     */
    void send(String to, String subject, String body);
}

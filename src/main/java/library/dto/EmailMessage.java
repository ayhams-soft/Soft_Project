package library.dto;

/**
 * Simple DTO representing an email message.
 * Used by the notification system to send or record emails.
 */
public class EmailMessage {

    private final String to;
    private final String subject;
    private final String body;

    /**
     * Creates a new email message.
     *
     * @param to      the receiver's email address
     * @param subject the email subject line
     * @param body    the content of the email
     */
    public EmailMessage(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    /** @return the receiver email address */
    public String getTo() { return to; }

    /** @return the email subject */
    public String getSubject() { return subject; }

    /** @return the email body content */
    public String getBody() { return body; }

    @Override
    public String toString() {
        return "EmailMessage[to=" + to + ", subject=" + subject + "]";
    }
}

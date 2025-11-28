package library.notifications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Fake EmailClient لتسجيل الرسائل المرسلة أثناء الاختبارات أو في وضع اختبار (test mode).
 */
public class FakeEmailClient implements EmailClient {

    public static class SentEmail {
        public final String to;
        public final String subject;
        public final String body;
        public SentEmail(String to, String subject, String body) {
            this.to = to; this.subject = subject; this.body = body;
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
            return Objects.equals(to, s.to) && Objects.equals(subject, s.subject) && Objects.equals(body, s.body);
        }
    }

    private final List<SentEmail> sent = new ArrayList<>();

    @Override
    public void send(String to, String subject, String body) {
        sent.add(new SentEmail(to, subject, body));
    }

    /**
     * إرجاع نسخة غير قابلة للتعديل من الرسائل المسجَّلة
     */
    public List<SentEmail> getSent() {
        return Collections.unmodifiableList(sent);
    }

    /**
     * مسح السجل (مفيد قبل كل اختبار)
     */
    public void clear() {
        sent.clear();
    }
}

package library.notifications;

/**
 * واجهة بسيطة لإرسال الإيميلات.
 * تنفيذ حقيقي قد يكون عبر SMTP أو أي مزوّد آخر.
 * لتنفيذ الاختبارات نستخدم FakeEmailClient.
 */
public interface EmailClient {
    /**
     * أرسل رسالة نصية بسيطة.
     * @param to   عنوان المستلم (email)
     * @param subject موضوع الرسالة
     * @param body نص الرسالة
     */
    void send(String to, String subject, String body);
}

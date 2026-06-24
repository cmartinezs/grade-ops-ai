package cl.gradeops.ai.api.auth.application.port.out;

public interface EmailNotificationPort {
    void sendPasswordResetEmail(String toEmail, String firstName, String rawCode);
}

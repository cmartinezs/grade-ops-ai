package cl.gradeops.ai.api.auth.infrastructure.adapter.out.email;

import cl.gradeops.ai.api.auth.application.port.out.EmailNotificationPort;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.email.JavaMailEmailService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThymeleafEmailNotificationAdapter implements EmailNotificationPort {

    private final JavaMailEmailService javaMailEmailService;
    private final String webBaseUrl;

    @Override
    public void sendPasswordResetEmail(String toEmail, String firstName, String rawCode) {
        String resetUrl = webBaseUrl + "/reset-password?code=" + rawCode;
        javaMailEmailService.sendPasswordReset(toEmail, firstName, resetUrl);
    }
}

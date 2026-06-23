package cl.gradeops.ai.api.shared.infrastructure.adapter.out.email;

import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsEmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class JavaMailEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final GradeOpsEmailProperties emailProperties;

    public JavaMailEmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        GradeOpsEmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailProperties = emailProperties;
    }

    public void sendPasswordReset(String toEmail, String firstName, String resetLink) {
        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("expiresInMinutes", emailProperties.getResetPassword().getTtlMinutes());
        ctx.setVariable("appName", emailProperties.getFromName());
        ctx.setVariable("supportEmail", emailProperties.getSupportEmail());

        String html = templateEngine.process("email/password-reset", ctx);
        sendHtml(toEmail, emailProperties.getResetPassword().getSubject(), html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(emailProperties.getFromAddress(), emailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}

package cl.gradeops.ai.api.email;

import cl.gradeops.ai.api.shared.infrastructure.adapter.out.email.JavaMailEmailService;
import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsEmailProperties;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JavaMailEmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock TemplateEngine templateEngine;

    JavaMailEmailService emailService;
    GradeOpsEmailProperties emailProperties;

    @BeforeEach
    void setUp() {
        emailProperties = new GradeOpsEmailProperties();
        emailProperties.setFromAddress("noreply@gradeops.app");
        emailProperties.setFromName("GradeOps AI");
        emailProperties.setSupportEmail("soporte@gradeops.app");

        emailService = new JavaMailEmailService(mailSender, templateEngine, emailProperties);
    }

    @Test
    void sendPasswordReset_processesTemplateWithCorrectVariables() {
        MimeMessage mimeMsg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMsg);
        when(templateEngine.process(eq("email/password-reset"), any(IContext.class)))
            .thenReturn("<html>mock</html>");

        emailService.sendPasswordReset("teacher@school.cl", "Ana", "http://localhost:3000/reset-password?code=abc");

        ArgumentCaptor<IContext> ctxCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("email/password-reset"), ctxCaptor.capture());

        IContext ctx = ctxCaptor.getValue();
        assertThat(ctx.getVariable("firstName")).isEqualTo("Ana");
        assertThat(ctx.getVariable("resetLink")).isEqualTo("http://localhost:3000/reset-password?code=abc");
        assertThat(ctx.getVariable("expiresInMinutes")).isEqualTo(30);
        assertThat(ctx.getVariable("appName")).isEqualTo("GradeOps AI");
        assertThat(ctx.getVariable("supportEmail")).isEqualTo("soporte@gradeops.app");
    }

    @Test
    void sendPasswordReset_sendsEmailViaMailSender() {
        MimeMessage mimeMsg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMsg);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>mock</html>");

        emailService.sendPasswordReset("teacher@school.cl", "Ana", "http://localhost:3000/reset-password?code=abc");

        verify(mailSender).send(mimeMsg);
    }

    @Test
    void sendPasswordReset_wrapsMessagingExceptionAsRuntimeException() throws Exception {
        MimeMessage mimeMsg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMsg);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>mock</html>");
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() ->
            emailService.sendPasswordReset("teacher@school.cl", "Ana", "http://x.com/reset?code=abc"))
            .isInstanceOf(RuntimeException.class);
    }
}

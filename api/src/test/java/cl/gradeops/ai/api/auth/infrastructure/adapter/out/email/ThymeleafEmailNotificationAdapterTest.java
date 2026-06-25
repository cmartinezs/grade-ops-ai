package cl.gradeops.ai.api.auth.infrastructure.adapter.out.email;

import cl.gradeops.ai.api.shared.infrastructure.adapter.out.email.JavaMailEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ThymeleafEmailNotificationAdapterTest {

    @Mock JavaMailEmailService javaMailEmailService;

    @Test
    void sendPasswordResetEmail_buildsCorrectUrl() {
        ThymeleafEmailNotificationAdapter adapter =
            new ThymeleafEmailNotificationAdapter(javaMailEmailService, "https://app.gradeops.cl");

        adapter.sendPasswordResetEmail("t@test.com", "Ana", "abc-123");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(javaMailEmailService).sendPasswordReset(eq("t@test.com"), eq("Ana"), urlCaptor.capture());
        assertThat(urlCaptor.getValue()).isEqualTo("https://app.gradeops.cl/reset-password?code=abc-123");
    }

    @Test
    void sendPasswordResetEmail_uuidFormatRawCodeProducesValidUrl() {
        ThymeleafEmailNotificationAdapter adapter =
            new ThymeleafEmailNotificationAdapter(javaMailEmailService, "https://app.gradeops.cl");
        String uuidCode = "550e8400-e29b-41d4-a716-446655440000";

        adapter.sendPasswordResetEmail("t@test.com", "Ana", uuidCode);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(javaMailEmailService).sendPasswordReset(eq("t@test.com"), eq("Ana"), urlCaptor.capture());
        assertThat(urlCaptor.getValue()).contains(uuidCode);
        assertThat(urlCaptor.getValue()).isEqualTo("https://app.gradeops.cl/reset-password?code=550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void sendPasswordResetEmail_passesEmailAndNameUnchanged() {
        ThymeleafEmailNotificationAdapter adapter =
            new ThymeleafEmailNotificationAdapter(javaMailEmailService, "https://app.gradeops.cl");

        adapter.sendPasswordResetEmail("teacher@school.com", "Pedro", "code-123");

        verify(javaMailEmailService).sendPasswordReset(eq("teacher@school.com"), eq("Pedro"), eq("https://app.gradeops.cl/reset-password?code=code-123"));
    }
}

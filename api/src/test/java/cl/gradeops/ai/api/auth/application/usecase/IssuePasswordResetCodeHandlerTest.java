package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class IssuePasswordResetCodeHandlerTest {

    private final PasswordResetCodeRepositoryPort codeRepository = mock(PasswordResetCodeRepositoryPort.class);
    private final IssuePasswordResetCodeHandler handler = new IssuePasswordResetCodeHandler(codeRepository);

    @Test
    void shouldDeleteExistingAndSaveNewCodeWhenExecuted() {
        IssuePasswordResetCodeCommand command = IssuePasswordResetCodeCommand.builder()
                .teacherUid("teacher-1").ttlMinutes(30).provider(SignInProvider.EMAIL_PASSWORD).build();

        IssuePasswordResetCodeResult result = handler.execute(command);

        verify(codeRepository).deleteByTeacherUid("teacher-1");

        ArgumentCaptor<PasswordResetCode> captor = ArgumentCaptor.forClass(PasswordResetCode.class);
        verify(codeRepository).save(captor.capture());

        PasswordResetCode saved = captor.getValue();
        assertThat(saved.getTeacherUid()).isEqualTo("teacher-1");
        assertThat(saved.getRawCode().value()).isNotBlank();
        assertThat(result.rawCode()).isEqualTo(saved.getRawCode().value());
        assertThat(result.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void shouldCallDeleteBeforeSaveWhenExecuted() {
        IssuePasswordResetCodeCommand command = IssuePasswordResetCodeCommand.builder()
                .teacherUid("teacher-2").ttlMinutes(15).provider(SignInProvider.EMAIL_PASSWORD).build();

        handler.execute(command);

        var inOrder = inOrder(codeRepository);
        inOrder.verify(codeRepository).deleteByTeacherUid("teacher-2");
        inOrder.verify(codeRepository).save(any(PasswordResetCode.class));
    }

    @Test
    void shouldThrowWhenProviderIsNotEmailPassword() {
        IssuePasswordResetCodeCommand command = IssuePasswordResetCodeCommand.builder()
                .teacherUid("teacher-3").ttlMinutes(30).provider(SignInProvider.GOOGLE).build();

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(codeRepository);
    }
}

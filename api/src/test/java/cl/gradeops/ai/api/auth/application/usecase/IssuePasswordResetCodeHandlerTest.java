package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class IssuePasswordResetCodeHandlerTest {

    private final PasswordResetCodeRepositoryPort codeRepository = mock(PasswordResetCodeRepositoryPort.class);
    private final IssuePasswordResetCodeHandler handler = new IssuePasswordResetCodeHandler(codeRepository);

    @Test
    void happy_path_deletes_existing_and_saves_new_code() {
        IssuePasswordResetCodeCommand command = IssuePasswordResetCodeCommand.builder()
                .teacherUid("teacher-1").ttlMinutes(30).build();

        IssuePasswordResetCodeResult result = handler.execute(command);

        verify(codeRepository).deleteByTeacherUid("teacher-1");

        ArgumentCaptor<PasswordResetCode> captor = ArgumentCaptor.forClass(PasswordResetCode.class);
        verify(codeRepository).save(captor.capture());

        PasswordResetCode saved = captor.getValue();
        assertThat(saved.getTeacherUid()).isEqualTo("teacher-1");
        assertThat(saved.getRawCode()).isNotBlank();
        assertThat(result.rawCode()).isEqualTo(saved.getRawCode());
        assertThat(result.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void delete_is_called_before_save() {
        IssuePasswordResetCodeCommand command = IssuePasswordResetCodeCommand.builder()
                .teacherUid("teacher-2").ttlMinutes(15).build();

        handler.execute(command);

        var inOrder = inOrder(codeRepository);
        inOrder.verify(codeRepository).deleteByTeacherUid("teacher-2");
        inOrder.verify(codeRepository).save(any(PasswordResetCode.class));
    }
}

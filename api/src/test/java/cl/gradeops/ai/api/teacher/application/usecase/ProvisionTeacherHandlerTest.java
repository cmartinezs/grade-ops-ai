package cl.gradeops.ai.api.teacher.application.usecase;

import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException;
import cl.gradeops.ai.api.teacher.application.command.ProvisionTeacherCommand;
import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.application.result.ProvisionTeacherResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvisionTeacherHandlerTest {

    @Mock AuthPort authPort;
    @Mock TeacherRepositoryPort teacherRepository;
    @Mock IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase;

    ProvisionTeacherHandler handler() {
        return new ProvisionTeacherHandler(authPort, teacherRepository, issuePasswordResetCodeUseCase, 30);
    }

    ProvisionTeacherCommand command(String email) {
        return ProvisionTeacherCommand.builder()
            .firstName("Ana").lastName("Soto").email(email).build();
    }

    @Test
    void shouldThrowDuplicateEmailExceptionAndSkipFirebaseWhenEmailAlreadyRegistered() {
        when(teacherRepository.existsByEmail("dup@x.com")).thenReturn(true);

        assertThatThrownBy(() -> handler().execute(command("dup@x.com")))
                .isInstanceOf(DuplicateEmailException.class);

        verifyNoInteractions(authPort);
    }

    @Test
    void shouldReturnFirebaseUidAndRawCodeWhenProvisioningSucceeds() {
        when(teacherRepository.existsByEmail("new@x.com")).thenReturn(false);
        when(authPort.createUser("new@x.com", "Ana Soto")).thenReturn("uid-123");
        when(issuePasswordResetCodeUseCase.execute(any()))
            .thenReturn(IssuePasswordResetCodeResult.builder()
                .rawCode("code-abc").expiresAt(Instant.now().plusSeconds(1800)).build());

        ProvisionTeacherResult result = handler().execute(command("new@x.com"));

        assertThat(result.firebaseUid()).isEqualTo("uid-123");
        assertThat(result.rawCode()).isEqualTo("code-abc");
    }

    @Test
    void shouldDeleteFirebaseUserWhenDbSaveFails() {
        when(teacherRepository.existsByEmail("fail@x.com")).thenReturn(false);
        when(authPort.createUser("fail@x.com", "Ana Soto")).thenReturn("uid-123");
        doThrow(new RuntimeException("db down")).when(teacherRepository).save(any());

        assertThatThrownBy(() -> handler().execute(command("fail@x.com")))
                .isInstanceOf(RuntimeException.class);

        verify(authPort).deleteUser("uid-123");
    }

    @Test
    void shouldNotCallDeleteUserWhenFirebaseCreateUserFails() {
        when(teacherRepository.existsByEmail("err@x.com")).thenReturn(false);
        when(authPort.createUser(eq("err@x.com"), any()))
            .thenThrow(new DuplicateEmailException("err@x.com"));

        assertThatThrownBy(() -> handler().execute(command("err@x.com")))
                .isInstanceOf(DuplicateEmailException.class);

        verify(authPort, never()).deleteUser(any());
    }

    @Test
    void shouldDeleteFirebaseUserWhenIssuePasswordResetCodeFails() {
        when(teacherRepository.existsByEmail("code@x.com")).thenReturn(false);
        when(authPort.createUser("code@x.com", "Ana Soto")).thenReturn("uid-456");
        when(issuePasswordResetCodeUseCase.execute(any()))
            .thenThrow(new RuntimeException("code service down"));

        assertThatThrownBy(() -> handler().execute(command("code@x.com")))
                .isInstanceOf(RuntimeException.class);

        verify(authPort).deleteUser("uid-456");
    }
}

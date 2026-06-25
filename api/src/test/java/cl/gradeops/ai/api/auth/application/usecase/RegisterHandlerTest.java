package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.command.RegisterCommand;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.auth.application.result.RegisterResult;
import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;
import cl.gradeops.ai.api.shared.domain.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterHandlerTest {

    private final AuthPort authPort = mock(AuthPort.class);
    private final TeacherRepositoryPort teacherRepository = mock(TeacherRepositoryPort.class);
    private final RegisterHandler handler = new RegisterHandler(authPort, teacherRepository);

    @Test
    void shouldReturnCreatedFalseWhenTeacherAlreadyExists() {
        TeacherIdentity identity = new TeacherIdentity("uid-1", "t@school.com", true, "Grace Hopper", SignInProvider.GOOGLE);
        when(authPort.verifyTokenUnchecked("token-1")).thenReturn(identity);
        when(teacherRepository.existsById("uid-1")).thenReturn(true);

        RegisterResult result = handler.execute(
                RegisterCommand.builder().idToken("token-1").firstName("Grace").lastName("Hopper").build());

        assertThat(result.uid()).isEqualTo("uid-1");
        assertThat(result.created()).isFalse();
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void shouldSaveAndReturnCreatedTrueWhenNewTeacher() {
        TeacherIdentity identity = new TeacherIdentity("uid-2", "a@school.com", true, "Ada Lovelace", SignInProvider.GOOGLE);
        when(authPort.verifyTokenUnchecked("token-2")).thenReturn(identity);
        when(teacherRepository.existsById("uid-2")).thenReturn(false);

        RegisterResult result = handler.execute(
                RegisterCommand.builder().idToken("token-2").firstName("Ada").lastName("Lovelace").build());

        assertThat(result.uid()).isEqualTo("uid-2");
        assertThat(result.created()).isTrue();
        verify(teacherRepository).save(any());
    }

    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenIsInvalid() {
        when(authPort.verifyTokenUnchecked("bad-token")).thenThrow(new IllegalArgumentException("bad"));

        assertThatThrownBy(() -> handler.execute(
                RegisterCommand.builder().idToken("bad-token").firstName("X").lastName("Y").build()))
                .isInstanceOf(InvalidTokenException.class);
    }
}

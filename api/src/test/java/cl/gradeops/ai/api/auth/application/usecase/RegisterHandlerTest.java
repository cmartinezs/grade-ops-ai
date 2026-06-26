package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.command.RegisterCommand;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.result.RegisterResult;
import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;
import cl.gradeops.ai.api.shared.domain.exception.InvalidTokenException;
import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.domain.model.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

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
        Teacher existing = Teacher.restore("uid-1", "Grace", "Hopper", "t@school.com",
                AuthProvider.GOOGLE, OffsetDateTime.now(), OffsetDateTime.now(),
                null, false, null, null, null, null);
        when(teacherRepository.findById("uid-1")).thenReturn(Optional.of(existing));

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
        when(teacherRepository.findById("uid-2")).thenReturn(Optional.empty());

        RegisterResult result = handler.execute(
                RegisterCommand.builder().idToken("token-2").firstName("Ada").lastName("Lovelace").build());

        assertThat(result.uid()).isEqualTo("uid-2");
        assertThat(result.created()).isTrue();
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenIsInvalid() {
        when(authPort.verifyTokenUnchecked("bad-token")).thenThrow(new IllegalArgumentException("bad"));

        assertThatThrownBy(() -> handler.execute(
                RegisterCommand.builder().idToken("bad-token").firstName("X").lastName("Y").build()))
                .isInstanceOf(InvalidTokenException.class);
    }
}

package cl.gradeops.ai.api.auth.application.orchestrator;

import cl.gradeops.ai.api.auth.application.command.ResetPasswordCommand;
import cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.domain.exception.InvalidResetCodeException;
import cl.gradeops.ai.api.auth.domain.exception.PasswordMismatchException;
import cl.gradeops.ai.api.auth.domain.exception.ResetCodeEmailMismatchException;
import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.auth.domain.valueobject.RawCode;
import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.domain.model.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ResetPasswordOrchestratorTest {

    private final PasswordResetCodeRepositoryPort codeRepository = mock(PasswordResetCodeRepositoryPort.class);
    private final AuthPort authPort = mock(AuthPort.class);
    private final RevokeRefreshTokensUseCase revokeRefreshTokensUseCase = mock(RevokeRefreshTokensUseCase.class);
    private final TeacherRepositoryPort teacherRepository = mock(TeacherRepositoryPort.class);
    private final ResetPasswordOrchestrator orchestrator =
            new ResetPasswordOrchestrator(codeRepository, authPort, revokeRefreshTokensUseCase, teacherRepository);

    @BeforeEach
    void setUp() {
        reset(codeRepository, authPort, revokeRefreshTokensUseCase, teacherRepository);
    }

    private ResetPasswordCommand command(String code, String email, String password) {
        return ResetPasswordCommand.builder()
                .rawCode(code).email(email).password(password).passwordRepeat(password).build();
    }

    private Teacher teacher(String uid, String email) {
        return Teacher.restore(uid, "Grace", "Hopper", email,
                AuthProvider.EMAIL_PASSWORD, OffsetDateTime.now(), OffsetDateTime.now(),
                null, false, null, null, null, null);
    }

    @Test
    void shouldThrowPasswordMismatchExceptionWhenPasswordsDoNotMatch() {
        ResetPasswordCommand command = ResetPasswordCommand.builder()
                .rawCode("code").email("a@test.com").password("pass1").passwordRepeat("pass2").build();

        assertThatThrownBy(() -> orchestrator.execute(command))
                .isInstanceOf(PasswordMismatchException.class);
    }

    @Test
    void shouldThrowInvalidResetCodeExceptionWhenCodeNotFound() {
        when(codeRepository.findByRawCode("bad-code")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orchestrator.execute(command("bad-code", "a@test.com", "pass123")))
                .isInstanceOf(InvalidResetCodeException.class);
    }

    @Test
    void shouldThrowInvalidResetCodeExceptionWhenCodeIsExpired() {
        PasswordResetCode expired = PasswordResetCode.restore("uid-1", new RawCode("code-1"),
                Instant.now().minusSeconds(3600), Instant.now().minusSeconds(7200), null);
        when(codeRepository.findByRawCode("code-1")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> orchestrator.execute(command("code-1", "a@test.com", "pass123")))
                .isInstanceOf(InvalidResetCodeException.class);
    }

    @Test
    void shouldThrowInvalidResetCodeExceptionWhenCodeIsAlreadyUsed() {
        PasswordResetCode used = PasswordResetCode.restore("uid-1", new RawCode("code-2"),
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(100), Instant.now());
        when(codeRepository.findByRawCode("code-2")).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> orchestrator.execute(command("code-2", "a@test.com", "pass123")))
                .isInstanceOf(InvalidResetCodeException.class);
    }

    @Test
    void shouldThrowResetCodeEmailMismatchExceptionWhenEmailDoesNotMatch() {
        PasswordResetCode code = PasswordResetCode.restore("uid-1", new RawCode("code-3"),
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(100), null);
        when(codeRepository.findByRawCode("code-3")).thenReturn(Optional.of(code));
        when(teacherRepository.findById("uid-1")).thenReturn(Optional.of(teacher("uid-1", "other@test.com")));

        assertThatThrownBy(() -> orchestrator.execute(command("code-3", "mismatch@test.com", "pass123")))
                .isInstanceOf(ResetCodeEmailMismatchException.class);
    }

    @Test
    void shouldUpdatePasswordAndRevokeTokensAndMarkUsedWhenCodeIsValid() {
        PasswordResetCode code = PasswordResetCode.restore("uid-1", new RawCode("code-4"),
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(100), null);
        when(codeRepository.findByRawCode("code-4")).thenReturn(Optional.of(code));
        when(teacherRepository.findById("uid-1")).thenReturn(Optional.of(teacher("uid-1", "g@test.com")));

        orchestrator.execute(command("code-4", "g@test.com", "newpass123"));

        verify(authPort).updatePassword("uid-1", "newpass123");
        verify(revokeRefreshTokensUseCase).execute("uid-1");
        verify(codeRepository).save(code);
        assertThat(code.isUsed()).isTrue();
    }
}

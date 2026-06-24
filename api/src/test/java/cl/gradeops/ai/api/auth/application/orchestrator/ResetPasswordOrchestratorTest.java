package cl.gradeops.ai.api.auth.application.orchestrator;

import cl.gradeops.ai.api.auth.application.command.ResetPasswordCommand;
import cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.domain.exception.InvalidResetCodeException;
import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResetPasswordOrchestratorTest {

    private final PasswordResetCodeRepositoryPort codeRepository = mock(PasswordResetCodeRepositoryPort.class);
    private final AuthPort authPort = mock(AuthPort.class);
    private final RevokeRefreshTokensUseCase revokeRefreshTokensUseCase = mock(RevokeRefreshTokensUseCase.class);
    private final TeacherRepository teacherRepository = mock(TeacherRepository.class);
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

    @Test
    void code_not_found_throws_InvalidResetCodeException() {
        when(codeRepository.findByRawCode("bad-code")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orchestrator.execute(command("bad-code", "a@test.com", "pass123")))
                .isInstanceOf(InvalidResetCodeException.class);
    }

    @Test
    void expired_code_throws_InvalidResetCodeException() {
        PasswordResetCode expired = PasswordResetCode.restore("uid-1", "code-1",
                Instant.now().minusSeconds(3600), Instant.now().minusSeconds(7200), null);
        when(codeRepository.findByRawCode("code-1")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> orchestrator.execute(command("code-1", "a@test.com", "pass123")))
                .isInstanceOf(InvalidResetCodeException.class);
    }

    @Test
    void used_code_throws_InvalidResetCodeException() {
        PasswordResetCode used = PasswordResetCode.restore("uid-1", "code-2",
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(100), Instant.now());
        when(codeRepository.findByRawCode("code-2")).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> orchestrator.execute(command("code-2", "a@test.com", "pass123")))
                .isInstanceOf(InvalidResetCodeException.class);
    }

    @Test
    void email_mismatch_throws_422() {
        PasswordResetCode code = PasswordResetCode.restore("uid-1", "code-3",
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(100), null);
        when(codeRepository.findByRawCode("code-3")).thenReturn(Optional.of(code));
        TeacherEntity teacher = new TeacherEntity("uid-1", "Grace", "Hopper", "other@test.com", "EMAIL_PASSWORD");
        when(teacherRepository.findById("uid-1")).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() -> orchestrator.execute(command("code-3", "mismatch@test.com", "pass123")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getReason()).isEqualTo("RESET_CODE_EMAIL_MISMATCH"));
    }

    @Test
    void happy_path_updates_password_revokes_tokens_marks_used() {
        PasswordResetCode code = PasswordResetCode.restore("uid-1", "code-4",
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(100), null);
        when(codeRepository.findByRawCode("code-4")).thenReturn(Optional.of(code));
        TeacherEntity teacher = new TeacherEntity("uid-1", "Grace", "Hopper", "g@test.com", "EMAIL_PASSWORD");
        when(teacherRepository.findById("uid-1")).thenReturn(Optional.of(teacher));

        orchestrator.execute(command("code-4", "g@test.com", "newpass123"));

        verify(authPort).updatePassword("uid-1", "newpass123");
        verify(revokeRefreshTokensUseCase).execute("uid-1");
        verify(codeRepository).save(code);
        assertThat(code.isUsed()).isTrue();
    }
}

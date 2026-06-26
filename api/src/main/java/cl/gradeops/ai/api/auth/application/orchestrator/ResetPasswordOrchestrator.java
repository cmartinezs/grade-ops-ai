package cl.gradeops.ai.api.auth.application.orchestrator;

import cl.gradeops.ai.api.auth.application.command.ResetPasswordCommand;
import cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase;
import cl.gradeops.ai.api.auth.application.port.in.ResetPasswordUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.domain.exception.InvalidResetCodeException;
import cl.gradeops.ai.api.auth.domain.exception.PasswordMismatchException;
import cl.gradeops.ai.api.auth.domain.exception.ResetCodeEmailMismatchException;
import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class ResetPasswordOrchestrator implements ResetPasswordUseCase {

    private final PasswordResetCodeRepositoryPort codeRepository;
    private final AuthPort authPort;
    private final RevokeRefreshTokensUseCase revokeRefreshTokensUseCase;
    private final TeacherRepositoryPort teacherRepository;

    @Override
    @Transactional
    public void execute(ResetPasswordCommand command) {
        if (!command.password().equals(command.passwordRepeat())) {
            throw new PasswordMismatchException();
        }

        PasswordResetCode code = codeRepository.findByRawCode(command.rawCode())
                .orElseThrow(() -> new InvalidResetCodeException("code not found"));

        if (code.isExpired()) {
            throw new InvalidResetCodeException("code expired");
        }
        if (code.isUsed()) {
            throw new InvalidResetCodeException("code already used");
        }

        Teacher teacher = teacherRepository.findById(code.getTeacherUid())
                .orElseThrow(() -> new InvalidResetCodeException("teacher not found"));

        if (!teacher.getEmail().equalsIgnoreCase(command.email())) {
            throw new ResetCodeEmailMismatchException();
        }

        authPort.updatePassword(teacher.getFirebaseUid(), command.password());
        revokeRefreshTokensUseCase.execute(teacher.getFirebaseUid());
        code.markUsed();
        codeRepository.save(code);
        log.debug("Password reset completed for uid={}, events={}", teacher.getFirebaseUid(),
                code.pullDomainEvents().size());
    }
}

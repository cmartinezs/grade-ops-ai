package cl.gradeops.ai.api.auth.application.orchestrator;

import cl.gradeops.ai.api.auth.application.command.ResetPasswordCommand;
import cl.gradeops.ai.api.auth.application.port.in.RevokeRefreshTokensUseCase;
import cl.gradeops.ai.api.auth.application.port.in.ResetPasswordUseCase;
import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.domain.exception.InvalidResetCodeException;
import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
public class ResetPasswordOrchestrator implements ResetPasswordUseCase {

    private final PasswordResetCodeRepositoryPort codeRepository;
    private final AuthPort authPort;
    private final RevokeRefreshTokensUseCase revokeRefreshTokensUseCase;
    private final TeacherRepository teacherRepository;

    @Override
    public void execute(ResetPasswordCommand command) {
        if (!command.password().equals(command.passwordRepeat())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_MISMATCH");
        }

        PasswordResetCode code = codeRepository.findByRawCode(command.rawCode())
                .orElseThrow(() -> new InvalidResetCodeException("code not found"));

        if (code.isExpired()) {
            throw new InvalidResetCodeException("code expired");
        }
        if (code.isUsed()) {
            throw new InvalidResetCodeException("code already used");
        }

        TeacherEntity teacher = teacherRepository.findById(code.getTeacherUid())
                .orElseThrow(() -> new InvalidResetCodeException("teacher not found"));

        if (!teacher.getEmail().equalsIgnoreCase(command.email())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "RESET_CODE_EMAIL_MISMATCH");
        }

        authPort.updatePassword(teacher.getFirebaseUid(), command.password());
        revokeRefreshTokensUseCase.execute(teacher.getFirebaseUid());
        code.markUsed();
        codeRepository.save(code);
    }
}

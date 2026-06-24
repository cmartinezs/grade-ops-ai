package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
import cl.gradeops.ai.api.auth.domain.model.PasswordResetCode;
import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import cl.gradeops.ai.api.auth.domain.valueobject.RawCode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public class IssuePasswordResetCodeHandler implements IssuePasswordResetCodeUseCase {

    private final PasswordResetCodeRepositoryPort codeRepository;

    @Override
    @Transactional
    public IssuePasswordResetCodeResult execute(IssuePasswordResetCodeCommand command) {
        if (command.provider() != SignInProvider.EMAIL_PASSWORD) {
            throw new IllegalArgumentException(
                    "Reset codes can only be issued for EMAIL_PASSWORD providers, got: " + command.provider());
        }

        codeRepository.deleteByTeacherUid(command.teacherUid());

        RawCode rawCode = RawCode.generate();
        Instant expiresAt = Instant.now().plus(command.ttlMinutes(), ChronoUnit.MINUTES);
        PasswordResetCode code = PasswordResetCode.issue(command.teacherUid(), rawCode, expiresAt);
        codeRepository.save(code);

        return IssuePasswordResetCodeResult.builder()
                .rawCode(rawCode.value())
                .expiresAt(expiresAt)
                .build();
    }
}

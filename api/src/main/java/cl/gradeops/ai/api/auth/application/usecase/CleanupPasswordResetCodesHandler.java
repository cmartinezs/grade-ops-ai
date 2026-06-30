package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.port.in.CleanupPasswordResetCodesUseCase;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public class CleanupPasswordResetCodesHandler implements CleanupPasswordResetCodesUseCase {

    private final PasswordResetCodeRepositoryPort codeRepository;
    private final Clock clock;
    private final int retentionDays;

    public long execute() {
        var threshold = Instant.now(clock).minus(retentionDays, ChronoUnit.DAYS);
        return codeRepository.deleteAllClosedCreatedBefore(threshold);
    }
}

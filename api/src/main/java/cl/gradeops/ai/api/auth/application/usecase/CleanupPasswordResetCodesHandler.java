package cl.gradeops.ai.api.auth.application.usecase;

import cl.gradeops.ai.api.auth.application.port.in.CleanupPasswordResetCodesUseCase;
import cl.gradeops.ai.api.auth.application.port.out.PasswordResetCodeRepositoryPort;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor
public class CleanupPasswordResetCodesHandler implements CleanupPasswordResetCodesUseCase {

    private final PasswordResetCodeRepositoryPort codeRepository;
    private final Clock clock;
    @Getter
    private final int retentionDays;

    public long execute() {
        var threshold = Instant.now(clock).minus(retentionDays, ChronoUnit.DAYS);
        log.info("PasswordResetCode cleanup threshold={}", threshold);
        return codeRepository.deleteAllClosedCreatedBefore(threshold);
    }
}

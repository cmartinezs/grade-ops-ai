package cl.gradeops.ai.api.auth.infrastructure.adapter.in.scheduler;

import cl.gradeops.ai.api.auth.application.port.in.CleanupPasswordResetCodesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class PasswordResetCodeCleanupJob {

    private final CleanupPasswordResetCodesUseCase cleanupUseCase;

    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    public void run() {
        log.info("PasswordResetCode cleanup started — retentionDays={}", cleanupUseCase.getRetentionDays());
        var start = Instant.now();
        try {
            long deleted = cleanupUseCase.execute();
            log.info("PasswordResetCode cleanup finished — deleted={}, durationMs={}",
                    deleted, Duration.between(start, Instant.now()).toMillis());
        } catch (Exception e) {
            log.error("PasswordResetCode cleanup failed", e);
        }
    }
}

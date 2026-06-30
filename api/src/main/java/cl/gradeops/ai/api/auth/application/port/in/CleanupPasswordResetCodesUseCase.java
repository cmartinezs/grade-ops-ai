package cl.gradeops.ai.api.auth.application.port.in;

public interface CleanupPasswordResetCodesUseCase {
    long execute();
    int getRetentionDays();
}

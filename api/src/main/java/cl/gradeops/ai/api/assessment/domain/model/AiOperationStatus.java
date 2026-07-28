package cl.gradeops.ai.api.assessment.domain.model;

public enum AiOperationStatus {
    PENDING,
    IN_PROGRESS,
    SUCCEEDED,
    FAILED_RETRYABLE,
    FAILED_TERMINAL
}

package cl.gradeops.ai.api.assessment;

public record AssessmentSummaryDto(
        String id,
        String title,
        AssessmentStatus status,
        int submissionCount,
        int pendingApprovals,
        String reportLink   // nullable
) {}

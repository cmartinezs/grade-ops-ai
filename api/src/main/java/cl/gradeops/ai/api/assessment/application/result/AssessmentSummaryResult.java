package cl.gradeops.ai.api.assessment.application.result;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import lombok.Builder;

@Builder
public record AssessmentSummaryResult(
    String id,
    String title,
    AssessmentStatus status,
    int submissionCount,
    int pendingApprovals,
    String reportLink
) {}

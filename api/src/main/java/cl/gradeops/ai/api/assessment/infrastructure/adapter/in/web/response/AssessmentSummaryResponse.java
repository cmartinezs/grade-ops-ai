package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;

public record AssessmentSummaryResponse(
    String id,
    String title,
    AssessmentStatus status,
    int submissionCount,
    int pendingApprovals,
    String reportLink
) {}

package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import java.util.UUID;

/** Row shape for {@link AssessmentJpaRepository#findSummariesByTeacherUid}. */
public interface AssessmentSummaryProjection {
    UUID getId();
    String getStatus();
    String getTitle();
}

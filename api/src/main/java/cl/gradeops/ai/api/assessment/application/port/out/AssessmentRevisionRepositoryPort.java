package cl.gradeops.ai.api.assessment.application.port.out;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentRevisionRepositoryPort {
    void save(AssessmentRevision revision);
    Optional<AssessmentRevision> findById(UUID id);
    List<AssessmentRevision> findAllByAssessmentId(AssessmentId assessmentId);
}

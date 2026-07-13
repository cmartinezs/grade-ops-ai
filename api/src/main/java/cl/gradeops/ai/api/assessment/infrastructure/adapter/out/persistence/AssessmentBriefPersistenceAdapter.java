package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class AssessmentBriefPersistenceAdapter implements AssessmentBriefRepositoryPort {

    private final AssessmentBriefJpaRepository jpaRepository;
    private final AssessmentBriefPersistenceMapper mapper;

    @Override
    public void save(AssessmentBrief brief) {
        jpaRepository.save(mapper.toEntity(brief));
    }

    @Override
    public Optional<AssessmentBrief> findByAssessmentId(AssessmentId assessmentId) {
        return jpaRepository.findByAssessmentId(assessmentId.value()).map(mapper::toDomain);
    }
}

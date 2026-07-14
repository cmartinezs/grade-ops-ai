package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AssessmentDraftPersistenceAdapter implements AssessmentDraftRepositoryPort {

    private final AssessmentDraftJpaRepository jpaRepository;
    private final AssessmentDraftPersistenceMapper mapper;

    @Override
    public void save(AssessmentDraft draft) {
        jpaRepository.save(mapper.toEntity(draft));
    }

    @Override
    public Optional<AssessmentDraft> findCurrentByAssessmentId(AssessmentId assessmentId) {
        return jpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessmentId.value())
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public List<AssessmentDraft> findAllByAssessmentId(AssessmentId assessmentId) {
        return jpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessmentId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

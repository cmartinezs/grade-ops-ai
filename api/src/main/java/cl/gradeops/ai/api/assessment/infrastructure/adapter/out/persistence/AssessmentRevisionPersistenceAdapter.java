package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AssessmentRevisionPersistenceAdapter implements AssessmentRevisionRepositoryPort {

    private final AssessmentRevisionJpaRepository jpaRepository;
    private final AssessmentRevisionPersistenceMapper mapper;

    @Override
    public void save(AssessmentRevision revision) {
        jpaRepository.save(mapper.toEntity(revision));
    }

    @Override
    public Optional<AssessmentRevision> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AssessmentRevision> findAllByAssessmentId(AssessmentId assessmentId) {
        return jpaRepository.findAllByAssessmentIdOrderByVersionNumberDesc(assessmentId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

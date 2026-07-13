package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AssessmentPersistenceAdapter implements AssessmentRepositoryPort {

    private final AssessmentJpaRepository jpaRepository;
    private final AssessmentPersistenceMapper mapper;

    @Override
    public void save(Assessment assessment) {
        jpaRepository.save(mapper.toEntity(assessment));
    }

    @Override
    public Optional<Assessment> findById(AssessmentId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<AssessmentSummaryResult> findAllByTeacherId(String teacherUid) {
        // Intentionally empty-list-preserving until task-10 joins in the current
        // draft/brief title — see task-10's Technical Design, which documents this
        // method as "left as an empty-list-preserving stub-equivalent in task-01".
        // Mapping real assessment rows here (even with title = null) would regress
        // the dashboard as soon as task-06 starts creating Assessment rows, before
        // task-10's join logic exists to populate them correctly.
        return List.of();
    }
}

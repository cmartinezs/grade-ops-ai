package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
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
        // Temporary mapping: Assessment has no title of its own — task-10 wires the
        // display title in from the current draft/brief once those tables exist.
        return jpaRepository.findAllByTeacherUid(teacherUid).stream()
                .map(e -> AssessmentSummaryResult.builder()
                        .id(e.getId().toString())
                        .title(null)
                        .status(AssessmentStatus.valueOf(e.getStatus()))
                        .submissionCount(0)
                        .pendingApprovals(0)
                        .reportLink(null)
                        .build())
                .toList();
    }
}

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
        return jpaRepository.findSummariesByTeacherUid(teacherUid).stream()
                .map(p -> AssessmentSummaryResult.builder()
                        .id(p.getId().toString())
                        .title(p.getTitle())
                        .status(AssessmentStatus.valueOf(p.getStatus()))
                        .submissionCount(0)
                        .pendingApprovals(0)
                        .reportLink(null)
                        .build())
                .toList();
    }
}

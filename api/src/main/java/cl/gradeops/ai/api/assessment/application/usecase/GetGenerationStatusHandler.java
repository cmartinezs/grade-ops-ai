package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientProperties;
import cl.gradeops.ai.api.assessment.application.command.GetGenerationStatusCommand;
import cl.gradeops.ai.api.assessment.application.port.in.GetGenerationStatusUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GetGenerationStatusResult;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>{@code GET .../generation-status} — read-only, derives a durable status snapshot from {@code
 * Assessment}/{@code AiOperation}/{@code AgentAttempt} at read time. Scoped to {@code
 * CREATE_INITIAL_REVISION} (see {@link AiOperationCoordinator#retryInitialRevision}'s javadoc).
 * No scheduler, no background reconciliation — {@code INDETERMINATE} is computed on the fly, per
 * LOCAL-CONTRACTS.md § "Orphaned in-flight detection."
 *
 * <p>Status taxonomy: {@code currentRevisionId != null} always wins first and reports {@code
 * SUCCEEDED} (an assessment can reach a current revision via a human edit, which has no {@code
 * AiOperation} at all, or via generate/regenerate — the read path does not need to know which).
 * Otherwise the latest {@code AiOperation}'s own status is projected: PENDING/IN_PROGRESS become
 * either {@code IN_PROGRESS} or {@code INDETERMINATE} depending on the latest {@code
 * AgentAttempt}'s age; FAILED_RETRYABLE/FAILED_TERMINAL are reported as themselves (verbatim —
 * FAILED_TERMINAL is not one of LOCAL-CONTRACTS.md's four explicitly enumerated status values,
 * a gap this session filled honestly rather than mislabeling a non-retryable failure as
 * retryable; documented in API-A3-HANDOFF.md).
 */
public class GetGenerationStatusHandler implements GetGenerationStatusUseCase {

    private static final AiOperationType OPERATION_TYPE = AiOperationType.CREATE_INITIAL_REVISION;
    private static final Duration INDETERMINATE_THRESHOLD = AgentClientProperties.READ_TIMEOUT.multipliedBy(5);

    private final AssessmentRepositoryPort assessmentRepository;
    private final AiOperationRepositoryPort aiOperationRepository;
    private final AgentAttemptRepositoryPort agentAttemptRepository;
    private final OwnershipVerifier ownershipVerifier;

    public GetGenerationStatusHandler(AssessmentRepositoryPort assessmentRepository,
                                       AiOperationRepositoryPort aiOperationRepository,
                                       AgentAttemptRepositoryPort agentAttemptRepository,
                                       OwnershipVerifier ownershipVerifier) {
        this.assessmentRepository = assessmentRepository;
        this.aiOperationRepository = aiOperationRepository;
        this.agentAttemptRepository = agentAttemptRepository;
        this.ownershipVerifier = ownershipVerifier;
    }

    @Override
    public GetGenerationStatusResult execute(GetGenerationStatusCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        if (assessment.getCurrentRevisionId() != null) {
            return new GetGenerationStatusResult(OPERATION_TYPE.name(), "SUCCEEDED", null, false,
                    assessment.getCurrentRevisionId());
        }

        Optional<AiOperation> latestOpt = aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, OPERATION_TYPE);
        if (latestOpt.isEmpty()) {
            return new GetGenerationStatusResult(OPERATION_TYPE.name(), "NOT_STARTED", null, false, null);
        }
        AiOperation latest = latestOpt.get();

        return switch (latest.getStatus()) {
            case SUCCEEDED -> new GetGenerationStatusResult(OPERATION_TYPE.name(), "SUCCEEDED", null, false,
                    latest.getResultRevisionId());
            case FAILED_RETRYABLE -> new GetGenerationStatusResult(OPERATION_TYPE.name(), "FAILED_RETRYABLE",
                    latestFailureCode(latest.getId()), true, null);
            case FAILED_TERMINAL -> new GetGenerationStatusResult(OPERATION_TYPE.name(), "FAILED_TERMINAL",
                    latestFailureCode(latest.getId()), false, null);
            case PENDING, IN_PROGRESS -> {
                AgentAttempt latestAttempt = latestAttempt(latest.getId());
                boolean indeterminate = latestAttempt.getStatus() == AgentAttemptStatus.DISPATCHED
                        && Duration.between(latestAttempt.getDispatchedAt(), Instant.now()).compareTo(INDETERMINATE_THRESHOLD) > 0;
                yield indeterminate
                        ? new GetGenerationStatusResult(OPERATION_TYPE.name(), "INDETERMINATE", null, true, null)
                        : new GetGenerationStatusResult(OPERATION_TYPE.name(), "IN_PROGRESS", null, false, null);
            }
        };
    }

    private String latestFailureCode(java.util.UUID operationId) {
        return latestAttempt(operationId).getFailureCode();
    }

    private AgentAttempt latestAttempt(java.util.UUID operationId) {
        List<AgentAttempt> attempts = agentAttemptRepository.findAllByAiOperationId(operationId);
        return attempts.get(0);
    }
}

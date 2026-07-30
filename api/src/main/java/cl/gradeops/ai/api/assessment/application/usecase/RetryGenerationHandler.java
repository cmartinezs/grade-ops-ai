package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AgentClientProperties;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.RetryGenerationCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoActiveOperationToRetryException;
import cl.gradeops.ai.api.assessment.application.exception.OperationInProgressException;
import cl.gradeops.ai.api.assessment.application.exception.StaleOnCompletionException;
import cl.gradeops.ai.api.assessment.application.port.in.RetryGenerationUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.RetryGenerationResult;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>{@code POST .../draft/retry} — scoped to {@code CREATE_INITIAL_REVISION} only (see {@link
 * AiOperationCoordinator#retryInitialRevision}'s own javadoc for why). Always responds with an
 * {@code AiOperation} snapshot, {@code 202}, regardless of whether the retried dispatch itself
 * succeeded or failed this time — Web is documented to never parse this body, only to re-poll
 * {@code generation-status} afterward (Authoring Operation Contract § 3). This means, unlike
 * {@link GenerateAssessmentDraftHandler}/{@link RegenerateAssessmentDraftHandler}, a failure from
 * {@link AiOperationCoordinator#retryInitialRevision} is caught here, not propagated — the
 * coordinator has already durably recorded the outcome by the time it throws.
 */
public class RetryGenerationHandler implements RetryGenerationUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryGenerationHandler.class);
    private static final AiOperationType OPERATION_TYPE = AiOperationType.CREATE_INITIAL_REVISION;
    private static final Duration INDETERMINATE_THRESHOLD = AgentClientProperties.READ_TIMEOUT.multipliedBy(5);

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final AiOperationRepositoryPort aiOperationRepository;
    private final AgentAttemptRepositoryPort agentAttemptRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final AiOperationCoordinator aiOperationCoordinator;

    public RetryGenerationHandler(AssessmentRepositoryPort assessmentRepository,
                                   AssessmentBriefRepositoryPort assessmentBriefRepository,
                                   AiOperationRepositoryPort aiOperationRepository,
                                   AgentAttemptRepositoryPort agentAttemptRepository,
                                   OwnershipVerifier ownershipVerifier,
                                   AiOperationCoordinator aiOperationCoordinator) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentBriefRepository = assessmentBriefRepository;
        this.aiOperationRepository = aiOperationRepository;
        this.agentAttemptRepository = agentAttemptRepository;
        this.ownershipVerifier = ownershipVerifier;
        this.aiOperationCoordinator = aiOperationCoordinator;
    }

    @Override
    public RetryGenerationResult execute(RetryGenerationCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        AiOperation latest = aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, OPERATION_TYPE)
                .orElseThrow(() -> new NoActiveOperationToRetryException(assessmentId.value().toString()));

        if (latest.getStatus() == AiOperationStatus.SUCCEEDED || latest.getStatus() == AiOperationStatus.FAILED_TERMINAL) {
            throw new NoActiveOperationToRetryException(assessmentId.value().toString());
        }

        List<AgentAttempt> attempts = agentAttemptRepository.findAllByAiOperationId(latest.getId());
        AgentAttempt latestAttempt = attempts.get(0);

        if (latest.getStatus() != AiOperationStatus.FAILED_RETRYABLE) {
            // PENDING or IN_PROGRESS: only allowed past the indeterminate threshold.
            boolean indeterminate = latestAttempt.getStatus() == AgentAttemptStatus.DISPATCHED
                    && Duration.between(latestAttempt.getDispatchedAt(), Instant.now()).compareTo(INDETERMINATE_THRESHOLD) > 0;
            if (!indeterminate) {
                throw new OperationInProgressException(assessmentId.value().toString());
            }
            log.warn("Retrying assessment {} while AiOperation {} is still nominally {} — its latest "
                            + "AgentAttempt {} has been DISPATCHED since {}, past the indeterminate threshold "
                            + "of {}. Flagged as a stale-retry, not hidden (Authoring Operation Contract ADR).",
                    assessmentId.value(), latest.getId(), latest.getStatus(), latestAttempt.getId(),
                    latestAttempt.getDispatchedAt(), INDETERMINATE_THRESHOLD);
        }

        AssessmentBrief brief = assessmentBriefRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        AssessmentCommand agentCommand = new AssessmentCommand(
                brief.getLearningGoal(), brief.getTopic(), brief.getLevel(), brief.getDuration(), brief.getLanguage(),
                null, null, null, null, null);

        int nextAttemptNumber = latestAttempt.getAttemptNumber() + 1;

        try {
            aiOperationCoordinator.retryInitialRevision(assessment, latest, agentCommand, nextAttemptNumber);
        } catch (AgentClientException | StaleOnCompletionException ex) {
            // The coordinator already durably recorded AgentAttempt/AiOperation's terminal state
            // before throwing — retry's own contract is to always respond 202 regardless, so the
            // exception is intentionally swallowed here, not rethrown.
        }

        return toResult(latest.getId());
    }

    private RetryGenerationResult toResult(java.util.UUID operationId) {
        AiOperation reloaded = aiOperationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(operationId.toString()));
        List<AgentAttempt> attempts = agentAttemptRepository.findAllByAiOperationId(operationId);
        AgentAttempt latestAttempt = attempts.get(0);
        boolean retryable = reloaded.getStatus() == AiOperationStatus.FAILED_RETRYABLE;
        return new RetryGenerationResult(reloaded.getId(), reloaded.getOperationType().name(),
                reloaded.getStatus().name(), latestAttempt.getFailureCode(), retryable, reloaded.getResultRevisionId());
    }
}

package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.AlreadyGeneratedException;
import cl.gradeops.ai.api.assessment.application.exception.OperationInProgressException;
import cl.gradeops.ai.api.assessment.application.exception.StaleOnCompletionException;
import cl.gradeops.ai.api.assessment.application.port.in.GenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftOutcome;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.application.result.RetryGenerationResult;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyCompletionContext;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyPayloadHasher;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRecord;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>The write path: checks idempotency (Task 06) &rarr; checks for a matching-key operation to
 * recover/relay (A3 Final Idempotency Correction § 5/6) &rarr; checks the {@code
 * ALREADY_GENERATED} precondition &rarr; delegates to the durable {@link AiOperationCoordinator}
 * (Task 07B). A legacy {@link Assessment} restored with {@code currentRevisionId == null} is
 * treated as "not generated yet," never as an error — this is what lets a pre-cut assessment go
 * through initial generation successfully.
 *
 * <p>A3 Contract Correction § Correction 2: the coordinator has already durably recorded the
 * {@code AiOperation}/{@code AgentAttempt} pair (Phase 0) before an {@link AgentClientException}
 * or {@link StaleOnCompletionException} can be thrown — this handler catches those, plus (A3
 * Final Idempotency Correction) {@link OperationInProgressException} for the case where this
 * request lost a Phase-0 race to a sibling request carrying the SAME idempotency key — and
 * reports {@code 202 Accepted} with a snapshot of the durable operation, instead of letting the
 * exception propagate as a bare error or a spurious {@code 409}. The coordinator itself writes
 * the {@code IdempotencyRecord} atomically with the durable outcome (success or failure) — this
 * handler no longer writes one after the fact.
 */
public class GenerateAssessmentDraftHandler implements GenerateAssessmentDraftUseCase {

    private static final String OPERATION_TYPE = "CREATE_INITIAL_REVISION";

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    private final AiOperationRepositoryPort aiOperationRepository;
    private final AgentAttemptRepositoryPort agentAttemptRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final IdempotencyGuard idempotencyGuard;
    private final AiOperationCoordinator aiOperationCoordinator;

    public GenerateAssessmentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                           AssessmentBriefRepositoryPort assessmentBriefRepository,
                                           AssessmentRevisionRepositoryPort assessmentRevisionRepository,
                                           AiOperationRepositoryPort aiOperationRepository,
                                           AgentAttemptRepositoryPort agentAttemptRepository,
                                           OwnershipVerifier ownershipVerifier,
                                           IdempotencyGuard idempotencyGuard,
                                           AiOperationCoordinator aiOperationCoordinator) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentBriefRepository = assessmentBriefRepository;
        this.assessmentRevisionRepository = assessmentRevisionRepository;
        this.aiOperationRepository = aiOperationRepository;
        this.agentAttemptRepository = agentAttemptRepository;
        this.ownershipVerifier = ownershipVerifier;
        this.idempotencyGuard = idempotencyGuard;
        this.aiOperationCoordinator = aiOperationCoordinator;
    }

    @Override
    public GenerateAssessmentDraftOutcome execute(GenerateAssessmentDraftCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        IdempotencyScope scope = IdempotencyScope.assessment(assessmentId.value());
        String payloadHash = IdempotencyPayloadHasher.hash(assessmentId.value().toString());
        Optional<IdempotencyRecord> prior =
                idempotencyGuard.check(scope, OPERATION_TYPE, command.idempotencyKey(), payloadHash);
        if (prior.isPresent()) {
            return replay(prior.get());
        }

        Optional<GenerateAssessmentDraftOutcome> recovered =
                recoverFromExistingOperation(assessmentId, scope, payloadHash, command.idempotencyKey());
        if (recovered.isPresent()) {
            return recovered.get();
        }

        AssessmentBrief brief = assessmentBriefRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));

        if (assessment.getCurrentRevisionId() != null) {
            throw new AlreadyGeneratedException(assessmentId.value().toString(), assessment.getCurrentRevisionId());
        }

        AssessmentCommand agentCommand = new AssessmentCommand(
                brief.getLearningGoal(), brief.getTopic(), brief.getLevel(), brief.getDuration(), brief.getLanguage(),
                null, null, null, null, null);

        IdempotencyCompletionContext idempotencyContext =
                new IdempotencyCompletionContext(scope, OPERATION_TYPE, command.idempotencyKey(), payloadHash);

        try {
            AssessmentRevision revision = aiOperationCoordinator.createInitialRevision(
                    assessment, agentCommand, command.teacherUid(), command.idempotencyKey(), idempotencyContext);

            return new GenerateAssessmentDraftOutcome.RevisionCreated(GenerateAssessmentDraftResult.fromRevision(revision));
        } catch (AgentClientException | StaleOnCompletionException | OperationInProgressException ex) {
            AiOperation latest = aiOperationRepository
                    .findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION)
                    .orElseThrow(() -> ex);

            return new GenerateAssessmentDraftOutcome.OperationAccepted(toOperationSnapshot(latest));
        }
    }

    /**
     * A3 Final Idempotency Correction § 5/6: no {@code IdempotencyRecord} was found for this key
     * (either a pre-fix orphaned {@code AiOperation}, or a genuinely concurrent sibling request
     * that has not committed Phase 2 yet). If the latest {@code AiOperation} for this assessment
     * carries the SAME idempotency key, relay its state instead of dispatching a second time:
     * still in flight &rarr; a {@code 202} snapshot of it; terminal &rarr; reconstruct the
     * original outcome and backfill the missing record. A different (or absent) key falls through
     * to the normal {@code ALREADY_GENERATED}/dispatch path, unchanged.
     */
    private Optional<GenerateAssessmentDraftOutcome> recoverFromExistingOperation(
            AssessmentId assessmentId, IdempotencyScope scope, String payloadHash, String idempotencyKey) {
        Optional<AiOperation> existing = aiOperationRepository
                .findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION);
        if (existing.isEmpty() || !existing.get().getIdempotencyKey().equals(idempotencyKey)) {
            return Optional.empty();
        }

        AiOperation operation = existing.get();
        if (operation.getStatus() == AiOperationStatus.PENDING || operation.getStatus() == AiOperationStatus.IN_PROGRESS) {
            return Optional.of(new GenerateAssessmentDraftOutcome.OperationAccepted(toOperationSnapshot(operation)));
        }

        if (operation.getStatus() == AiOperationStatus.SUCCEEDED) {
            AssessmentRevision revision = assessmentRevisionRepository.findById(operation.getResultRevisionId())
                    .orElseThrow(() -> new ResourceNotFoundException(operation.getResultRevisionId().toString()));
            backfillIdempotencyRecord(scope, idempotencyKey, payloadHash, revision.getId().toString(), 201);
            return Optional.of(new GenerateAssessmentDraftOutcome.RevisionCreated(GenerateAssessmentDraftResult.fromRevision(revision)));
        }

        // FAILED_RETRYABLE / FAILED_TERMINAL — the original dispatch already durably failed;
        // relay it and backfill the missing record without dispatching a second AgentAttempt.
        backfillIdempotencyRecord(scope, idempotencyKey, payloadHash, operation.getId().toString(), 202);
        return Optional.of(new GenerateAssessmentDraftOutcome.OperationAccepted(toOperationSnapshot(operation)));
    }

    /**
     * Best-effort: another concurrent recovery for the same key may have already backfilled this
     * exact record, in which case {@code idempotency_records}' own unique constraint rejects the
     * duplicate insert — harmless, since the outcome already being returned is correct regardless.
     */
    private void backfillIdempotencyRecord(IdempotencyScope scope, String idempotencyKey, String payloadHash,
                                            String resultReference, int responseStatus) {
        try {
            idempotencyGuard.record(scope, OPERATION_TYPE, idempotencyKey, payloadHash, resultReference, responseStatus);
        } catch (DataIntegrityViolationException ex) {
            // Already backfilled by a concurrent recovery — nothing further to do.
        }
    }

    private GenerateAssessmentDraftOutcome replay(IdempotencyRecord record) {
        if (record.getResponseStatus() != null && record.getResponseStatus() == 202) {
            UUID operationId = UUID.fromString(record.getResultReference());
            AiOperation operation = aiOperationRepository.findById(operationId)
                    .orElseThrow(() -> new ResourceNotFoundException(operationId.toString()));
            return new GenerateAssessmentDraftOutcome.OperationAccepted(toOperationSnapshot(operation));
        }
        UUID revisionId = UUID.fromString(record.getResultReference());
        AssessmentRevision revision = assessmentRevisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException(revisionId.toString()));
        return new GenerateAssessmentDraftOutcome.RevisionCreated(GenerateAssessmentDraftResult.fromRevision(revision));
    }

    private RetryGenerationResult toOperationSnapshot(AiOperation operation) {
        List<AgentAttempt> attempts = agentAttemptRepository.findAllByAiOperationId(operation.getId());
        AgentAttempt latestAttempt = attempts.get(0);
        boolean retryable = operation.getStatus() == AiOperationStatus.FAILED_RETRYABLE;
        return new RetryGenerationResult(operation.getId(), operation.getOperationType().name(),
                operation.getStatus().name(), latestAttempt.getFailureCode(), retryable, operation.getResultRevisionId());
    }
}

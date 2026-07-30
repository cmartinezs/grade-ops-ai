package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.port.in.CreateAssessmentBriefUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyPayloadHasher;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRecord;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>{@code (teacherUid, CREATE_ASSESSMENT_BRIEF, idempotencyKey)}-scoped idempotency guard
 * (A3 Contract Correction § Correction 1) — the Assessment/AssessmentBrief/IdempotencyRecord
 * triad is written atomically in one transaction, so a genuinely failed creation leaves no
 * idempotency record behind (retryable with the same key). A concurrent duplicate (two requests
 * racing with the same key/payload) is resolved by {@code idempotency_records}' own unique
 * constraint: the loser's transaction is rolled back by the DB, and it then replays the winner's
 * committed record instead of surfacing the raw constraint violation.
 */
public class CreateAssessmentBriefHandler implements CreateAssessmentBriefUseCase {

    private static final String OPERATION_TYPE = "CREATE_ASSESSMENT_BRIEF";

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final IdempotencyGuard idempotencyGuard;
    private final TransactionTemplate transactionTemplate;

    public CreateAssessmentBriefHandler(AssessmentRepositoryPort assessmentRepository,
                                         AssessmentBriefRepositoryPort assessmentBriefRepository,
                                         IdempotencyGuard idempotencyGuard,
                                         PlatformTransactionManager transactionManager) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentBriefRepository = assessmentBriefRepository;
        this.idempotencyGuard = idempotencyGuard;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public CreateAssessmentBriefResult execute(CreateAssessmentBriefCommand command) {
        IdempotencyScope scope = IdempotencyScope.teacher(command.teacherUid());
        String payloadHash = IdempotencyPayloadHasher.hash(command.learningGoal(), command.topic(),
                command.level(), command.duration(), command.language());

        Optional<IdempotencyRecord> prior =
                idempotencyGuard.check(scope, OPERATION_TYPE, command.idempotencyKey(), payloadHash);
        if (prior.isPresent()) {
            return new CreateAssessmentBriefResult(prior.get().getResultReference());
        }

        try {
            return transactionTemplate.execute(status -> {
                Assessment assessment = Assessment.create(command.teacherUid());
                assessmentRepository.save(assessment);

                AssessmentBrief brief = AssessmentBrief.create(
                        assessment.getId(), command.learningGoal(), command.topic(),
                        command.level(), command.duration(), command.language());
                assessmentBriefRepository.save(brief);

                idempotencyGuard.record(scope, OPERATION_TYPE, command.idempotencyKey(), payloadHash,
                        assessment.getId().value().toString(), 201);

                return new CreateAssessmentBriefResult(assessment.getId().value().toString());
            });
        } catch (DataIntegrityViolationException ex) {
            // A concurrent request with the same (teacherUid, key) won the idempotency_records
            // race — this transaction rolled back entirely (no orphaned Assessment survives).
            // Replay the winner's committed record; a genuine payload mismatch under the race
            // still throws IdempotencyKeyPayloadMismatchException from this same check.
            return idempotencyGuard.check(scope, OPERATION_TYPE, command.idempotencyKey(), payloadHash)
                    .map(record -> new CreateAssessmentBriefResult(record.getResultReference()))
                    .orElseThrow(() -> ex);
        }
    }
}

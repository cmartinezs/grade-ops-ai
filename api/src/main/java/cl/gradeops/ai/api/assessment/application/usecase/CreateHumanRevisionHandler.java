package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.CreateHumanRevisionCommand;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
import cl.gradeops.ai.api.assessment.application.port.in.CreateHumanRevisionUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>Replaces {@code UpdateAssessmentDraftHandler}'s in-place edit: every human edit creates a
 * new, immutable {@link AssessmentRevision} (origin {@code HUMAN_EDITED}) chained after the
 * revision the client expected to be current — it never mutates an existing row. No call to
 * {@code agents/} is made and no {@code AiOperation}/{@code AgentAttempt} is created (Authoring
 * Operation Contract § 6).
 *
 * <p>The staleness check (application-level, pre-write) and the {@code lockVersion} CAS
 * (DB-level, at flush/commit) are two separate mechanisms guarding the same invariant: the first
 * catches the common case cheaply inside the transaction; the second is what actually catches two
 * truly concurrent writers racing on the same {@code expectedRevisionId}. Because {@code
 * assessments.current_revision_id} is a foreign key into {@code assessment_revisions}, the new
 * revision row must be inserted before the CAS-protected {@code Assessment} update in the same
 * transaction — which means a lost race can surface as either exception, depending on timing:
 * {@link ObjectOptimisticLockingFailureException} if the loser reaches the {@code Assessment}
 * update at all, or a {@link DataIntegrityViolationException} on {@code assessment_revisions}'
 * own {@code UNIQUE (assessment_id, version_number)} constraint if the loser's insert collides
 * with the winner's before that (both threads compute the same {@code versionNumber} from the
 * same immutable {@code expectedRevision} read). Both are the same underlying conflict and are
 * reported identically as {@code STALE_REVISION} — the unique constraint is not a separate
 * failure mode, just an earlier detection point for the identical race (Idempotency and
 * Concurrency Strategy ADR § "Version number generation" explicitly names this constraint as
 * defense-in-depth for exactly this scenario). Both exceptions surface only after this method's
 * transactional callback has already returned, so they are caught outside the transaction
 * template.
 */
public class CreateHumanRevisionHandler implements CreateHumanRevisionUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final TransactionTemplate transactionTemplate;

    public CreateHumanRevisionHandler(AssessmentRepositoryPort assessmentRepository,
                                       AssessmentRevisionRepositoryPort assessmentRevisionRepository,
                                       OwnershipVerifier ownershipVerifier,
                                       PlatformTransactionManager transactionManager) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentRevisionRepository = assessmentRevisionRepository;
        this.ownershipVerifier = ownershipVerifier;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public GenerateAssessmentDraftResult execute(CreateHumanRevisionCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        try {
            AssessmentRevision newRevision = transactionTemplate.execute(status -> {
                Assessment fresh = assessmentRepository.findById(assessmentId)
                        .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
                if (fresh.getCurrentRevisionId() == null
                        || !fresh.getCurrentRevisionId().equals(command.expectedRevisionId())) {
                    throw new StaleRevisionException(assessmentId.value().toString());
                }

                AssessmentRevision expectedRevision = assessmentRevisionRepository.findById(command.expectedRevisionId())
                        .orElseThrow(() -> new ResourceNotFoundException(command.expectedRevisionId().toString()));

                AssessmentRevision revision = AssessmentRevision.createFromHumanEdit(expectedRevision,
                        command.title(), command.context(), command.instructions(),
                        command.objectives(), command.deliverables(), command.constraints(),
                        command.teacherUid(), command.reason());
                assessmentRevisionRepository.save(revision);

                assessmentRepository.save(fresh.withCurrentRevision(revision.getId()));

                return revision;
            });

            return GenerateAssessmentDraftResult.fromRevision(newRevision);
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
            throw new StaleRevisionException(assessmentId.value().toString());
        }
    }
}

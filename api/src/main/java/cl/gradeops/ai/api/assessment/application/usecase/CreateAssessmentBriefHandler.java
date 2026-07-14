package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.port.in.CreateAssessmentBriefUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

// NO @Service — declared as @Bean in AssessmentConfig (task-05)
@RequiredArgsConstructor
public class CreateAssessmentBriefHandler implements CreateAssessmentBriefUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;

    @Override
    @Transactional
    public CreateAssessmentBriefResult execute(CreateAssessmentBriefCommand command) {
        Assessment assessment = Assessment.create(command.teacherUid());
        assessmentRepository.save(assessment);

        AssessmentBrief brief = AssessmentBrief.create(
                assessment.getId(), command.learningGoal(), command.topic(),
                command.level(), command.duration(), command.language());
        assessmentBriefRepository.save(brief);

        return new CreateAssessmentBriefResult(assessment.getId().value().toString());
    }
}

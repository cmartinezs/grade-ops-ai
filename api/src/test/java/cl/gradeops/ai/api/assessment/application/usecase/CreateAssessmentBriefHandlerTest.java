package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateAssessmentBriefHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentBriefRepositoryPort assessmentBriefRepository;

    CreateAssessmentBriefHandler handler() {
        return new CreateAssessmentBriefHandler(assessmentRepository, assessmentBriefRepository);
    }

    @Test
    void shouldCreateAssessmentInDraftStatusAndItsBriefLinkedById() {
        CreateAssessmentBriefCommand command = new CreateAssessmentBriefCommand(
                "uid-1", "Evaluate loops", "Java loops", "basic", "90min", "Java");

        CreateAssessmentBriefResult result = handler().execute(command);

        ArgumentCaptor<Assessment> assessmentCaptor = ArgumentCaptor.forClass(Assessment.class);
        verify(assessmentRepository).save(assessmentCaptor.capture());
        Assessment savedAssessment = assessmentCaptor.getValue();
        assertThat(savedAssessment.getTeacherUid()).isEqualTo("uid-1");
        assertThat(savedAssessment.getStatus()).isEqualTo(AssessmentStatus.DRAFT);

        ArgumentCaptor<AssessmentBrief> briefCaptor = ArgumentCaptor.forClass(AssessmentBrief.class);
        verify(assessmentBriefRepository).save(briefCaptor.capture());
        AssessmentBrief savedBrief = briefCaptor.getValue();
        assertThat(savedBrief.getAssessmentId()).isEqualTo(savedAssessment.getId());
        assertThat(savedBrief.getLearningGoal()).isEqualTo("Evaluate loops");
        assertThat(savedBrief.getTopic()).isEqualTo("Java loops");
        assertThat(savedBrief.getLevel()).isEqualTo("basic");
        assertThat(savedBrief.getDuration()).isEqualTo("90min");
        assertThat(savedBrief.getLanguage()).isEqualTo("Java");

        assertThat(result.assessmentId()).isEqualTo(savedAssessment.getId().value().toString());
    }
}

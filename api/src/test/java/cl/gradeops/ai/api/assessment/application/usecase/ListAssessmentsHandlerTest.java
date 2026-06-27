package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAssessmentsHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;

    ListAssessmentsHandler handler() {
        return new ListAssessmentsHandler(assessmentRepository);
    }

    @Test
    void shouldReturnEmptyListWhenTeacherHasNoAssessments() {
        when(assessmentRepository.findAllByTeacherId("uid-1")).thenReturn(List.of());

        List<AssessmentSummaryResult> result = handler().execute("uid-1");

        assertThat(result).isEmpty();
        verify(assessmentRepository).findAllByTeacherId("uid-1");
    }

    @Test
    void shouldReturnAssessmentsFromRepositoryWithoutTransformation() {
        AssessmentSummaryResult r1 = AssessmentSummaryResult.builder()
                .id("assess-1").title("Java Basics").status(AssessmentStatus.OPEN)
                .submissionCount(10).pendingApprovals(3).reportLink(null).build();
        AssessmentSummaryResult r2 = AssessmentSummaryResult.builder()
                .id("assess-2").title("Data Structures").status(AssessmentStatus.GRADING)
                .submissionCount(25).pendingApprovals(0)
                .reportLink("https://reports.example.com/assess-2").build();
        when(assessmentRepository.findAllByTeacherId("uid-2")).thenReturn(List.of(r1, r2));

        List<AssessmentSummaryResult> result = handler().execute("uid-2");

        assertThat(result).hasSize(2).containsExactly(r1, r2);
        verify(assessmentRepository).findAllByTeacherId("uid-2");
    }
}

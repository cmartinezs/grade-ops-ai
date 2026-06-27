package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web;

import cl.gradeops.ai.api.assessment.application.port.in.ListAssessmentsUseCase;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response.AssessmentSummaryResponse;
import cl.gradeops.ai.api.shared.infrastructure.config.security.AuthenticatedTeacher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssessmentController {

    private final ListAssessmentsUseCase listAssessmentsUseCase;

    @GetMapping("/assessments")
    public List<AssessmentSummaryResponse> listAssessments() {
        AuthenticatedTeacher teacher = (AuthenticatedTeacher)
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return listAssessmentsUseCase.execute(teacher.uid()).stream()
            .map(r -> new AssessmentSummaryResponse(
                r.id(), r.title(), r.status(),
                r.submissionCount(), r.pendingApprovals(), r.reportLink()))
            .toList();
    }
}

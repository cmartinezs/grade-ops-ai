package cl.gradeops.ai.api.assessment;

import cl.gradeops.ai.api.shared.infrastructure.config.security.AuthenticatedTeacher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/assessments")
    public List<AssessmentSummaryDto> listAssessments() {
        AuthenticatedTeacher teacher = (AuthenticatedTeacher)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return assessmentService.listForTeacher(teacher.uid());
    }
}

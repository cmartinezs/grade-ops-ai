package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web;

import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.port.in.CreateAssessmentBriefUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.ListAssessmentsUseCase;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.request.CreateAssessmentBriefRequest;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response.AssessmentSummaryResponse;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response.CreateAssessmentBriefResponse;
import cl.gradeops.ai.api.shared.infrastructure.config.security.AuthenticatedTeacher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssessmentController {

    private final ListAssessmentsUseCase listAssessmentsUseCase;
    private final CreateAssessmentBriefUseCase createAssessmentBriefUseCase;

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

    @PostMapping("/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAssessmentBriefResponse createAssessmentBrief(@Valid @RequestBody CreateAssessmentBriefRequest request) {
        AuthenticatedTeacher teacher = (AuthenticatedTeacher)
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CreateAssessmentBriefResult result = createAssessmentBriefUseCase.execute(new CreateAssessmentBriefCommand(
            teacher.uid(), request.learningGoal(), request.topic(),
            request.level(), request.duration(), request.language()));
        return new CreateAssessmentBriefResponse(result.assessmentId());
    }
}

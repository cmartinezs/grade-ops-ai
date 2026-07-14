package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web;

import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.GetCurrentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.ListDraftVersionsCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.UpdateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.port.in.CreateAssessmentBriefUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.GenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.GetCurrentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.ListAssessmentsUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.ListDraftVersionsUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.RegenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.UpdateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.request.CreateAssessmentBriefRequest;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.request.RegenerateAssessmentDraftRequest;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.request.UpdateAssessmentDraftRequest;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response.AssessmentSummaryResponse;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response.CreateAssessmentBriefResponse;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response.GenerateAssessmentDraftResponse;
import cl.gradeops.ai.api.shared.infrastructure.config.security.AuthenticatedTeacher;
import cl.gradeops.ai.api.shared.infrastructure.exception.MissingAuthenticationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssessmentController {

    private final ListAssessmentsUseCase listAssessmentsUseCase;
    private final CreateAssessmentBriefUseCase createAssessmentBriefUseCase;
    private final GenerateAssessmentDraftUseCase generateAssessmentDraftUseCase;
    private final RegenerateAssessmentDraftUseCase regenerateAssessmentDraftUseCase;
    private final UpdateAssessmentDraftUseCase updateAssessmentDraftUseCase;
    private final GetCurrentDraftUseCase getCurrentDraftUseCase;
    private final ListDraftVersionsUseCase listDraftVersionsUseCase;

    @GetMapping("/assessments")
    public List<AssessmentSummaryResponse> listAssessments() {
        AuthenticatedTeacher teacher = currentTeacher();
        return listAssessmentsUseCase.execute(teacher.uid()).stream()
            .map(r -> new AssessmentSummaryResponse(
                r.id(), r.title(), r.status(),
                r.submissionCount(), r.pendingApprovals(), r.reportLink()))
            .toList();
    }

    @PostMapping("/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAssessmentBriefResponse createAssessmentBrief(@Valid @RequestBody CreateAssessmentBriefRequest request) {
        AuthenticatedTeacher teacher = currentTeacher();
        CreateAssessmentBriefResult result = createAssessmentBriefUseCase.execute(new CreateAssessmentBriefCommand(
            teacher.uid(), request.learningGoal(), request.topic(),
            request.level(), request.duration(), request.language()));
        return new CreateAssessmentBriefResponse(result.assessmentId());
    }

    @PostMapping("/assessments/{id}/draft")
    @ResponseStatus(HttpStatus.CREATED)
    public GenerateAssessmentDraftResponse generateDraft(@PathVariable UUID id) {
        AuthenticatedTeacher teacher = currentTeacher();
        GenerateAssessmentDraftResult result = generateAssessmentDraftUseCase.execute(
            new GenerateAssessmentDraftCommand(id, teacher.uid()));
        return toResponse(result);
    }

    @PostMapping("/assessments/{id}/draft/regenerate")
    @ResponseStatus(HttpStatus.CREATED)
    public GenerateAssessmentDraftResponse regenerateDraft(@PathVariable UUID id,
                                                             @Valid @RequestBody RegenerateAssessmentDraftRequest request) {
        AuthenticatedTeacher teacher = currentTeacher();
        GenerateAssessmentDraftResult result = regenerateAssessmentDraftUseCase.execute(
            new RegenerateAssessmentDraftCommand(id, teacher.uid(), request.adjustmentNotes()));
        return toResponse(result);
    }

    @PatchMapping("/assessments/{id}/draft")
    public GenerateAssessmentDraftResponse updateDraft(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateAssessmentDraftRequest request) {
        AuthenticatedTeacher teacher = currentTeacher();
        GenerateAssessmentDraftResult result = updateAssessmentDraftUseCase.execute(new UpdateAssessmentDraftCommand(
            id, teacher.uid(), request.title(), request.context(), request.instructions(),
            request.objectives(), request.deliverables(), request.constraints()));
        return toResponse(result);
    }

    @GetMapping("/assessments/{id}/draft")
    public GenerateAssessmentDraftResponse getCurrentDraft(@PathVariable UUID id) {
        AuthenticatedTeacher teacher = currentTeacher();
        GenerateAssessmentDraftResult result = getCurrentDraftUseCase.execute(
            new GetCurrentDraftCommand(id, teacher.uid()));
        return toResponse(result);
    }

    @GetMapping("/assessments/{id}/draft/versions")
    public List<GenerateAssessmentDraftResponse> listDraftVersions(@PathVariable UUID id) {
        AuthenticatedTeacher teacher = currentTeacher();
        return listDraftVersionsUseCase.execute(new ListDraftVersionsCommand(id, teacher.uid())).stream()
            .map(this::toResponse)
            .toList();
    }

    private GenerateAssessmentDraftResponse toResponse(GenerateAssessmentDraftResult result) {
        return new GenerateAssessmentDraftResponse(
            result.draftId(), result.title(), result.context(), result.instructions(),
            result.objectives(), result.deliverables(), result.constraints(), result.versionNumber());
    }

    private AuthenticatedTeacher currentTeacher() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(Authentication::getPrincipal)
            .filter(AuthenticatedTeacher.class::isInstance)
            .map(AuthenticatedTeacher.class::cast)
            .orElseThrow(MissingAuthenticationException::new);
    }
}

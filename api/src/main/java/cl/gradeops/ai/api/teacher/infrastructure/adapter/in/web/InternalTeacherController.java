package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web;

import cl.gradeops.ai.api.teacher.application.command.ProvisionTeacherCommand;
import cl.gradeops.ai.api.teacher.application.command.UpdatePilotFlagsCommand;
import cl.gradeops.ai.api.teacher.application.port.in.ProvisionTeacherUseCase;
import cl.gradeops.ai.api.teacher.application.port.in.UpdatePilotFlagsUseCase;
import cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.request.ProvisionTeacherRequest;
import cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.request.UpdatePilotFlagsRequest;
import cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.response.ProvisionTeacherResponse;
import cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.response.UpdatePilotFlagsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/teachers")
public class InternalTeacherController {

    private final ProvisionTeacherUseCase provisionTeacherUseCase;
    private final UpdatePilotFlagsUseCase updatePilotFlagsUseCase;
    private final String webBaseUrl;

    public InternalTeacherController(
            ProvisionTeacherUseCase provisionTeacherUseCase,
            UpdatePilotFlagsUseCase updatePilotFlagsUseCase,
            @Value("${gradeops.web.base-url}") String webBaseUrl) {
        this.provisionTeacherUseCase = provisionTeacherUseCase;
        this.updatePilotFlagsUseCase = updatePilotFlagsUseCase;
        this.webBaseUrl = webBaseUrl;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProvisionTeacherResponse provision(@RequestBody ProvisionTeacherRequest request) {
        var result = provisionTeacherUseCase.execute(
            ProvisionTeacherCommand.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build()
        );
        String inviteLink = webBaseUrl + "/reset-password?code=" + result.rawCode();
        return new ProvisionTeacherResponse(result.firebaseUid(), inviteLink);
    }

    @PatchMapping("/{uid}/flags")
    public UpdatePilotFlagsResponse updateFlags(
            @PathVariable String uid,
            @RequestBody UpdatePilotFlagsRequest request) {
        var result = updatePilotFlagsUseCase.execute(
            UpdatePilotFlagsCommand.builder()
                .uid(uid)
                .planType(request.planType())
                .relatedParty(request.relatedParty())
                .offerDetails(request.offerDetails())
                .evidenceLink(request.evidenceLink())
                .setBy(request.setBy())
                .build()
        );
        return new UpdatePilotFlagsResponse(
            result.firebaseUid(), result.planType(),
            result.relatedParty(), result.flagSetAt()
        );
    }
}

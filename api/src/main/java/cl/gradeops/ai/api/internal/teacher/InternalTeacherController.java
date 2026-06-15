package cl.gradeops.ai.api.internal.teacher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class InternalTeacherController {

    private final ProvisionTeacherService provisionTeacherService;
    private final PilotFlagService pilotFlagService;

    public InternalTeacherController(ProvisionTeacherService provisionTeacherService,
                                     PilotFlagService pilotFlagService) {
        this.provisionTeacherService = provisionTeacherService;
        this.pilotFlagService = pilotFlagService;
    }

    @PostMapping("/internal/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    public ProvisionTeacherResponse provisionTeacher(@RequestBody ProvisionTeacherRequest request) {
        return provisionTeacherService.provision(request);
    }

    @PatchMapping("/internal/teachers/{uid}/flags")
    @ResponseStatus(HttpStatus.OK)
    public PilotFlagResponse patchFlags(@PathVariable String uid,
                                        @RequestBody PilotFlagRequest req) {
        return pilotFlagService.updateFlags(uid, req);
    }
}

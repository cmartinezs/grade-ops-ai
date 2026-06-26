package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Size;

public record UpdatePilotFlagsRequest(
    @Size(max = 50) String planType,
    Boolean relatedParty,
    @Size(max = 500) String offerDetails,
    @Size(max = 2048) String evidenceLink,
    @Size(max = 100) String setBy
) {}

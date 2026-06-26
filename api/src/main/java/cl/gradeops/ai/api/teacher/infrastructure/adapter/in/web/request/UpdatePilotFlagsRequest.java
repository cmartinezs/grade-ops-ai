package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.request;

public record UpdatePilotFlagsRequest(
    String planType,
    Boolean relatedParty,
    String offerDetails,
    String evidenceLink,
    String setBy
) {}

package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.response;

public record UpdatePilotFlagsResponse(
    String firebaseUid,
    String planType,
    Boolean relatedParty,
    String flagSetAt
) {}

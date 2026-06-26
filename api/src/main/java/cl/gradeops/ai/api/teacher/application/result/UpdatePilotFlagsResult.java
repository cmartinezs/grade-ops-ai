package cl.gradeops.ai.api.teacher.application.result;

import lombok.Builder;

@Builder
public record UpdatePilotFlagsResult(
    String firebaseUid,
    String planType,
    boolean relatedParty,
    String flagSetAt
) {}

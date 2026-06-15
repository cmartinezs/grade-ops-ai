package cl.gradeops.ai.api.internal.teacher;

public record PilotFlagResponse(
        String firebaseUid,
        String planType,
        boolean relatedParty,
        String flagSetAt
) {}

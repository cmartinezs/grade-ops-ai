package cl.gradeops.ai.api.internal.teacher;

public record PilotFlagRequest(
        String planType,
        Boolean relatedParty,
        String offerDetails,
        String evidenceLink,
        String setBy
) {}

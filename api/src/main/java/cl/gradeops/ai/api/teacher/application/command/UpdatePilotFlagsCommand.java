package cl.gradeops.ai.api.teacher.application.command;

import lombok.Builder;

import java.util.Objects;

@Builder
public record UpdatePilotFlagsCommand(
    String uid,
    String planType,
    Boolean relatedParty,
    String offerDetails,
    String evidenceLink,
    String setBy
) {
    public UpdatePilotFlagsCommand {
        Objects.requireNonNull(uid, "uid must not be null");
    }
}

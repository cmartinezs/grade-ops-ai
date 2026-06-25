package cl.gradeops.ai.api.auth.domain.event;

import cl.gradeops.ai.api.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetCodeUsedEvent(
    String eventId,
    Instant occurredAt,
    String teacherUid
) implements DomainEvent {

    public PasswordResetCodeUsedEvent(String teacherUid) {
        this(UUID.randomUUID().toString(), Instant.now(), teacherUid);
    }

    @Override
    public String eventType() {
        return "password_reset_code.used";
    }

    @Override
    public String aggregateId() {
        return teacherUid;
    }
}

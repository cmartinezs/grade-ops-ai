package cl.gradeops.ai.api.auth.domain.event;

import cl.gradeops.ai.api.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetCodeIssuedEvent(
    String eventId,
    Instant occurredAt,
    String teacherUid,
    Instant expiresAt
) implements DomainEvent {

    public PasswordResetCodeIssuedEvent(String teacherUid, Instant expiresAt) {
        this(UUID.randomUUID().toString(), Instant.now(), teacherUid, expiresAt);
    }

    @Override
    public String eventType() {
        return "password_reset_code.issued";
    }

    @Override
    public String aggregateId() {
        return teacherUid;
    }
}

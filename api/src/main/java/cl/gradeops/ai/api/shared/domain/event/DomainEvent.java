package cl.gradeops.ai.api.shared.domain.event;

import java.time.Instant;

public interface DomainEvent {
    String eventId();
    Instant occurredAt();
    String eventType();
    String aggregateId();
}

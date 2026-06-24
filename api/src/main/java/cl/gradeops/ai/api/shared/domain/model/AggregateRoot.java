package cl.gradeops.ai.api.shared.domain.model;

import cl.gradeops.ai.api.shared.domain.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AggregateRoot<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot() {}

    protected abstract ID id();

    protected void registerEvent(DomainEvent event) {
        Objects.requireNonNull(event, "domain event must not be null");
        domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}

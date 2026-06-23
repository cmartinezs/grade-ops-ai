package cl.gradeops.ai.api.shared.domain.model;

import cl.gradeops.ai.api.shared.domain.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot<ID> {

    protected final ID id;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(ID id) {
        this.id = id;
    }

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public ID getId() {
        return id;
    }
}

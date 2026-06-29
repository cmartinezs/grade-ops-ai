# ⚛️ TASK 02 — Create shared.domain foundation types

> **Status:** DONE
> **Workflow:** IMPLEMENT
> **Depends On:** —
> [← story file](../story-01-shared-kernel.md)

---

## Objective

Create the three net-new pure-Java types that form the hexagonal domain foundation: `AggregateRoot<ID>`, `DomainEvent`, and `DomainException`. These are the base abstractions every bounded-context domain class will extend or implement.

---

## Technical Design

- **Approach:** All three types are placed under `cl.gradeops.ai.api.shared.domain.*` with no external imports beyond `java.*`. They contain zero Spring, JPA, or Lombok annotations — domain code must be framework-agnostic. `AggregateRoot<ID>` is an abstract class (not an interface) because it carries state (the event list). `DomainEvent` is a Java `interface` (not a record) so bounded contexts can implement it with records or classes as appropriate. `DomainException` is abstract so bounded contexts must declare concrete exceptions.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/shared/domain/model/AggregateRoot.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/shared/domain/event/DomainEvent.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/shared/domain/exception/DomainException.java` ← NEW
- **Interfaces / contracts:**
  - `AggregateRoot<ID>`: generic on `ID` (the identity value object type); exposes `protected void registerEvent(DomainEvent)` and `public List<DomainEvent> pullDomainEvents()` (clears internal list and returns snapshot). The `id` field is `protected final ID`.
  - `DomainEvent`: interface with methods `String eventId()`, `java.time.Instant occurredAt()`, `String eventType()`, `String aggregateId()`.
  - `DomainException`: abstract class extending `RuntimeException` with a single `String message` constructor.
- **Design notes:** `pullDomainEvents()` must be non-destructive per caller — it returns a copy and clears the internal list atomically. Bounded contexts in stories 02–04 will call `pullDomainEvents()` after loading an aggregate to dispatch events. No events are dispatched in Story 01 itself, so the behaviour is defined but not yet exercised.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/shared/domain/model/`.
2. Create `AggregateRoot.java`:
   ```java
   package cl.gradeops.ai.api.shared.domain.model;

   import cl.gradeops.ai.api.shared.domain.event.DomainEvent;
   import java.util.ArrayList;
   import java.util.Collections;
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
   ```
3. Create directory `src/main/java/cl/gradeops/ai/api/shared/domain/event/`.
4. Create `DomainEvent.java`:
   ```java
   package cl.gradeops.ai.api.shared.domain.event;

   import java.time.Instant;

   public interface DomainEvent {
       String eventId();
       Instant occurredAt();
       String eventType();
       String aggregateId();
   }
   ```
5. Create directory `src/main/java/cl/gradeops/ai/api/shared/domain/exception/`.
6. Create `DomainException.java`:
   ```java
   package cl.gradeops.ai.api.shared.domain.exception;

   public abstract class DomainException extends RuntimeException {
       protected DomainException(String message) {
           super(message);
       }
   }
   ```
7. Run `./mvnw compile -q` to confirm no compilation errors.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `AggregateRoot.pullDomainEvents()` returns registered events and then returns empty on second call | Write a one-off inline test or verify in `HexagonalArchitectureTest` (task-08); no Spring context needed |
| 2 | `DomainEvent` has all 4 methods | Confirmed by compilation: any class implementing the interface without all 4 methods will fail to compile |
| 3 | `DomainException` extends `RuntimeException` | `./mvnw compile` passes; confirmed by reading the file |
| 4 | No Spring/JPA imports in any of the 3 files | `grep -r "springframework\|jakarta.persistence" src/main/java/cl/gradeops/ai/api/shared/domain/` returns 0 matches |

---

## Done Criteria

- [ ] `src/main/java/cl/gradeops/ai/api/shared/domain/model/AggregateRoot.java` exists, is generic on `<ID>`, has `registerEvent(DomainEvent)` and `pullDomainEvents()` methods
- [ ] `src/main/java/cl/gradeops/ai/api/shared/domain/event/DomainEvent.java` exists as an interface with methods `eventId()`, `occurredAt()`, `eventType()`, `aggregateId()`
- [ ] `src/main/java/cl/gradeops/ai/api/shared/domain/exception/DomainException.java` exists as an abstract class extending `RuntimeException`
- [ ] Zero imports of `org.springframework.*`, `jakarta.persistence.*`, or `jakarta.validation.*` in any of the 3 files
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-01-shared-kernel.md)

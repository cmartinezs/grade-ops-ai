# 02 — DDD táctico

## Objetivo

Usar DDD táctico para modelar el negocio de GradeOps AI de forma expresiva, testeable y resistente al cambio.

No todo requiere DDD pesado. DDD se justifica cuando hay reglas, invariantes, transiciones de estado o lenguaje de negocio relevante.

## Aggregate Root

Un aggregate root representa una frontera de consistencia transaccional. Es el único objeto que debe modificar el estado interno del aggregate.

Ejemplos candidatos en GradeOps AI:

- `Assessment`.
- `Rubric`.
- `Submission`.
- `GradingSession`.
- `FeedbackBatch`.
- `CreditWallet`.
- `AgentExecution`.

### Reglas

- El aggregate root protege invariantes.
- El estado interno no se modifica desde fuera con setters públicos.
- Las operaciones del negocio son métodos con nombres del lenguaje ubicuo.
- El aggregate emite domain events cuando ocurre algo significativo.
- El aggregate no llama repositories, APIs, servicios Spring ni proveedores externos.

### Ejemplo

```java
public final class Assessment extends AggregateRoot<AssessmentId> {

  private final AssessmentId id;
  private final TeacherId teacherId;
  private final CourseId courseId;
  private AssessmentStatus status;
  private RubricId rubricId;
  private final List<LearningObjective> learningObjectives;

  private Assessment(
      AssessmentId id,
      TeacherId teacherId,
      CourseId courseId,
      List<LearningObjective> learningObjectives
  ) {
    this.id = requireNonNull(id, "id is required");
    this.teacherId = requireNonNull(teacherId, "teacherId is required");
    this.courseId = requireNonNull(courseId, "courseId is required");
    this.learningObjectives = List.copyOf(learningObjectives);
    this.status = AssessmentStatus.DRAFT;
  }

  public static Assessment draft(
      AssessmentId id,
      TeacherId teacherId,
      CourseId courseId,
      List<LearningObjective> learningObjectives
  ) {
    if (learningObjectives.isEmpty()) {
      throw new InvalidAssessmentException("An assessment requires at least one learning objective");
    }
    Assessment assessment = new Assessment(id, teacherId, courseId, learningObjectives);
    assessment.registerEvent(new AssessmentDraftCreatedEvent(id, teacherId, courseId));
    return assessment;
  }

  public void attachRubric(RubricId rubricId) {
    if (status != AssessmentStatus.DRAFT) {
      throw new InvalidAssessmentStateException("Only draft assessments can attach a rubric");
    }
    this.rubricId = requireNonNull(rubricId, "rubricId is required");
    registerEvent(new AssessmentRubricAttachedEvent(id, rubricId));
  }

  public void publish() {
    if (rubricId == null) {
      throw new InvalidAssessmentStateException("Assessment cannot be published without rubric");
    }
    this.status = AssessmentStatus.PUBLISHED;
    registerEvent(new AssessmentPublishedEvent(id));
  }

  @Override
  public AssessmentId id() {
    return id;
  }
}
```

## Base `AggregateRoot`

```java
public abstract class AggregateRoot<ID> {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  public abstract ID id();

  protected final void registerEvent(DomainEvent event) {
    domainEvents.add(requireNonNull(event, "event is required"));
  }

  public final List<DomainEvent> pullDomainEvents() {
    List<DomainEvent> events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }
}
```

## Value Object

Un value object representa un concepto identificado por su valor, no por una identidad técnica.

Ejemplos:

- `AssessmentId`.
- `RubricId`.
- `Score`.
- `ScoreRange`.
- `Percentage`.
- `CreditAmount`.
- `LearningObjective`.
- `PromptHash`.
- `AgentModelName`.
- `TenantId`.

### Reglas

- Debe ser inmutable.
- Debe validar sus invariantes al construirlo.
- Debe tener igualdad por valor.
- Puede ser `record` si la validación es simple.
- No debe tener setters.
- No debe representar DTOs de entrada/salida.

### Ejemplo con `record`

```java
public record Score(BigDecimal value) {

  public Score {
    requireNonNull(value, "value is required");
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("score cannot be negative");
    }
  }

  public boolean isPassing(Score minimum) {
    return value.compareTo(minimum.value) >= 0;
  }
}
```

### Ejemplo con clase

```java
public final class CreditAmount {

  private final long value;

  private CreditAmount(long value) {
    if (value < 0) {
      throw new IllegalArgumentException("credits cannot be negative");
    }
    this.value = value;
  }

  public static CreditAmount of(long value) {
    return new CreditAmount(value);
  }

  public CreditAmount plus(CreditAmount other) {
    return new CreditAmount(this.value + other.value);
  }

  public CreditAmount minus(CreditAmount other) {
    if (this.value < other.value) {
      throw new InsufficientCreditsException();
    }
    return new CreditAmount(this.value - other.value);
  }

  public long value() {
    return value;
  }
}
```

## Domain Event

Un domain event representa algo que ya ocurrió dentro del dominio.

Nombrar en pasado:

- `AssessmentPublishedEvent`.
- `SubmissionReceivedEvent`.
- `SubmissionGradedEvent`.
- `FeedbackApprovedEvent`.
- `CreditsConsumedEvent`.
- `AgentExecutionCompletedEvent`.

### Reglas

- Nombre en pasado.
- Inmutable.
- Debe contener IDs, no aggregates completos.
- Debe incluir `eventId` y `occurredAt`.
- Debe contener datos necesarios para reaccionar, no payloads gigantes.
- No debe contener entidades JPA.
- No debe contener objetos de request HTTP.

### Contrato base

```java
public interface DomainEvent {

  UUID eventId();

  Instant occurredAt();

  String eventType();

  String aggregateId();
}
```

### Ejemplo

```java
public record SubmissionGradedEvent(
    UUID eventId,
    Instant occurredAt,
    SubmissionId submissionId,
    AssessmentId assessmentId,
    TeacherId teacherId,
    Score finalScore
) implements DomainEvent {

  public SubmissionGradedEvent(
      SubmissionId submissionId,
      AssessmentId assessmentId,
      TeacherId teacherId,
      Score finalScore
  ) {
    this(UUID.randomUUID(), Instant.now(), submissionId, assessmentId, teacherId, finalScore);
  }

  @Override
  public String eventType() {
    return "submission.graded";
  }

  @Override
  public String aggregateId() {
    return submissionId.value().toString();
  }
}
```

## Publicación de eventos

El dominio solo registra eventos. La aplicación decide cuándo publicarlos.

Flujo recomendado:

1. Caso de uso carga aggregate.
2. Ejecuta comportamiento de dominio.
3. Persiste aggregate.
4. Extrae eventos con `pullDomainEvents()`.
5. Guarda eventos en outbox o publica mediante puerto.
6. Confirma transacción.

Evitar publicar eventos desde el constructor, desde JPA callbacks o desde entidades JPA.

## Domain Service

Usar un domain service cuando la regla de negocio:

- No pertenece naturalmente a un único aggregate.
- Requiere comparar varios objetos de dominio.
- Es una política del negocio, no un detalle técnico.

Ejemplos:

- `ScoreCalculationService`.
- `RubricConsistencyService`.
- `LearningGapDetectionService`.
- `AssessmentDifficultyPolicy`.

No usar `DomainService` como sinónimo de `@Service` de Spring.

## Repository

En DDD, un repository abstrae una colección de aggregates. En GradeOps AI, para mantener la aplicación explícita, la interfaz vivirá por defecto en:

```text
application.port.out
```

Ejemplo:

```java
public interface AssessmentRepositoryPort {

  Optional<Assessment> findById(AssessmentId assessmentId);

  Assessment save(Assessment assessment);

  boolean existsByTitleForCourse(AssessmentTitle title, CourseId courseId);
}
```

La implementación vive en infraestructura:

```text
infrastructure.adapter.out.persistence
```

Si en una feature el repository es parte fundamental del lenguaje de dominio, se puede ubicar una abstracción en `domain`, pero debe justificarse en la decisión técnica.

## Entidades de dominio vs entidades JPA

No son lo mismo.

| Tipo | Ubicación | Propósito |
|---|---|---|
| Domain Entity | `domain.model` | Modela comportamiento e identidad del negocio. |
| JPA Entity | `infrastructure.adapter.out.persistence` | Modela persistencia relacional. |

Regla: no exponer entidades JPA fuera del adapter de persistencia.

## Anti-patterns DDD

Evitar:

- Anemic domain model: aggregates con solo getters/setters.
- `@Entity` como objeto de dominio central.
- Reglas de negocio distribuidas en controllers.
- Reglas de negocio escondidas en mappers.
- `DomainEvent` usado como comando.
- Aggregates enormes que cargan todo el grafo.
- Value objects sin validación.
- Estados representados solo con `String`.

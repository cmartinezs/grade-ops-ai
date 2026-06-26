# 05 — Patrones GoF y patrones asociados

## Criterio general

Un patrón se usa para resolver una presión de diseño real. No se usa para adornar el código.

Cada patrón debe mejorar al menos una de estas dimensiones:

- Claridad del flujo.
- Testabilidad.
- Sustitución de proveedores.
- Encapsulamiento de reglas.
- Reducción de acoplamiento.
- Control de variabilidad.

## Factory Method / Factory

### Usar cuando

- Crear un aggregate requiere validar invariantes.
- La construcción depende de tipo, plan, tenant o contexto.
- No quieres exponer constructores complejos.

### En GradeOps AI

- `AssessmentFactory`.
- `RubricFactory`.
- `FeedbackDraftFactory`.
- `AgentPromptFactory`.

### Ejemplo

```java
public class AssessmentFactory {

  public Assessment createDraft(CreateAssessmentCommand command) {
    return Assessment.draft(
        AssessmentId.newId(),
        command.teacherId(),
        command.courseId(),
        command.learningObjectives().stream()
            .map(LearningObjective::of)
            .toList()
    );
  }
}
```

### Evitar cuando

- El constructor o factory method estático es suficiente.
- Solo envuelve `new` sin agregar intención.

## Builder

### Usar cuando

- Hay objetos de entrada/salida con varios campos.
- Mejora legibilidad en tests.
- Se construyen `Command`, `Result`, DTOs o contextos de workflow.

### En GradeOps AI

- `GenerateAssessmentCommand`.
- `GradeSubmissionContext`.
- `AgentExecutionLogEntry`.
- Responses API.

### Evitar cuando

- Puede saltarse invariantes del aggregate.
- Se usa en entidades JPA sin necesidad.
- Se reemplaza validación de negocio por setters encadenados.

## Strategy

### Usar cuando

Hay variación real de algoritmo.

### En GradeOps AI

- `GradingStrategy` por tipo de pregunta.
- `CreditConsumptionStrategy` por plan.
- `PromptRenderingStrategy` por proveedor AI.
- `FeedbackToneStrategy` por nivel del estudiante.

### Ejemplo

```java
public interface GradingStrategy {

  boolean supports(AssessmentType type);

  GradingResult grade(Submission submission, Rubric rubric);
}
```

## Adapter

### Usar siempre en infraestructura

Todo proveedor externo entra o sale mediante adapter.

### En GradeOps AI

- `GeminiAssessmentGenerationAdapter`.
- `PostgresAssessmentPersistenceAdapter`.
- `StripeBillingAdapter`.
- `CloudStorageSubmissionFileAdapter`.
- `MailFeedbackNotificationAdapter`.

### Regla

El adapter implementa un puerto de aplicación. El caso de uso no conoce SDKs externos.

## Facade

### Usar cuando

- Un subsistema técnico tiene muchas operaciones.
- Se quiere exponer una interfaz simple a la aplicación.
- Se encapsula complejidad externa.

### En GradeOps AI

- `AiProviderFacade`.
- `BillingFacade`.
- `AssessmentReportExportFacade`.

### Evitar cuando

- Se transforma en un “God Service”.
- Solo reenvía llamadas sin simplificar nada.

## Decorator

### Usar cuando

Quieres agregar comportamiento transversal sin modificar la implementación base.

### En GradeOps AI

- Logging de ejecución AI.
- Medición de tokens/costos.
- Retry.
- Cache.
- Circuit breaker.
- Redacción de datos sensibles.

Ejemplo conceptual:

```java
public class LoggingAssessmentGenerationPort implements AssessmentGenerationPort {

  private final AssessmentGenerationPort delegate;
  private final AgentExecutionLogPort logPort;

  @Override
  public GeneratedAssessment generate(AssessmentPromptInput input) {
    Instant startedAt = Instant.now();
    try {
      GeneratedAssessment result = delegate.generate(input);
      logPort.recordSuccess(input, result, startedAt, Instant.now());
      return result;
    } catch (RuntimeException ex) {
      logPort.recordFailure(input, ex, startedAt, Instant.now());
      throw ex;
    }
  }
}
```

## Chain of Responsibility / Pipeline

### Usar cuando

- Hay pasos ordenados.
- Cada paso puede validar, enriquecer o cortar el flujo.
- Necesitas trazabilidad por etapa.

### En GradeOps AI

- Flujo de corrección.
- Flujo de generación de evaluación.
- Flujo de aprobación de feedback.

Ver `03-use-cases-orquestadores-y-pasos.md`.

## Command

### Usar en la capa de aplicación

Un command representa intención de ejecutar una acción.

Ejemplos:

- `GenerateAssessmentCommand`.
- `GradeSubmissionCommand`.
- `ApproveFeedbackCommand`.
- `ConsumeCreditsCommand`.

No confundir con DTO REST.

## Observer / Domain Events

### Usar cuando

Una acción de dominio debe generar reacciones desacopladas.

Ejemplos:

- Al publicar evaluación, notificar estudiantes.
- Al corregir entrega, actualizar dashboard.
- Al consumir créditos, actualizar billing.
- Al completar agente, registrar auditoría.

Usar outbox si el evento debe sobrevivir fallos de publicación.

## State

### Usar cuando

Un aggregate tiene transiciones de estado complejas.

### En GradeOps AI

- `AssessmentStatus`: `DRAFT`, `READY_FOR_REVIEW`, `PUBLISHED`, `CLOSED`, `ARCHIVED`.
- `SubmissionStatus`: `RECEIVED`, `ANALYZING`, `GRADED`, `REVIEWED`, `RETURNED`.
- `FeedbackStatus`: `DRAFT`, `APPROVED`, `SENT`, `REJECTED`.

Si las transiciones son simples, un enum con métodos de validación es suficiente.

## Specification

No es GoF, pero es útil en DDD.

### Usar cuando

- Una regla booleana se reutiliza.
- La regla es parte del lenguaje de negocio.
- Necesitas componer reglas.

Ejemplo:

```java
public interface Specification<T> {

  boolean isSatisfiedBy(T candidate);
}
```

Aplicaciones:

- `AssessmentCanBePublishedSpecification`.
- `SubmissionEligibleForAutoGradingSpecification`.
- `TeacherCanConsumeCreditsSpecification`.

## Template Method

### Usar con cuidado

Puede ser útil para esqueletos de algoritmos estables, pero en Java/Spring suele ser más flexible usar composición con Strategy o Pipeline.

Evitar jerarquías profundas.

## Singleton

### Evitar implementarlo manualmente

Spring ya administra singletons por defecto. No crear singletons manuales salvo casos excepcionales y justificados.

## Repository

No es GoF, pero es estándar en DDD.

Regla:

- La interfaz vive en `application.port.out` por defecto.
- La implementación vive en `infrastructure.adapter.out.persistence`.
- Retorna dominio, no JPA entities.

## Mapper

No es GoF, pero es necesario.

Reglas:

- Mapper no contiene reglas de negocio.
- Mapper no llama repositories.
- Mapper no llama servicios externos.
- Mapper convierte entre modelos de capas.

Ejemplos:

- `AssessmentRequestMapper`.
- `AssessmentPersistenceMapper`.
- `AssessmentResponseMapper`.

## Anti-pattern: patrón innecesario

Evitar estructuras como:

```text
CreateAssessmentUseCase
CreateAssessmentUseCaseImpl
CreateAssessmentService
CreateAssessmentManager
CreateAssessmentProcessor
CreateAssessmentExecutor
CreateAssessmentFacade
```

Si todos hacen lo mismo, sobran.

Regla: cada clase debe tener una responsabilidad diferenciable en una frase.

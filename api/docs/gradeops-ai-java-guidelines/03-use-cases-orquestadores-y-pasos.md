# 03 — Use cases, orquestadores y pasos

## Objetivo

Estandarizar cómo se implementa una feature según su tamaño real.

GradeOps AI debe evitar dos extremos:

1. Un `Service` gigante con todo adentro.
2. Una microarquitectura ceremonial para una operación simple.

## Regla base

Cada acción de negocio expuesta debe tener un caso de uso explícito.

Ejemplos:

- `CreateAssessmentUseCase`.
- `GenerateRubricUseCase`.
- `SubmitAnswerUseCase`.
- `GradeSubmissionUseCase`.
- `ApproveFeedbackUseCase`.
- `ConsumeCreditsUseCase`.

## Patrón mínimo

```text
application
├── port
│   └── in
│       └── CreateAssessmentUseCase.java
├── command
│   └── CreateAssessmentCommand.java
├── result
│   └── CreateAssessmentResult.java
└── usecase
    └── CreateAssessmentHandler.java
```

### Contrato

```java
public interface CreateAssessmentUseCase {

  CreateAssessmentResult execute(CreateAssessmentCommand command);
}
```

### Command

```java
@Builder
public record CreateAssessmentCommand(
    TeacherId teacherId,
    CourseId courseId,
    String title,
    List<String> learningObjectives,
    DifficultyLevel difficultyLevel
) {

  public CreateAssessmentCommand {
    requireNonNull(teacherId, "teacherId is required");
    requireNonNull(courseId, "courseId is required");
    requireNonNull(title, "title is required");
    learningObjectives = List.copyOf(learningObjectives);
  }
}
```

### Result

```java
@Builder
public record CreateAssessmentResult(
    AssessmentId assessmentId,
    AssessmentStatus status
) {
}
```

### Handler

```java
// NO @Service — registrado como @Bean en AssessmentConfig
@RequiredArgsConstructor
public class CreateAssessmentHandler implements CreateAssessmentUseCase {

  private final AssessmentRepositoryPort assessmentRepository;
  private final DomainEventPublisherPort domainEventPublisher;

  @Override
  @Transactional
  public CreateAssessmentResult execute(CreateAssessmentCommand command) {
    Assessment assessment = Assessment.draft(
        AssessmentId.newId(),
        command.teacherId(),
        command.courseId(),
        toLearningObjectives(command.learningObjectives())
    );

    Assessment saved = assessmentRepository.save(assessment);
    domainEventPublisher.publish(saved.pullDomainEvents());

    return CreateAssessmentResult.builder()
        .assessmentId(saved.id())
        .status(saved.status())
        .build();
  }
}
```

## Nivel 1: Caso de uso simple

Usar un handler directo cuando:

- Hay una operación clara.
- Participan hasta 3 puertos o servicios.
- No hay ramas complejas.
- No hay workflow largo.
- No hay coordinación con agentes AI múltiples.

Ejemplos:

- Crear curso.
- Publicar evaluación.
- Aprobar feedback.
- Registrar entrega simple.

Estructura:

```text
<Verb><Object>UseCase
<Verb><Object>Handler
<Verb><Object>Command
<Verb><Object>Result
```

## Nivel 2: Use case con orquestador

Usar orquestador cuando:

- La feature coordina 4 o más colaboradores.
- Hay decisiones de flujo relevantes.
- Se combinan dominio, proveedor AI, créditos, auditoría y persistencia.
- El handler empieza a crecer demasiado.
- Se necesita testear la coordinación por separado.

Ejemplo:

```text
application
├── usecase
│   └── GenerateAssessmentHandler.java
├── orchestrator
│   └── GenerateAssessmentOrchestrator.java
├── command
│   └── GenerateAssessmentCommand.java
└── result
    └── GenerateAssessmentResult.java
```

El handler queda delgado:

```java
// NO @Service — registrado como @Bean en AssessmentConfig
@RequiredArgsConstructor
public class GenerateAssessmentHandler implements GenerateAssessmentUseCase {

  private final GenerateAssessmentOrchestrator orchestrator;

  @Override
  @Transactional
  public GenerateAssessmentResult execute(GenerateAssessmentCommand command) {
    return orchestrator.generate(command);
  }
}
```

El orquestador coordina:

```java
// NO @Component — registrado como @Bean en AssessmentConfig
@RequiredArgsConstructor
public class GenerateAssessmentOrchestrator {

  private final CreditConsumptionPort creditConsumptionPort;
  private final AssessmentGenerationPort assessmentGenerationPort;
  private final RubricValidationPort rubricValidationPort;
  private final AssessmentRepositoryPort assessmentRepository;
  private final AgentExecutionLogPort agentExecutionLogPort;

  public GenerateAssessmentResult generate(GenerateAssessmentCommand command) {
    creditConsumptionPort.reserve(command.teacherId(), CreditUse.ASSESSMENT_GENERATION);

    GeneratedAssessment generated = assessmentGenerationPort.generate(command.toPromptInput());
    ValidatedRubric rubric = rubricValidationPort.validate(generated.rubric());

    Assessment assessment = Assessment.fromGeneratedContent(
        AssessmentId.newId(),
        command.teacherId(),
        command.courseId(),
        generated,
        rubric
    );

    Assessment saved = assessmentRepository.save(assessment);
    agentExecutionLogPort.recordFrom(generated, rubric);

    return GenerateAssessmentResult.from(saved);
  }
}
```

## Nivel 3: Workflow con pasos

Usar pipeline de pasos cuando:

- La feature tiene un flujo largo.
- Cada etapa tiene validación, logging, compensación o retry.
- Participan varios agentes AI.
- Se requiere trazabilidad por etapa.
- Se necesita activar/desactivar pasos por plan, tenant o configuración.
- Hay ejecución asíncrona o reanudable.

Ejemplo: corrección completa de una entrega.

```text
application
├── orchestrator
│   └── GradeSubmissionOrchestrator.java
├── step
│   ├── LoadSubmissionStep.java
│   ├── ValidateRubricStep.java
│   ├── AnalyzeSubmissionStep.java
│   ├── CalculateScoreStep.java
│   ├── GenerateFeedbackStep.java
│   ├── DetectLearningGapsStep.java
│   ├── PersistGradingStep.java
│   └── EmitEventsStep.java
└── workflow
    └── GradeSubmissionContext.java
```

### Step contract

```java
public interface GradeSubmissionStep {

  GradeSubmissionContext execute(GradeSubmissionContext context);
}
```

### Context

```java
@Builder(toBuilder = true)
public record GradeSubmissionContext(
    SubmissionId submissionId,
    AssessmentId assessmentId,
    TeacherId teacherId,
    Submission submission,
    Rubric rubric,
    AgentAnalysis agentAnalysis,
    Score finalScore,
    FeedbackDraft feedbackDraft,
    List<DomainEvent> events
) {
}
```

### Orchestrator

```java
// NO @Component — registrado como @Bean en GradingConfig
@RequiredArgsConstructor
public class GradeSubmissionOrchestrator {

  private final List<GradeSubmissionStep> steps;

  public GradeSubmissionResult execute(GradeSubmissionCommand command) {
    GradeSubmissionContext context = GradeSubmissionContext.builder()
        .submissionId(command.submissionId())
        .assessmentId(command.assessmentId())
        .teacherId(command.teacherId())
        .events(List.of())
        .build();

    for (GradeSubmissionStep step : steps) {
      context = step.execute(context);
    }

    return GradeSubmissionResult.from(context);
  }
}
```

## Cuándo NO usar orquestador

No usar orquestador cuando:

- Solo hay una llamada a repository.
- Solo se actualiza un estado simple.
- El flujo cabe de forma legible en un handler de menos de 40 líneas.
- No hay colaboración compleja.

## Convención de nombres

| Elemento | Convención | Ejemplo |
|---|---|---|
| Use case port | `<Verb><Object>UseCase` | `GradeSubmissionUseCase` |
| Handler | `<Verb><Object>Handler` | `GradeSubmissionHandler` |
| Command | `<Verb><Object>Command` | `GradeSubmissionCommand` |
| Query | `<Verb><Object>Query` | `FindAssessmentByIdQuery` |
| Result | `<Verb><Object>Result` | `GradeSubmissionResult` |
| Orchestrator | `<Verb><Object>Orchestrator` | `GradeSubmissionOrchestrator` |
| Step | `<Verb><Object>Step` | `GenerateFeedbackStep` |
| Context | `<Verb><Object>Context` | `GradeSubmissionContext` |

## Transacciones

Regla por defecto:

- La transacción se abre en el handler de aplicación.
- El orquestador participa dentro de esa transacción cuando el flujo es corto y síncrono.
- Si hay llamadas remotas lentas a IA, evaluar reservar créditos y registrar estado antes, ejecutar llamada fuera de transacción larga y persistir resultado después.

Evitar transacciones largas alrededor de:

- Gemini/OpenAI/Claude.
- Storage externo.
- Email.
- APIs de terceros.
- Procesos batch.

## Idempotencia

Los casos de uso que reciben eventos, callbacks o peticiones reintentables deben ser idempotentes.

Usar:

- `requestId`.
- `idempotencyKey`.
- `externalEventId`.
- `agentExecutionId`.

Ejemplo:

```java
public record GradeSubmissionCommand(
    SubmissionId submissionId,
    TeacherId teacherId,
    UUID idempotencyKey
) {
}
```

## Errores

- El dominio lanza excepciones de dominio.
- La aplicación traduce errores de puertos a errores de caso de uso.
- La API traduce errores a HTTP.
- La infraestructura traduce errores técnicos a excepciones propias del adapter.

No lanzar `ResponseStatusException` desde dominio o aplicación.

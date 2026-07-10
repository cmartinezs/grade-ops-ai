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

## Contratos públicos entre artifacts y agentes

Cuando un `Command` o `Result` cruza artifacts o procesos, por ejemplo `api/`
llamando a `agents/`, tratarlo como contrato público estable, no como DTO
interno descartable.

Reglas:

- Mantener el contrato inmutable; si contiene colecciones, hacer copia defensiva
  con `List.copyOf`, `Set.copyOf` o equivalente, normalizando `null` a colección
  vacía en vez de lanzar.
- Nunca usar `Objects.requireNonNull` ni excepciones de la API de Java
  (`NullPointerException`, `IllegalArgumentException`, `IllegalStateException`)
  para validar campos obligatorios — la regla de
  `12-excepciones-y-manejo-de-errores.md` aplica también a estos contratos.
  Si el contrato necesita rechazar un valor inválido en su propio constructor,
  usar la excepción propia de la capa/artifact que lo declara (en `api/`, una
  subclase de `DomainException`/`ApplicationException`; en `agents/`, la
  excepción propia del agente, p. ej. `AssessmentAgentException`).
- Si el `Result` es la salida de un proceso no confiable (por ejemplo la
  respuesta estructurada de un LLM deserializada por Spring AI), no validar
  campos obligatorios en el constructor compacto del `record`: un valor
  ausente ahí no es un bug de quien construye el objeto, es un dato esperado
  que debe evaluarse explícitamente. Dejar que el paso dedicado del pipeline
  (p. ej. `validateOutput`) sea el único punto que rechaza campos ausentes,
  con la excepción propia del artifact. El constructor compacto se limita a
  garantizar inmutabilidad (copia defensiva de colecciones).
- Modelar estados opcionales de forma coherente: si dos campos opcionales
  representan un mismo modo de ejecución, deben venir juntos o rechazarse.
- Evitar acoplar contratos públicos a Spring, JPA, Jackson o Bean Validation.
  Las anotaciones de framework pertenecen a adapters o DTOs de entrada/salida,
  no al contrato compartido entre artifacts.
- Fijar nombres de campos contra el consumidor real antes de implementar. Si un
  documento aspiracional y una historia atomizada discrepan, registrar la
  inconsistencia y declarar cuál fuente manda para ese slice.
- En `agents/`, se permite ubicar el `Command`/`Result` en el package público de
  la feature cuando actúa como fachada estable del agente. La implementación
  interna del agente debe seguir separando aplicación, dominio e infraestructura
  cuando aparezcan handlers, puertos, adapters o prompts.

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

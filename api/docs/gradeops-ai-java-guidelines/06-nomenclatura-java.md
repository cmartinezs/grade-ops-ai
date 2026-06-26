# 06 — Nomenclatura Java

## Idioma del código

El código fuente se escribe en inglés.

Permitido en español solo en:

- Textos visibles al usuario final si el producto lo requiere.
- Mensajes de validación localizados.
- Documentación de negocio en español.

No usar nombres mezclados:

```java
// No
crearAssessment()
obtenerRubrica()
calcularNotaFinal()
```

Usar:

```java
createAssessment()
findRubric()
calculateFinalScore()
```

## Packages

Todo en minúsculas, sin guiones, sin underscores.

```text
ai.gradeops.api.assessment.domain.model
ai.gradeops.api.assessment.application.usecase
ai.gradeops.api.assessment.infrastructure.adapter.out.persistence
```

No usar:

```text
ai.gradeops.API.Assessment
ai.gradeops.assessment_domain
ai.gradeops.grade-ops-ai
```

## Features

Feature package en singular cuando representa un bounded context o capacidad:

```text
assessment
rubric
submission
grading
feedback
billing
audit
```

Usar plural solo si el lenguaje del dominio lo exige.

## Clases por responsabilidad

| Tipo | Sufijo / Forma | Ejemplo |
|---|---|---|
| Aggregate Root | Nombre de negocio | `Assessment` |
| Domain Entity | Nombre de negocio | `RubricCriterion` |
| Value Object | Nombre de concepto | `AssessmentId`, `Score`, `CreditAmount` |
| Domain Event | Pasado + `Event` | `SubmissionGradedEvent` |
| Domain Service | Concepto + `Service` | `ScoreCalculationService` |
| Policy | Concepto + `Policy` | `CreditConsumptionPolicy` |
| Specification | Concepto + `Specification` | `AssessmentCanBePublishedSpecification` |
| Use case port | Verbo + objeto + `UseCase` | `GenerateAssessmentUseCase` |
| Handler | Verbo + objeto + `Handler` | `GenerateAssessmentHandler` |
| Command | Verbo + objeto + `Command` | `GenerateAssessmentCommand` |
| Query | Verbo + objeto + `Query` | `FindAssessmentByIdQuery` |
| Result | Verbo + objeto + `Result` | `GenerateAssessmentResult` |
| Port Out | Capacidad + `Port` | `AssessmentGenerationPort` |
| Repository Port | Aggregate + `RepositoryPort` | `AssessmentRepositoryPort` |
| Adapter | Tecnología + capacidad + `Adapter` | `GeminiAssessmentGenerationAdapter` |
| Controller | Recurso + `Controller` | `AssessmentController` |
| Request DTO | Acción + `Request` | `GenerateAssessmentRequest` |
| Response DTO | Acción/Recurso + `Response` | `AssessmentResponse` |
| JPA Entity | Aggregate + `JpaEntity` | `AssessmentJpaEntity` |
| Spring Data Repo | Entity + `Repository` | `AssessmentJpaRepository` |
| Mapper | Contexto + `Mapper` | `AssessmentPersistenceMapper` |
| Config | Tecnología + `Config` | `OpenApiConfig` |
| Exception | Error + `Exception` | `InvalidAssessmentStateException` |

## Métodos

Usar verbos claros.

### Operaciones de creación

```java
createAssessment()
generateRubric()
submitAnswer()
```

### Búsqueda

```java
findById()
findByTeacherId()
searchAssessments()
existsByTitle()
```

Regla:

- `find...` retorna `Optional<T>` cuando puede no existir.
- `get...` asume existencia o acceso directo a propiedad.
- `require...` puede lanzar excepción si no existe.

Ejemplo:

```java
Optional<Assessment> findById(AssessmentId id);
Assessment requireById(AssessmentId id);
```

### Comandos de dominio

Deben expresar intención de negocio:

```java
assessment.attachRubric(rubricId);
assessment.publish();
submission.markAsGraded(score);
feedback.approve(reviewerId);
wallet.consume(creditAmount, reason);
```

Evitar setters como flujo de negocio:

```java
assessment.setStatus(PUBLISHED);
submission.setScore(score);
```

## Variables y parámetros

### Nombres de clases: siempre importar, nunca calificar

Nunca usar el nombre completamente calificado de una clase directamente en declaraciones de variables, parámetros de métodos o tipos de retorno. Siempre importar la clase en la sección de imports.

No:

```java
public cl.gradeops.ai.api.assessment.domain.model.Assessment findById(
        cl.gradeops.ai.api.shared.domain.model.AssessmentId id) {
    cl.gradeops.ai.api.assessment.domain.model.Assessment assessment = repository.findById(id);
    return assessment;
}
```

Sí:

```java
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.shared.domain.model.AssessmentId;

public Assessment findById(AssessmentId id) {
    Assessment assessment = repository.findById(id);
    return assessment;
}
```

La única excepción aceptada es cuando dos clases de paquetes distintos tienen el mismo nombre simple y deben coexistir en el mismo archivo. En ese caso se importa la más usada y se califica la otra.

### Nombres de variables

Usar `lowerCamelCase`.

Preferir nombres completos:

```java
teacherId
assessmentId
learningObjectives
finalScore
creditAmount
```

Evitar abreviaciones ambiguas:

```java
usr
assess
rub
sub
cfg
obj
```

Abreviaciones aceptadas si son estándar en el dominio técnico:

- `id`.
- `url`.
- `uri`.
- `dto`.
- `api`.
- `jwt`.
- `http`.
- `json`.
- `csv`.
- `pdf`.

## Constantes

`UPPER_SNAKE_CASE`.

```java
private static final int MAX_LEARNING_OBJECTIVES = 10;
private static final String DEFAULT_MODEL_NAME = "gemini-default";
```

No crear interfaces solo para constantes.

## Tests

Nombre de clase:

```text
<ClassUnderTest>Test
<ClassUnderTest>IT
<ClassUnderTest>ArchitectureTest
```

Ejemplos:

```text
AssessmentTest
GenerateAssessmentHandlerTest
AssessmentControllerIT
HexagonalArchitectureTest
```

Nombre de método recomendado:

```java
@Test
void shouldPublishAssessmentWhenRubricIsAttached() {}

@Test
void shouldRejectSubmissionWhenAssessmentIsClosed() {}

@Test
void shouldConsumeCreditsWhenFeedbackIsGenerated() {}
```

Evitar nombres genéricos:

```java
test1()
createOk()
errorCase()
```

## Commits

Usar Conventional Commits:

```text
feat(assessment): add draft creation use case
fix(grading): prevent grading closed submissions
test(feedback): add approval regression tests
refactor(billing): extract credit consumption policy
docs(architecture): define package structure
```

Scopes recomendados:

```text
assessment
rubric
submission
grading
feedback
report
billing
audit
security
infra
api
agents
```

## Branches

```text
feat/assessment-generation
fix/submission-tenant-validation
refactor/grading-pipeline
chore/update-quality-gates
```

## Endpoints REST

Usar recursos en plural:

```text
POST   /api/v1/assessments
GET    /api/v1/assessments/{assessmentId}
POST   /api/v1/assessments/{assessmentId}/publish
POST   /api/v1/submissions
POST   /api/v1/submissions/{submissionId}/grade
POST   /api/v1/feedback/{feedbackId}/approve
```

No usar verbos innecesarios cuando el método HTTP basta, salvo acciones de dominio explícitas como `publish`, `grade`, `approve`, `reject`.

## Mensajes de error internos

En inglés para logs y excepciones internas:

```java
throw new InvalidAssessmentStateException("Assessment cannot be published without rubric");
```

La localización para usuario final se resuelve en la capa de presentación/API.

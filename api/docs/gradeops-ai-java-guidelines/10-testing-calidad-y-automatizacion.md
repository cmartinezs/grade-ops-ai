# 10 — Testing, calidad y automatización

## Principio

Una feature no está completa hasta que sus reglas críticas están cubiertas por pruebas automatizadas.

## Pirámide de pruebas

Orden de prioridad:

1. Tests unitarios de dominio.
2. Tests de casos de uso con fakes/mocks.
3. Tests de adapters.
4. Tests de API/controller.
5. Tests de integración con Testcontainers.
6. Tests end-to-end solo para flujos críticos.

## Domain tests

Deben ser rápidos, puros y sin Spring.

Ejemplo:

```java
class AssessmentTest {

  @Test
  void shouldPublishAssessmentWhenRubricIsAttached() {
    Assessment assessment = AssessmentTestBuilder.draft().build();
    assessment.attachRubric(RubricId.newId());

    assessment.publish();

    assertThat(assessment.status()).isEqualTo(AssessmentStatus.PUBLISHED);
    assertThat(assessment.pullDomainEvents())
        .anyMatch(event -> event instanceof AssessmentPublishedEvent);
  }

  @Test
  void shouldRejectPublishingAssessmentWithoutRubric() {
    Assessment assessment = AssessmentTestBuilder.draft().build();

    assertThatThrownBy(assessment::publish)
        .isInstanceOf(InvalidAssessmentStateException.class);
  }
}
```

## Application tests

Validan coordinación:

- Carga de aggregates.
- Invocación de puertos.
- Persistencia.
- Publicación de eventos.
- Manejo de errores.
- Idempotencia.

Usar fakes cuando sea más claro que mocks.

```java
class GenerateAssessmentHandlerTest {

  @Test
  void shouldGenerateAssessmentAndConsumeCredits() {
    // arrange
    // act
    // assert
  }
}
```

## Adapter tests

Validan integración técnica:

- Mapeos JPA.
- Queries.
- Serialización/deserialización.
- Clientes HTTP.
- Manejo de errores externos.

## Controller tests

Usar `@WebMvcTest` o equivalente para:

- Validación request.
- Status HTTP.
- Contrato response.
- Traducción de errores.
- Seguridad básica.

## Integration tests

Usar Testcontainers para:

- PostgreSQL.
- Redis si aplica.
- Brokers si aplica.
- Servicios fake HTTP si aplica.

No usar H2 como sustituto de PostgreSQL para pruebas críticas de SQL, JSONB, índices o constraints.

## Architecture tests

Usar ArchUnit para proteger la arquitectura.

Ejemplos de reglas:

- Domain no depende de Spring.
- Domain no depende de JPA.
- Controllers no acceden a repositories.
- Infrastructure no es usada por domain/application salvo excepciones controladas.
- API no retorna JPA entities.

Ejemplo conceptual:

```java
@AnalyzeClasses(packages = "ai.gradeops")
class HexagonalArchitectureTest {

  @ArchTest
  static final ArchRule domainShouldNotDependOnSpring = noClasses()
      .that().resideInAPackage("..domain..")
      .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..");
}
```

## Quality gates

Cada PR debe pasar:

- Compilación.
- Tests unitarios.
- Tests de integración necesarios.
- Checkstyle/Spotless o formatter definido.
- Static analysis si está configurado.
- Dependency check si aplica.
- ArchUnit.

## Cobertura

No perseguir cobertura vacía. Priorizar cobertura de reglas críticas.

Mínimos recomendados:

| Tipo | Mínimo |
|---|---:|
| Domain rules críticas | 90%+ |
| Application use cases críticos | 80%+ |
| Adapters complejos | 70%+ |
| Controllers públicos | 80%+ |

## Test data builders

Usar builders de test para reducir ruido.

```java
public class AssessmentTestBuilder {

  private AssessmentId id = AssessmentId.newId();
  private TeacherId teacherId = TeacherId.newId();
  private CourseId courseId = CourseId.newId();
  private List<LearningObjective> objectives = List.of(LearningObjective.of("Use loops"));

  public static AssessmentTestBuilder draft() {
    return new AssessmentTestBuilder();
  }

  public Assessment build() {
    return Assessment.draft(id, teacherId, courseId, objectives);
  }
}
```

## Qué probar obligatoriamente

### Dominio

- Happy path.
- Estado inválido.
- Invariantes.
- Eventos emitidos.
- Value objects inválidos.

### Aplicación

- Recurso no existe.
- Usuario sin permisos.
- Créditos insuficientes.
- Provider externo falla.
- Evento se publica o se guarda en outbox.
- Idempotencia.

### API

- Request válido.
- Request inválido.
- Error de dominio traducido.
- Error de autorización.
- Formato de response.

### Seguridad

- Acceso sin token.
- Acceso con rol incorrecto.
- Acceso cruzado entre tenants.
- Logs no exponen datos sensibles.

### AI Ops

- Respuesta AI malformada.
- Timeout de proveedor.
- Output con campos faltantes.
- Cost tracking registrado.
- Prompt/output sanitizado según política.

## Nombres de tests

Usar `should...When...`:

```java
shouldRejectFeedbackApprovalWhenTeacherDoesNotOwnAssessment()
shouldRecordAgentExecutionWhenRubricIsGenerated()
shouldNotLogRawStudentAnswerWhenGradingFails()
```

## Anti-patterns

- Tests que solo verifican getters/setters.
- Tests dependientes del orden de ejecución.
- Mocks excesivos que duplican implementación.
- Tests sin asserts.
- Tests que levantan Spring para probar lógica pura.
- Desactivar tests para pasar CI.

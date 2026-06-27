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

## Estructura Given-When-Then

Todo test unitario sigue estrictamente la estructura Given-When-Then con comentarios explícitos:

```java
@Test
void shouldProvisionTeacherWhenCommandIsValid() {
    // given
    ProvisionTeacherCommand command = ProvisionTeacherCommand.builder()
        .firstName("Ana")
        .lastName("García")
        .email("ana@example.com")
        .build();
    Teacher saved = Teacher.provision(TeacherId.newId(), command.firstName(),
        command.lastName(), command.email());
    when(teacherRepository.save(any())).thenReturn(saved);

    // when
    Teacher result = handler.handle(command);

    // then
    assertThat(result).isNotNull();
    assertThat(result.firstName()).isEqualTo("Ana");
    assertThat(result.lastName()).isEqualTo("García");
    assertThat(result.email()).isEqualTo("ana@example.com");
}
```

Reglas:
- `given` — preparación del estado y configuración de mocks.
- `when` — invocación del método bajo prueba, siempre en una sola línea capturada en variable.
- `then` — assertions; nunca mezclar lógica adicional aquí.

## Mockito con extensión JUnit 5

Usar `@ExtendWith(MockitoExtension.class)` en lugar de `MockitoAnnotations.openMocks(this)`. Esto activa el ciclo de vida correcto y libera recursos automáticamente.

```java
@ExtendWith(MockitoExtension.class)
class ProvisionTeacherHandlerTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private ProvisionTeacherHandler handler;

    @Test
    void shouldProvisionTeacherWhenCommandIsValid() { ... }
}
```

Reglas:
- `@Mock` — colaboradores que el sistema bajo prueba recibe por constructor o inyección.
- `@InjectMocks` — la clase que se está probando; Mockito la instancia inyectando los `@Mock`.
- No mezclar `@Mock` con stubs manuales `Mockito.mock(...)` en la misma clase de test.
- Usar `@Spy` solo cuando sea necesario espiar comportamiento real parcial y documentar el motivo.

## Estrategia de assertions exhaustivas

Cuando el método bajo prueba retorna un objeto, la secuencia de assertions debe ser:

1. **No nulo** — verificar primero que el resultado existe.
2. **Atributos no nulos** — verificar que cada campo observable no sea nulo.
3. **Valores esperados** — verificar que cada campo tenga el valor correcto según el comando o estado dado.
4. **Estado del aggregate** — si aplica, verificar status, versión u otros campos de estado.
5. **Interacciones de mocks** — verificar al final que los colaboradores fueron invocados correctamente.

```java
@Test
void shouldProvisionTeacherWhenCommandIsValid() {
    // given
    ProvisionTeacherCommand command = ProvisionTeacherCommand.builder()
        .firstName("Ana")
        .lastName("García")
        .email("ana@example.com")
        .build();
    when(teacherRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // when
    Teacher result = handler.handle(command);

    // then — 1. no nulo
    assertThat(result).isNotNull();
    // then — 2. atributos no nulos
    assertThat(result.id()).isNotNull();
    assertThat(result.firstName()).isNotNull();
    assertThat(result.lastName()).isNotNull();
    assertThat(result.email()).isNotNull();
    // then — 3. valores esperados
    assertThat(result.firstName()).isEqualTo("Ana");
    assertThat(result.lastName()).isEqualTo("García");
    assertThat(result.email()).isEqualTo("ana@example.com");
    // then — 4. estado
    assertThat(result.status()).isEqualTo(TeacherStatus.ACTIVE);
    // then — 5. interacciones
    verify(teacherRepository).save(any(Teacher.class));
    verify(eventPublisher).publish(any(TeacherProvisionedEvent.class));
}
```

No omitir la verificación de atributos porque "ya se testeó en otro test". Cada test debe ser autocontenido y verificar el contrato completo del método bajo prueba.

## Tests de métodos privados

La mejor práctica es testear métodos privados **indirectamente** a través de la API pública. Si un método privado no puede alcanzarse con cobertura completa desde la API pública, generalmente indica un problema de diseño.

### Estrategia 1 — Testing indirecto (recomendada)

Tratar el método privado como un detalle de implementación. Proveer distintos inputs al método público para activar todos los caminos, casos borde y excepciones del método privado.

```java
// El método privado `calculatePenalty(int days)` se activa indirectamente
@Test
void shouldApplyLatePenaltyWhenSubmissionIsOverdue() {
    // given — input que activa el camino del método privado
    Submission submission = Submission.builder().daysLate(5).build();

    // when
    GradeResult result = grader.grade(submission);

    // then — se valida el efecto observable, no la implementación interna
    assertThat(result.score()).isLessThan(100);
    assertThat(result.penaltyApplied()).isTrue();
}
```

Ventaja: los tests no se rompen al refactorizar, renombrar o eliminar el método privado.

### Estrategia 2 — Visibilidad package-private

Eliminar el modificador `private` para hacer el método accesible desde el mismo paquete. Colocar la clase de test en el mismo paquete bajo `src/test/java`.

```java
// Producción: src/main/java/cl/gradeops/ai/api/grading/application/GradeCalculator.java
class GradeCalculator {
    // Sin 'private': accesible desde el mismo paquete
    int applyPenalty(int score, int daysLate) {
        return score - (daysLate * 5);
    }
}

// Test: src/test/java/cl/gradeops/ai/api/grading/application/GradeCalculatorTest.java
@ExtendWith(MockitoExtension.class)
class GradeCalculatorTest {
    @Test
    void shouldReduceScoreByFivePointsPerDayLate() {
        // given
        GradeCalculator calculator = new GradeCalculator();

        // when
        int result = calculator.applyPenalty(100, 3);

        // then
        assertThat(result).isEqualTo(85);
    }
}
```

Ventaja: código de test limpio y legible, compatible con JUnit 5 sin workarounds.
Desventaja: el método queda expuesto a otras clases del mismo paquete en producción.

### Estrategia 3 — Java Reflection API

Usar solo cuando no se puede modificar la visibilidad del código de producción. El test compilará correctamente si se renombra el método, pero fallará en runtime con `NoSuchMethodException`.

```java
@Test
void shouldCalculatePenaltyViaReflection() throws Exception {
    // given
    GradeCalculator calculator = new GradeCalculator();
    Method method = GradeCalculator.class.getDeclaredMethod("applyPenalty", int.class, int.class);
    method.setAccessible(true);

    // when
    int result = (int) method.invoke(calculator, 100, 3);

    // then
    assertThat(result).isEqualTo(85);
}
```

Desventaja crítica: frágil ante refactorizaciones. Si el método cambia de nombre, el test compila pero revienta en runtime.

### Estrategia 4 — Extraer a una clase dedicada (recomendada para lógica compleja)

Si un método privado contiene lógica tan compleja que su testing indirecto es inmanejable, es una señal de que la clase viola el principio de responsabilidad única.

Pasos:
1. Extraer el método privado a una clase pública dedicada.
2. Hacer el método público dentro de esa clase.
3. Inyectar la nueva clase como colaborador.
4. Escribir tests unitarios directos para la clase extraída.

```java
// Antes: lógica compleja enterrada como método privado en GradeCalculator
// Después: extraída a su propia clase

public class PenaltyPolicy {
    public int apply(int score, int daysLate) {
        // lógica compleja previamente privada
        return Math.max(0, score - (daysLate * 5));
    }
}

// GradeCalculator ahora recibe PenaltyPolicy por constructor
// PenaltyPolicy tiene sus propios tests unitarios directos
```

Ventaja: máxima testabilidad y adherencia a SOLID. Nunca se necesita Reflection.

### Cuándo usar cada estrategia

| Estrategia | Seguridad | Mantenimiento | Cuándo usarla |
|---|---|---|---|
| Testing indirecto | Alta | Bajo | Desarrollo estándar — siempre la primera opción |
| Package-private | Media | Bajo | Cuando el indirecto no cubre todos los caminos |
| Reflection | Baja | Alto | Código legado sin posibilidad de modificar |
| Extracción a clase | Alta | Bajo | Lógica privada compleja o con múltiples branches |

## Anti-patterns

- Tests que solo verifican getters/setters.
- Tests dependientes del orden de ejecución.
- Mocks excesivos que duplican implementación.
- Tests sin asserts.
- Tests que levantan Spring para probar lógica pura.
- Desactivar tests para pasar CI.

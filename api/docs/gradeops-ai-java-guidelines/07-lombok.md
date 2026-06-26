# 07 — Uso de Lombok

## Objetivo

Lombok se usa para reducir ruido mecánico, no para ocultar el diseño.

Regla central:

> Mientras más cerca del dominio, más restrictivo debe ser el uso de Lombok.

## Permitido y recomendado

### `@Slf4j`

Usar en clases que realmente registran logs:

- Use case handlers.
- Orchestrators.
- Adapters.
- Consumers.
- Schedulers.
- Filters.

Ejemplo:

```java
// NO @Component — registrado como @Bean en AssessmentConfig
@Slf4j
@RequiredArgsConstructor
public class GeminiAssessmentGenerationAdapter implements AssessmentGenerationPort {

  @Override
  public GeneratedAssessment generate(AssessmentPromptInput input) {
    log.info("Generating assessment with provider=gemini tenantId={}", input.tenantId());
    // ...
  }
}
```

No agregar `@Slf4j` por defecto si la clase no loguea.

### `@RequiredArgsConstructor`

Recomendado para inyección por constructor en Spring.

```java
// NO @Service — registrado como @Bean en AssessmentConfig
@RequiredArgsConstructor
public class GenerateAssessmentHandler implements GenerateAssessmentUseCase {

  private final AssessmentRepositoryPort assessmentRepository;
  private final AssessmentGenerationPort assessmentGenerationPort;
}
```

Reglas:

- Dependencias `private final`.
- No usar field injection con `@Autowired`.
- No usar constructor manual salvo que agregue validación o lógica justificada.

### `@Builder`

Permitido en:

- Commands.
- Results.
- DTOs.
- Contextos de workflow.
- Objetos de test.
- Value objects complejos si no rompe invariantes.

Ejemplo:

```java
@Builder
public record GenerateAssessmentCommand(
    TeacherId teacherId,
    CourseId courseId,
    List<String> learningObjectives,
    DifficultyLevel difficultyLevel
) {
}
```

Usar `@Builder(toBuilder = true)` en contextos de pipeline cuando el objeto es inmutable y se transforma por pasos.

### `@Getter`

Permitido en:

- Objetos de dominio cuando se requiere lectura.
- Entidades JPA.
- Clases de configuración.

Preferir métodos con intención sobre getters cuando corresponde:

```java
assessment.isPublished();
wallet.hasEnoughCredits(amount);
submission.isEligibleForGrading();
```

### `@Setter`

Uso restringido.

Permitido en:

- Entidades JPA ubicadas en infraestructura.
- DTOs internos que requieran serialización/deserialización.
- Config properties.

No usar setters públicos en aggregates de dominio.

## Entidades JPA

En `@Entity`, regla obligatoria:

- Usar `@Getter` y `@Setter` si JPA/mappers lo requieren.
- No usar `@Data`.
- Agregar constructor protegido sin argumentos.
- Evitar `@EqualsAndHashCode` por defecto.
- Evitar `@ToString` con relaciones lazy o datos sensibles.

Ejemplo:

```java
@Getter
@Setter
@Entity
@Table(name = "assessments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssessmentJpaEntity {

  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID teacherId;

  @Column(nullable = false)
  private UUID courseId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AssessmentStatus status;
}
```

## `@Data`

### Prohibido en

- `@Entity`.
- Aggregates.
- Domain entities.
- Value objects con invariantes.
- Clases con relaciones lazy.
- Clases que contienen datos sensibles.

Motivos:

- Genera setters públicos.
- Genera `equals`/`hashCode` potencialmente peligrosos.
- Genera `toString` que puede exponer datos.
- Puede romper invariantes del dominio.
- Puede causar problemas con Hibernate y relaciones lazy.

### Permitido con cuidado en

- DTOs simples sin datos sensibles.
- Objetos usados solo en tests.
- Modelos internos de configuración sin invariantes.

Incluso ahí, preferir `record` o `@Getter` + `@Builder`.

## `@UtilityClass`

Usar solo para utilidades puras, sin estado y sin lógica de negocio.

Permitido:

```java
@UtilityClass
public class JsonSanitizer {

  public String maskEmail(String value) {
    // ...
  }
}
```

Prohibido:

```java
@UtilityClass
public class AssessmentRules {
  // Reglas de negocio escondidas como static methods
}
```

Las reglas de negocio deben vivir en aggregates, policies, specifications o domain services.

## `@EqualsAndHashCode`

### Value objects

Permitido y recomendado si no se usa `record`.

```java
@Getter
@EqualsAndHashCode
public final class LearningObjective {

  private final String value;
}
```

### Entidades JPA

Evitar por defecto. Si se necesita, usar solo ID estable y con cuidado.

```java
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AssessmentJpaEntity {

  @Id
  @EqualsAndHashCode.Include
  private UUID id;
}
```

No incluir relaciones.

## `@ToString`

Evitar en:

- Entidades con relaciones.
- Clases con submissions de estudiantes.
- Clases con prompts.
- Clases con tokens, credenciales o emails.

Si se usa, excluir campos sensibles.

```java
@ToString(exclude = {"rawPrompt", "studentAnswer"})
```

## `@SneakyThrows`

Prohibido.

Motivo:

- Oculta contratos de error.
- Dificulta testing.
- Empeora claridad en adapters.

## `val` y `var` de Lombok

Evitar. Usar `var` nativo de Java solo cuando el tipo sea obvio y no reduzca legibilidad.

## Reglas por capa

| Capa | Lombok recomendado | Lombok prohibido |
|---|---|---|
| Domain | `@Getter`, `@EqualsAndHashCode` en VO, `@Builder` con cuidado | `@Data`, `@Setter`, `@Slf4j` salvo excepción |
| Application | `@RequiredArgsConstructor`, `@Builder`, `@Slf4j` | `@Data` en commands críticos con invariantes débiles |
| Infrastructure | `@RequiredArgsConstructor`, `@Slf4j`, `@Getter`, `@Setter` en JPA | `@Data` en `@Entity`, `@SneakyThrows` |
| API | `@RequiredArgsConstructor`, `@Builder`, `@Getter` | `@Data` si hay datos sensibles |
| Tests | `@Builder`, `@UtilityClass` para fixtures | Abuso de builders que oculten datos importantes |

## Decisión final

Lombok debe reducir ruido, pero nunca debe borrar intención de negocio.

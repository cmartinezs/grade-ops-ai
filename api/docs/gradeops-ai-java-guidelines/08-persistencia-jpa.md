# 08 — Persistencia JPA

## Regla principal

La persistencia es un detalle de infraestructura.

Los aggregates de dominio no son entidades JPA.

## Ubicación

```text
infrastructure.adapter.out.persistence
├── AssessmentJpaEntity.java
├── AssessmentJpaRepository.java
├── AssessmentPersistenceAdapter.java
└── AssessmentPersistenceMapper.java
```

## Entity JPA

```java
@Getter
@Setter
@Entity
@Table(name = "assessments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssessmentJpaEntity {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID teacherId;

  @Column(nullable = false)
  private UUID courseId;

  @Column(nullable = false, length = 160)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private AssessmentStatus status;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;
}
```

## Repository Spring Data

```java
interface AssessmentJpaRepository extends JpaRepository<AssessmentJpaEntity, UUID> {

  Optional<AssessmentJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

  boolean existsByTenantIdAndCourseIdAndTitleIgnoreCase(UUID tenantId, UUID courseId, String title);
}
```

Mantener package-private cuando solo lo usa el adapter.

## Persistence Adapter

```java
@Component
@RequiredArgsConstructor
public class AssessmentPersistenceAdapter implements AssessmentRepositoryPort {

  private final AssessmentJpaRepository repository;
  private final AssessmentPersistenceMapper mapper;

  @Override
  public Optional<Assessment> findById(AssessmentId id) {
    return repository.findById(id.value())
        .map(mapper::toDomain);
  }

  @Override
  public Assessment save(Assessment assessment) {
    AssessmentJpaEntity entity = mapper.toEntity(assessment);
    AssessmentJpaEntity saved = repository.save(entity);
    return mapper.toDomain(saved);
  }
}
```

## Mapper de persistencia

Reglas:

- No contiene reglas de negocio.
- No llama repositories.
- No llama servicios externos.
- No decide estados.
- Convierte entre JPA y dominio.

```java
@Component
public class AssessmentPersistenceMapper {

  public Assessment toDomain(AssessmentJpaEntity entity) {
    return Assessment.restore(
        AssessmentId.of(entity.getId()),
        TenantId.of(entity.getTenantId()),
        TeacherId.of(entity.getTeacherId()),
        CourseId.of(entity.getCourseId()),
        AssessmentTitle.of(entity.getTitle()),
        entity.getStatus()
    );
  }

  public AssessmentJpaEntity toEntity(Assessment assessment) {
    AssessmentJpaEntity entity = new AssessmentJpaEntity();
    entity.setId(assessment.id().value());
    entity.setTenantId(assessment.tenantId().value());
    entity.setTeacherId(assessment.teacherId().value());
    entity.setCourseId(assessment.courseId().value());
    entity.setTitle(assessment.title().value());
    entity.setStatus(assessment.status());
    return entity;
  }
}
```

## Métodos `restore`

Cuando se reconstruye un aggregate desde persistencia, usar un factory de restauración que no emita eventos de creación.

```java
public static Assessment restore(
    AssessmentId id,
    TenantId tenantId,
    TeacherId teacherId,
    CourseId courseId,
    AssessmentTitle title,
    AssessmentStatus status
) {
  return new Assessment(id, tenantId, teacherId, courseId, title, status);
}
```

## Migraciones

Toda modificación de schema debe tener migración versionada.

Con Flyway:

```text
src/main/resources/db/migration
├── V001__create_assessments.sql
├── V002__create_submissions.sql
└── V003__create_agent_execution_logs.sql
```

Reglas:

- No editar migraciones ya aplicadas en ambientes compartidos.
- Usar nombres descriptivos.
- Incluir índices relevantes.
- Incluir restricciones `not null`, `unique`, `foreign key` cuando corresponda.
- No depender solo de validación de aplicación.

## Tenant y seguridad de datos

Toda tabla multi-tenant debe incluir `tenant_id`.

Toda query sensible debe filtrar por tenant.

Ejemplo:

```java
Optional<AssessmentJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
```

Evitar:

```java
Optional<AssessmentJpaEntity> findById(UUID id);
```

salvo en tablas globales explícitamente documentadas.

## Transacciones

Preferir `@Transactional` en aplicación:

```java
@Service
@RequiredArgsConstructor
public class PublishAssessmentHandler implements PublishAssessmentUseCase {

  @Override
  @Transactional
  public PublishAssessmentResult execute(PublishAssessmentCommand command) {
    // ...
  }
}
```

Usar `@Transactional(readOnly = true)` para queries.

Evitar:

- Transacciones en controllers.
- Transacciones largas con llamadas AI/HTTP.
- Mezclar persistencia y llamadas remotas sin estrategia de compensación.

## Outbox para eventos críticos

Usar outbox cuando un evento debe publicarse de forma confiable después de persistir.

Ejemplos:

- `SubmissionGradedEvent`.
- `FeedbackApprovedEvent`.
- `CreditsConsumedEvent`.
- `AgentExecutionCompletedEvent`.

Tabla sugerida:

```sql
create table outbox_events (
  id uuid primary key,
  aggregate_id varchar(120) not null,
  event_type varchar(160) not null,
  payload jsonb not null,
  status varchar(40) not null,
  occurred_at timestamp with time zone not null,
  published_at timestamp with time zone null,
  created_at timestamp with time zone not null
);
```

## N+1 y performance

Reglas:

- No exponer relaciones lazy a la API.
- Usar queries explícitas para read models.
- No cargar aggregate completo para listados.
- Usar proyecciones para dashboards.
- Medir antes de optimizar.

## Read models

Para reportes docentes, dashboards o búsquedas, se permite usar read models separados:

```java
public record AssessmentSummaryReadModel(
    AssessmentId assessmentId,
    String title,
    AssessmentStatus status,
    int submissionCount,
    BigDecimal averageScore
) {
}
```

Estos modelos no reemplazan el dominio para comandos.

## Auditoría

Tablas críticas deben incluir:

- `created_at`.
- `updated_at`.
- `created_by` si aplica.
- `updated_by` si aplica.
- `tenant_id` si aplica.

Para cambios auditables complejos, usar tabla/eventos de auditoría.

## Anti-patterns

- `@Entity` en `domain`.
- `JpaRepository` inyectado en use cases.
- `EntityManager` en controller.
- Relaciones bidireccionales innecesarias.
- `CascadeType.ALL` por comodidad.
- `FetchType.EAGER` por defecto.
- `@Data` en entidades.
- Guardar prompts o respuestas completas sin política de privacidad.

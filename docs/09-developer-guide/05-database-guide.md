# Database Guide

This guide covers the PostgreSQL schema, Flyway migration conventions, JPA entity mapping, and the patterns used for data access in GradeOps AI.

---

## Technology

**Database:** PostgreSQL 15+
**Migration tool:** Flyway (managed automatically by Spring Boot on startup)
**ORM:** Spring Data JPA / Hibernate

---

## Current schema

The current API schema is managed by Flyway migrations V1-V17. It includes teacher/auth records, the assessment authoring model (revisions, durable AI operations, idempotency), and the legacy tables that model superseded (see [Legacy authoring tables](#legacy-authoring-tables-read-only) below).

| Migration | Summary |
| --- | --- |
| `V1__create_teacher_table.sql` | Creates `teacher`. |
| `V2__add_pilot_flag_columns.sql` | Adds plan, related-party, offer and evidence fields. |
| `V3__add_provider_column.sql` | Adds auth provider on `teacher`. |
| `V4__split_name_into_first_last.sql` | Splits teacher name into first/last name columns. |
| `V5__add_password_reset_codes.sql` | Adds password reset code storage. |
| `V6__add_unique_constraint_prc_teacher_uid.sql` | Allows one active reset code per teacher. |
| `V7__rename_prc_code_to_raw_code.sql` | Renames reset code column. |
| `V8__add_index_prc_created_at.sql` | Adds reset-code cleanup index. |
| `V9__add_assessments.sql` | Adds `assessments`. |
| `V10__add_assessment_briefs.sql` | Adds `assessment_briefs`. |
| `V11__add_assessment_drafts.sql` | Adds versioned `assessment_drafts`. **Superseded — read-only, see below.** |
| `V12__add_agent_execution_logs.sql` | Adds `agent_execution_logs`. **Superseded — read-only, see below.** |
| `V13__add_agent_attempts_and_ai_operations.sql` | Adds `ai_operations`/`agent_attempts` — the durable AI dispatch model. |
| `V14__add_assessment_revisions.sql` | Adds `assessment_revisions` — immutable content snapshots, replaces `assessment_drafts` as the write path. |
| `V15__add_assessment_current_revision.sql` | Adds `assessments.current_revision_id`/`lock_version`. |
| `V16__add_idempotency_records.sql` | Adds `idempotency_records`. |
| `V17__backfill_legacy_authoring_data.sql` | One-time backfill of every `assessment_drafts`/`agent_execution_logs` row into `assessment_revisions`/`ai_operations`/`agent_attempts`, plus `assessment_revisions.provenance_complete`. |

### `teacher` table — V1-V4

```sql
CREATE TABLE teacher (
    firebase_uid VARCHAR(128)              NOT NULL,
    first_name   VARCHAR(255)              NOT NULL,
    last_name    VARCHAR(255)              NOT NULL DEFAULT '',
    email        VARCHAR(255)              NOT NULL,
    provider     VARCHAR(20)               NOT NULL DEFAULT 'EMAIL_PASSWORD',
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT teacher_pkey PRIMARY KEY (firebase_uid)
);

CREATE UNIQUE INDEX teacher_email_idx ON teacher (email);
```

Column reference:

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `firebase_uid` | `VARCHAR(128)` | PRIMARY KEY, NOT NULL | Firebase UID from Google Identity Platform. Used as the natural PK — no surrogate UUID needed because Firebase UIDs are stable and globally unique. |
| `first_name` | `VARCHAR(255)` | NOT NULL | Teacher first/display name. |
| `last_name` | `VARCHAR(255)` | NOT NULL, DEFAULT `''` | Teacher last name. |
| `email` | `VARCHAR(255)` | NOT NULL, unique | Email from the Firebase token. Enforced as unique by `teacher_email_idx`. |
| `provider` | `VARCHAR(20)` | NOT NULL, CHECK | `EMAIL_PASSWORD` or `GOOGLE`. |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT NOW() | Record creation timestamp. Never updated after insert. |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT NOW() | Last modification timestamp. Updated on every write to the record. |

### Columns added by V2 — pilot flags

```sql
ALTER TABLE teacher
    ADD COLUMN plan_type     VARCHAR(10)              CHECK (plan_type IN ('pilot', 'free', 'paid')),
    ADD COLUMN related_party BOOLEAN                  NOT NULL DEFAULT FALSE,
    ADD COLUMN offer_details TEXT,
    ADD COLUMN evidence_link TEXT,
    ADD COLUMN flag_set_by   VARCHAR(255),
    ADD COLUMN flag_set_at   TIMESTAMP WITH TIME ZONE;
```

Column reference:

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `plan_type` | `VARCHAR(10)` | nullable, CHECK constraint | Allowed values: `pilot`, `free`, `paid`. Null means the teacher has not been assigned a plan yet. |
| `related_party` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Marks whether the teacher is a related party (e.g., a friend, family member, or team collaborator) for transparent traction reporting. |
| `offer_details` | `TEXT` | nullable | Free-text description of the offer made to the pilot teacher. |
| `evidence_link` | `TEXT` | nullable | URL to external evidence (e.g., a screenshot, invoice, or letter of intent). |
| `flag_set_by` | `VARCHAR(255)` | nullable | Identifier of the operator who set the pilot flags. |
| `flag_set_at` | `TIMESTAMPTZ` | nullable | When the pilot flags were last set. |

---

## JPA entity mapping

Teacher persistence currently lives under `cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence`, with the domain model under `cl.gradeops.ai.api.teacher.domain.model`.

Key mapping details:

- `firebase_uid` is the primary key for `teacher`. The domain ID is the Firebase UID, not a generated database UUID.
- `@Column(name = "created_at", nullable = false, updatable = false)` — the `updatable = false` flag tells Hibernate to never emit this column in an `UPDATE` statement. Only written at insert time.
- `@Column(name = "updated_at", nullable = false)` — has a public `setUpdatedAt(OffsetDateTime)` setter. The service layer must call this explicitly before persisting. There is no automatic `@PreUpdate` hook.
- Pilot flag columns from V2 (`planType`, `relatedParty`, etc.) are all nullable in Java (`String`, `OffsetDateTime`) except `relatedParty`, which is a primitive `boolean` defaulting to `false` — matching the `NOT NULL DEFAULT FALSE` database constraint.
- All timestamps use `OffsetDateTime`, which maps cleanly to `TIMESTAMP WITH TIME ZONE` in PostgreSQL.

Assessment persistence follows the same hexagonal pattern:

| Domain concept | Persistence adapter area |
| --- | --- |
| `Assessment` | `assessment/infrastructure/adapter/out/persistence/Assessment*` |
| `AssessmentBrief` | `AssessmentBrief*` |
| `AssessmentRevision` | `AssessmentRevision*` |
| `AiOperation` | `AiOperation*` |
| `AgentAttempt` | `AgentAttempt*` |

`AssessmentDraft`/`AgentExecutionLog` domain classes and their adapters were removed (API session A4, Task 13) once every legacy row was backfilled into `AssessmentRevision`/`AiOperation`/`AgentAttempt` by `V17__backfill_legacy_authoring_data.sql`. The `assessment_drafts`/`agent_execution_logs` **tables** themselves were not dropped — see [Legacy authoring tables](#legacy-authoring-tables-read-only).

---

## Flyway conventions

### File naming

```
V{version}__{description}.sql
```

Note the **double underscore** between the version number and the description. A single underscore is not recognized by Flyway.

Examples:
- `V1__create_teacher_table.sql`
- `V2__add_pilot_flag_columns.sql`
- `V13__create_rubrics.sql`

### File location

```
api/src/main/resources/db/migration/
```

### Checksum enforcement

Flyway validates the checksum of every applied migration file on each startup. If a file is modified after it has been applied to a database, the application will fail to start with a `FlywayException`. This is intentional — it prevents silent schema drift.

**Never modify an existing migration file.** To correct a mistake in a previous migration, create a new version (for example, V13) that applies the corrective `ALTER TABLE` or `UPDATE` statement.

---

## How to add a new migration

1. **Determine the next version number.** Current migrations run through V12. The next available version is V13 unless another branch has already added it.

2. **Create the file** in `api/src/main/resources/db/migration/`:
   ```
   V13__description_of_change.sql
   ```
   Use lowercase, underscore-separated words for the description.

3. **Write additive SQL.** For the MVP, prefer additive changes — `ALTER TABLE ADD COLUMN` rather than dropping or renaming existing columns. Example:
   ```sql
   ALTER TABLE teacher
       ADD COLUMN cohort_name VARCHAR(255);
   ```

4. **Add the JPA field.** Open `TeacherEntity.java` and add the corresponding field with `@Column` annotation:
   ```java
   @Column(name = "cohort_name", length = 255)
   private String cohortName;
   ```
   Add getter and setter methods.

5. **Run the application.** Flyway applies pending migrations automatically on startup:
   ```bash
   cd api/
   ./mvnw spring-boot:run -Dspring.profiles.active=local
   ```

6. **To run migrations without starting the full app:**
   ```bash
   ./mvnw flyway:migrate
   ```

7. **Verify the result** using `psql`:
   ```bash
   psql -U gradeops -d gradeops -c "\d teacher"
   ```
   The `\d teacher` command shows the current column layout.

---

## Testing with Flyway

In the test environment, Flyway is disabled and an in-memory H2 database is used instead. This keeps tests fast and independent from migration state.

The test profile is activated via `@ActiveProfiles("test")` on test classes. Spring Boot loads `application-test.yml` (or `application-test.properties`) when this profile is active.

Recommended `application-test.yml` structure:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
  flyway:
    enabled: false
```

With `ddl-auto: create-drop`, Hibernate generates the schema from JPA entity annotations at test startup and drops it at the end. This means **H2 schema is driven by entity annotations, not migration files** — a gap to be aware of. If you add a column to a migration but forget to add it to the entity, the integration tests will not catch that mismatch.

The `AuthControllerTest` class demonstrates the full test setup:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class AuthControllerTest { ... }
```

`FirebaseTestConfig` registers a Mockito mock for `FirebaseAuth` as the primary bean, overriding the real Firebase Admin SDK initialization that would fail without valid credentials in a test environment.

---

## Implemented assessment/evidence tables

| Table | Purpose |
|-------|---------|
| `assessments` | Assessment aggregate owned by a teacher and current workflow status. `current_revision_id`/`lock_version` (V15) point at the authoritative current `assessment_revisions` row under CAS. |
| `assessment_briefs` | Teacher intake fields: learning goal, topic, level, duration, language. |
| `assessment_revisions` | **Authoritative** immutable content snapshots — `origin` (`AI_GENERATED`/`HUMAN_EDITED`/`LEGACY_UNKNOWN`), `actor_id`, `reason`, `previous_revision_id` chain, `provenance_complete`. Replaces `assessment_drafts` as the write path (V14). |
| `ai_operations` | **Authoritative** durable record of every generate/regenerate dispatch intent, independent of retries. |
| `agent_attempts` | **Authoritative** one row per concrete dispatch under an `ai_operations` row: resolved provider/model, cost, tokens, failure code. |
| `idempotency_records` | Replay records for idempotency-key-guarded mutations (24h retention, no cleanup job yet). |

### Legacy authoring tables (read-only)

| Table | Status |
|-------|--------|
| `assessment_drafts` | **Deprecated, read-only.** No application code reads or writes it (Java domain/adapter classes removed in API session A4, Task 13). Retained one release as an audit fallback per the [Assessment Authoring Model ADR](../99-decisions/2026-07-28-assessment-authoring-model.md). |
| `agent_execution_logs` | **Deprecated, read-only.** Same retention/removal status as `assessment_drafts`. |

Every row in both tables was migrated by `V17__backfill_legacy_authoring_data.sql` into `assessment_revisions`/`ai_operations`/`agent_attempts` — unconditionally labeled `origin = LEGACY_UNKNOWN`, `provenance_complete = false`, `actor_id = NULL` (the legacy schema has no edit-timestamp column, so no row can be honestly labeled `AI_GENERATED` with confidence). **New writes to either legacy table are prohibited** — there is no code path left that can produce one. Physical `DROP TABLE` is deferred to a future, unscheduled release, once the backfilled data has been spot-checked in the target environment (see [06 — Database Migration](../implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md)).

`assessment_drafts.agent_execution_log_id` links a legacy draft row to the execution that generated it; a legacy log row can exist without a draft on failure paths.

## Planned schema (future releases)

The following tables remain planned unless they already appear in migrations after V12. Full column-level details are in [`docs/04-architecture/data-model.md`](../04-architecture/data-model.md).

| Table | Purpose | Added in |
|-------|---------|---------|
| `rubric` | Versioned rubric criteria, weights, and levels — one approved rubric per assessment | Epic 03 |
| `student_submission` | Teacher-uploaded student work (text or file reference) | Epic 04 |
| `grade_suggestion` | AI grading output per submission, pending teacher approval | Epic 05 |
| `feedback_draft` | AI feedback text, pending teacher approval before delivery | Epic 06 |
| `learning_gap` | Cohort-level gap analysis derived from graded submissions | Epic 07 |
| `recovery_activity` | Remedial activity suggestions linked to a learning gap | Epic 08 |
| `teacher_report` | Final assessment cycle summary, pending teacher approval | Epic 09 |
| `agent_execution_log` | Mandatory log for every agent run: model, tokens, cost, status, approval state | All epics |
| `approval_event` | Immutable record of every teacher approval, edit, or rejection action | All epics |
| `question` | Question bank items (TF/SC/MC) for closed assessments | Epic 10 |
| `question_option` | Individual answer alternatives for each question | Epic 10 |
| `assessment_question_snapshot` | Immutable copy of a question at the moment the assessment was published | Epic 12 |
| `assessment_option_snapshot` | Immutable copy of options at publish time — the frozen answer key | Epic 12 |
| `learner_ref` | Minimal student reference for link-based access; not a login account | Epic 10 |
| `assessment_invitation` | One-to-one signed token linking a learner to an assessment | Epic 10 |
| `assessment_attempt` | Student response session record | Epic 10 |
| `grade_result` | Final graded outcome after teacher approval; used for both open and closed assessments | Epics 05, 12 |
| `usage_event` | Structured log of product usage events for billing and plan enforcement | All epics |
| `revenue_event` | Payment, commitment, or pilot revenue records with `related_party` flag | All epics |
| `cost_event` | AI and infrastructure cost records for unit economics tracking | All epics |

See [`docs/04-architecture/data-model.md`](../04-architecture/data-model.md) for the complete schema, field validation rules, allowed enum values, and JSON shape expectations for each table.

---

## Data access patterns

### Spring Data JPA repositories

Every entity has a corresponding repository interface extending `JpaRepository<Entity, IdType>`. For `TeacherEntity`:

```java
public interface TeacherRepository extends JpaRepository<TeacherEntity, String> {
    Optional<TeacherEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    List<TeacherEntity> findByPlanType(String planType);
    List<TeacherEntity> findByRelatedParty(boolean relatedParty);
}
```

Spring Data generates the SQL at startup from method name conventions. This covers the majority of queries needed in MVP.

### Complex queries with JPQL

For queries involving joins, aggregations, or multiple filters, use `@Query` with JPQL rather than native SQL:

```java
@Query("SELECT t FROM TeacherEntity t WHERE t.planType = :planType AND t.relatedParty = true")
List<TeacherEntity> findPilotRelatedParties(@Param("planType") String planType);
```

Only fall back to native SQL (`nativeQuery = true`) when JPQL cannot express the required query (e.g., PostgreSQL-specific functions or JSONB operators).

### N+1 prevention

When loading an entity that has lazy-loaded associations, use `JOIN FETCH` in JPQL to prevent N+1 queries:

```java
@Query("SELECT a FROM AssessmentEntity a JOIN FETCH a.rubrics WHERE a.teacherFirebaseUid = :uid")
List<AssessmentEntity> findByTeacherWithRubrics(@Param("uid") String uid);
```

Alternatively, annotate a repository method with `@EntityGraph` to specify which associations to eager-load without writing a JPQL query:

```java
@EntityGraph(attributePaths = {"rubrics"})
List<AssessmentEntity> findByTeacherFirebaseUid(String uid);
```

---

<!-- nav -->

← [04-security-implementation.md](04-security-implementation.md) | [↑ Top](#database-guide) | [Agent Development →](06-agent-development.md)

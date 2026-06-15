# Database Guide

This guide covers the PostgreSQL schema, Flyway migration conventions, JPA entity mapping, and the patterns used for data access in GradeOps AI.

---

## Technology

**Database:** PostgreSQL 15+
**Migration tool:** Flyway (managed automatically by Spring Boot on startup)
**ORM:** Spring Data JPA / Hibernate

---

## Current schema (Epic 01)

As of Epic 01, the database contains a single table: `teacher`. The schema was applied in two migrations.

### `teacher` table — V1

```sql
CREATE TABLE teacher (
    firebase_uid VARCHAR(128)              NOT NULL,
    name         VARCHAR(255)              NOT NULL,
    email        VARCHAR(255)              NOT NULL,
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
| `name` | `VARCHAR(255)` | NOT NULL | Teacher's display name. |
| `email` | `VARCHAR(255)` | NOT NULL, unique | Email from the Firebase token. Enforced as unique by `teacher_email_idx`. |
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
| `related_party` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Marks whether the teacher is a related party (e.g., a friend, family member, or team collaborator). Required for hackathon revenue evidence reporting. |
| `offer_details` | `TEXT` | nullable | Free-text description of the offer made to the pilot teacher. |
| `evidence_link` | `TEXT` | nullable | URL to external evidence (e.g., a screenshot, invoice, or letter of intent). |
| `flag_set_by` | `VARCHAR(255)` | nullable | Identifier of the operator who set the pilot flags. |
| `flag_set_at` | `TIMESTAMPTZ` | nullable | When the pilot flags were last set. |

---

## JPA entity mapping

`TeacherEntity` at `cl.gradeops.ai.api.domain.teacher.TeacherEntity` maps to the `teacher` table.

Key mapping details:

- `@Id @Column(name = "firebase_uid", length = 128, nullable = false)` — Firebase UID is the primary key. The type is `String`, not a generated UUID.
- `@Column(name = "created_at", nullable = false, updatable = false)` — the `updatable = false` flag tells Hibernate to never emit this column in an `UPDATE` statement. Only written at insert time.
- `@Column(name = "updated_at", nullable = false)` — has a public `setUpdatedAt(OffsetDateTime)` setter. The service layer must call this explicitly before persisting. There is no automatic `@PreUpdate` hook.
- Pilot flag columns from V2 (`planType`, `relatedParty`, etc.) are all nullable in Java (`String`, `OffsetDateTime`) except `relatedParty`, which is a primitive `boolean` defaulting to `false` — matching the `NOT NULL DEFAULT FALSE` database constraint.
- The no-arg constructor `protected TeacherEntity() {}` exists only for JPA reflection and should not be used in application code. Use `new TeacherEntity(firebaseUid, name, email)`.
- All timestamps use `OffsetDateTime`, which maps cleanly to `TIMESTAMP WITH TIME ZONE` in PostgreSQL.

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
- `V3__create_assessment_table.sql`

### File location

```
api/src/main/resources/db/migration/
```

### Checksum enforcement

Flyway validates the checksum of every applied migration file on each startup. If a file is modified after it has been applied to a database, the application will fail to start with a `FlywayException`. This is intentional — it prevents silent schema drift.

**Never modify an existing migration file.** To correct a mistake in a previous migration, create a new version (e.g., V3) that applies the corrective `ALTER TABLE` or `UPDATE` statement.

---

## How to add a new migration

1. **Determine the next version number.** Currently V1 and V2 are applied. The next available version is V3.

2. **Create the file** in `api/src/main/resources/db/migration/`:
   ```
   V3__description_of_change.sql
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

## Planned schema (Epics 02–13)

The following tables will be added in subsequent epics. They are not yet implemented. Full column-level details are in [`docs/04-architecture/data-model.md`](../04-architecture/data-model.md).

| Table | Purpose | Added in |
|-------|---------|---------|
| `assessment` | Core assessment lifecycle — stores the teacher's brief, AI-generated draft JSON, and workflow status | Epic 02 |
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

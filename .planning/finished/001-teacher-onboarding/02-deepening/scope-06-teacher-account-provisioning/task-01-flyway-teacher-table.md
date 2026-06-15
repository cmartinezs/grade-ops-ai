# ⚛️ TASK 01 — Flyway Migration: teacher Table

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-06-teacher-account-provisioning.md)

---

## Objective

Create `api/src/main/resources/db/migration/V1__create_teacher_table.sql` defining the `teacher` table with Firebase UID as primary key, name, email, and timestamps.

---

## Technical Design

- **Approach:** Flyway-managed SQL migration. The table uses `firebase_uid VARCHAR(128)` as the natural PK (Firebase UIDs are max 128 chars). `email` has a unique constraint. No password column — credentials are Firebase-managed.
- **Affected files / components:**
  - `api/src/main/resources/db/migration/V1__create_teacher_table.sql` (new file)
- **Interfaces / contracts:** Table `teacher(firebase_uid, name, email, created_at, updated_at)`. Consumed by `TeacherRepository` (scope-06 task-03) and `TeacherEntity` JPA entity.
- **Design notes:** Pilot flag columns (`plan_type`, `related_party`, etc.) are added in scope-03 task-01 as a separate migration (`V2__`). This migration is intentionally minimal. `email` uniqueness is enforced at DB level; Firebase Auth also prevents duplicate emails, but dual enforcement is safer.

---

## Implementation Steps

1. Create `api/src/main/resources/db/migration/V1__create_teacher_table.sql` with:
   ```sql
   CREATE TABLE teacher (
       firebase_uid VARCHAR(128) PRIMARY KEY,
       name         VARCHAR(255) NOT NULL,
       email        VARCHAR(255) NOT NULL,
       created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
       updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
   );
   CREATE UNIQUE INDEX teacher_email_idx ON teacher(email);
   ```
2. Verify Flyway is configured in `api/src/main/resources/application.yml` (or `application.properties`) with `spring.flyway.enabled=true` and `spring.flyway.locations=classpath:db/migration`.
3. Run `./mvnw flyway:migrate` (or start the app) to apply the migration against the local PostgreSQL instance.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Migration applies cleanly | `flyway:migrate` completes with status `Successfully applied 1 migration` | `./mvnw flyway:migrate` |
| 2 | Table has correct columns | `\d teacher` in psql shows firebase_uid (PK), name, email, created_at, updated_at | psql / integration test |
| 3 | Duplicate email rejected | `INSERT` of two rows with the same email fails with unique constraint violation | SQL / integration test |

---

## Done Criteria

- [ ] `V1__create_teacher_table.sql` exists at the correct path.
- [ ] Migration applies without errors against a clean PostgreSQL instance.
- [ ] `teacher` table has `firebase_uid` as PK, `email` with unique index, `created_at`, `updated_at`.
- [ ] All unit tests listed above pass.
- [ ] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-06-teacher-account-provisioning.md)

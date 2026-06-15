# ⚛️ TASK 01 — Flyway Migration: Pilot Flag Columns

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-03-pilot-account-flag.md)

---

## Objective

Create `api/src/main/resources/db/migration/V2__add_pilot_flag_columns.sql` adding pilot-flag, related-party, offer, and audit columns to the `teacher` table.

---

## Technical Design

- **Approach:** Flyway migration adding nullable columns to the existing `teacher` table (from scope-06 V1). Columns: `plan_type` (VARCHAR enum: `pilot`, `free`, `paid`), `related_party` (BOOLEAN), `offer_details` (TEXT), `evidence_link` (TEXT), `flag_set_by` (VARCHAR — operator identifier), `flag_set_at` (TIMESTAMP). All nullable (existing teachers have no flag).
- **Affected files / components:**
  - `api/src/main/resources/db/migration/V2__add_pilot_flag_columns.sql` (new)
- **Interfaces / contracts:** New columns consumed by `PilotFlagRepository` / `TeacherEntity` (scope-03 task-02). `related_party` maps to `RevenueEvent.related_party` semantics from `docs/05-evidence/`.
- **Design notes:** Use a DB-level check constraint on `plan_type` (`CHECK (plan_type IN ('pilot', 'free', 'paid'))`). `related_party` defaults to `false` to make the field queryable for all accounts. V2 must run after V1 — Flyway ordering is guaranteed by version prefix.

---

## Implementation Steps

1. Create `api/src/main/resources/db/migration/V2__add_pilot_flag_columns.sql`:
   ```sql
   ALTER TABLE teacher
     ADD COLUMN plan_type     VARCHAR(10)  CHECK (plan_type IN ('pilot', 'free', 'paid')),
     ADD COLUMN related_party BOOLEAN      NOT NULL DEFAULT FALSE,
     ADD COLUMN offer_details TEXT,
     ADD COLUMN evidence_link TEXT,
     ADD COLUMN flag_set_by   VARCHAR(255),
     ADD COLUMN flag_set_at   TIMESTAMP WITH TIME ZONE;
   ```
2. Run `./mvnw flyway:migrate` against the local DB (which already has V1 applied).
3. Verify columns exist with `\d teacher` in psql.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | V2 migration applies cleanly after V1 | `flyway:migrate` reports `Successfully applied 1 migration` | `./mvnw flyway:migrate` |
| 2 | New columns exist and accept expected values | `UPDATE teacher SET plan_type='pilot', related_party=true WHERE firebase_uid='test'` succeeds | psql / integration test |
| 3 | Invalid `plan_type` rejected | `UPDATE teacher SET plan_type='enterprise' ...` fails with check constraint | psql / integration test |

---

## Done Criteria

- [x] `V2__add_pilot_flag_columns.sql` exists at the correct path.
- [x] Migration applies without errors on a DB with V1 already applied.
- [x] All 6 new columns exist with correct types and constraints.
- [x] `related_party` defaults to `false`.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-03-pilot-account-flag.md)

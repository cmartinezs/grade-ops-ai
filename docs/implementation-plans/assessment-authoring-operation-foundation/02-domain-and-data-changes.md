<a id="top"></a>

# 02 — Domain and Data Changes

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [01 — Current State and Target](01-current-state-and-target.md) · **Next:** [03 — Operation and API Contracts →](03-operation-and-api-contracts.md)

Rationale for every decision here is in [Assessment Authoring Model](../../99-decisions/2026-07-28-assessment-authoring-model.md), [Idempotency and Concurrency Strategy](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md), and [Durable AI Operation Model](../../99-decisions/2026-07-28-durable-ai-operation-model.md). This document is the field-level reference used while implementing.

## Domain changes

### `Assessment` (existing aggregate, extended)

| Field | Type | Change |
|---|---|---|
| `id` | `AssessmentId` | unchanged |
| `teacherUid` | `String` | unchanged |
| `status` | `AssessmentStatus` | unchanged — frozen, no new values, authoring never reads it |
| `createdAt` | `Instant` | unchanged |
| `currentRevisionId` | `UUID`, nullable | **new** — null until the first revision exists |
| `lockVersion` | `int` | **new** — Hibernate `@Version`; starts at `0` |

No new domain methods (`open`/`approve`/`publish`/etc.). Two new package-visible methods: `withCurrentRevision(UUID revisionId)` (returns a new `Assessment` value with `currentRevisionId` set — used only by the revision-creation use case, under CAS) — mutability stays constructor-based/immutable-record-style, matching the existing pattern (`restore`, no setters).

### `AssessmentBrief` — unchanged

No fields, no behavior changes in this cut. See [Assessment Authoring Model § Deferred: brief mutability](../../99-decisions/2026-07-28-assessment-authoring-model.md#deferred-brief-mutability).

### `AssessmentRevision` — new aggregate, replaces `AssessmentDraft` as the write path

```java
id: UUID
assessmentId: AssessmentId
versionNumber: int                    // >= 1
previousRevisionId: UUID              // null iff versionNumber == 1
origin: RevisionOrigin                // AI_GENERATED | HUMAN_EDITED | LEGACY_UNKNOWN
actorId: String                       // teacherUid; null only for LEGACY_UNKNOWN
reason: String                        // nullable
sourceAgentAttemptId: UUID            // nullable; set iff origin == AI_GENERATED
title, context, instructions: String
objectives, deliverables, constraints: List<String>
createdAt: Instant
```

Factory methods, mirroring `AssessmentDraft`'s existing shape but dropping `applyEdit`:

- `generateFromAi(assessmentId, content..., actorId, sourceAgentAttemptId)` → `versionNumber = 1`, `previousRevisionId = null`, `origin = AI_GENERATED`.
- `regenerateFromAi(previousRevision, content..., actorId, reason, sourceAgentAttemptId)` → `versionNumber = previous + 1`, validates `previousRevision.assessmentId` matches.
- `createFromHumanEdit(previousRevision, content..., actorId, reason)` → same versioning rule, `origin = HUMAN_EDITED`, `sourceAgentAttemptId = null`.
- `restore(...)` — full-field reconstruction for persistence mapping, same role as today's `restore`.
- **No `applyEdit`.** This is the one method this cut deletes.

`RevisionOrigin` enum: `AI_GENERATED`, `HUMAN_EDITED`, `LEGACY_UNKNOWN`. Application code (factories above) can only ever produce the first two; `LEGACY_UNKNOWN` is constructible only via `restore(...)`, which the migration script uses directly, not through a factory a handler could accidentally call.

### `AiOperation` — new aggregate

```java
id: UUID
assessmentId: AssessmentId
operationType: AiOperationType         // CREATE_INITIAL_REVISION | REGENERATE_REVISION
requestedBy: String                    // teacherUid
idempotencyKey: String
expectedRevisionId: UUID               // null for CREATE_INITIAL_REVISION
status: AiOperationStatus              // PENDING | IN_PROGRESS | SUCCEEDED | FAILED_RETRYABLE | FAILED_TERMINAL
resultRevisionId: UUID                 // nullable, set on SUCCEEDED
createdAt, updatedAt: Instant
```

### `AgentAttempt` — new aggregate

```java
id: UUID
aiOperationId: UUID
attemptNumber: int                     // 1, 2, 3… per operation
agentName: String
resolvedProvider, resolvedModel: String    // null only while status == DISPATCHED
promptVersion: String
correlationId: String
dispatchedAt: Instant
completedAt: Instant                   // null while DISPATCHED
status: AgentAttemptStatus             // DISPATCHED | COMPLETED | FAILED
providerRequestId: String              // nullable
failureCode: String                    // nullable, typed — see 04-ai-operation-lifecycle.md
estimatedInputTokens, estimatedOutputTokens: Integer
costEstimate: BigDecimal               // NUMERIC, not Double
structuredResult: String               // JSON, nullable while DISPATCHED/FAILED-before-response
```

### `IdempotencyRecord` — new, application-infrastructure concept, not a domain aggregate

Modeled as a repository-port-backed value, not an `AggregateRoot` — it has no independent lifecycle or business behavior beyond "does this key/scope combination already have a result." See [Idempotency and Concurrency Strategy](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md) for the full field list.

## Invariants (enforced in application code + proven by integration tests, per [Assessment Authoring Model](../../99-decisions/2026-07-28-assessment-authoring-model.md))

1. A revision is immutable after construction — no setter, no update path, on any content field.
2. `versionNumber == 1` iff `previousRevisionId == null`.
3. `previousRevisionId`, when set, references a revision with the same `assessmentId` and `versionNumber - 1`.
4. `Assessment.currentRevisionId`, once non-null, always references a revision of that same assessment.
5. `AiOperation` has at most one `PENDING`/`IN_PROGRESS` row per `(assessmentId, operationType)` — enforced by a partial unique index, not application logic alone.
6. `AgentAttempt.attemptNumber` is unique and monotonic per `aiOperationId`.
7. Authoring use cases never read `Assessment.status`.

## Ownership and mutability

- `Assessment` and `AssessmentBrief`: unchanged ownership (`teacherUid` equality, verified by `OwnershipVerifier`).
- `AssessmentRevision`, `AiOperation`, `AgentAttempt`: owned transitively through `assessmentId` — no independent ownership check; access control is always resolved via the parent `Assessment`.
- Every new aggregate above is immutable after creation except the two explicitly-scoped CAS updates: `Assessment.currentRevisionId`/`lockVersion`, and `AiOperation`/`AgentAttempt` status transitions (terminal-state updates only, never content).

## Persistence changes

### New tables

```sql
assessment_revisions (
  id                       UUID PRIMARY KEY,
  assessment_id            UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
  version_number           INT NOT NULL,
  previous_revision_id     UUID REFERENCES assessment_revisions(id),
  origin                   VARCHAR NOT NULL CHECK (origin IN ('AI_GENERATED','HUMAN_EDITED','LEGACY_UNKNOWN')),
  actor_id                 VARCHAR,             -- null only for LEGACY_UNKNOWN
  reason                   TEXT,
  source_agent_attempt_id  UUID REFERENCES agent_attempts(id),
  title                    VARCHAR NOT NULL,
  context                  TEXT NOT NULL,
  instructions             TEXT NOT NULL,
  objectives               JSONB NOT NULL,
  deliverables              JSONB NOT NULL,
  constraints               JSONB NOT NULL,
  created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (assessment_id, version_number)
);
CREATE INDEX idx_assessment_revisions_assessment_id ON assessment_revisions(assessment_id);

ALTER TABLE assessments
  ADD COLUMN current_revision_id UUID REFERENCES assessment_revisions(id),
  ADD COLUMN lock_version INT NOT NULL DEFAULT 0;

idempotency_records (
  id                    UUID PRIMARY KEY,
  scope_type            VARCHAR NOT NULL CHECK (scope_type IN ('TEACHER','ASSESSMENT')),
  teacher_uid           VARCHAR,
  assessment_id         UUID REFERENCES assessments(id),
  operation_type        VARCHAR NOT NULL,
  idempotency_key       VARCHAR NOT NULL,
  request_payload_hash  VARCHAR NOT NULL,
  result_reference       VARCHAR,
  response_status        INT,
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at             TIMESTAMPTZ NOT NULL,
  UNIQUE (scope_type, teacher_uid, assessment_id, operation_type, idempotency_key)
);

ai_operations (
  id                     UUID PRIMARY KEY,
  assessment_id          UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
  operation_type         VARCHAR NOT NULL CHECK (operation_type IN ('CREATE_INITIAL_REVISION','REGENERATE_REVISION')),
  requested_by            VARCHAR NOT NULL,
  idempotency_key         VARCHAR NOT NULL,
  expected_revision_id    UUID REFERENCES assessment_revisions(id),
  status                  VARCHAR NOT NULL CHECK (status IN ('PENDING','IN_PROGRESS','SUCCEEDED','FAILED_RETRYABLE','FAILED_TERMINAL')),
  result_revision_id      UUID REFERENCES assessment_revisions(id),
  created_at, updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uq_ai_operations_in_flight
  ON ai_operations(assessment_id, operation_type) WHERE status IN ('PENDING','IN_PROGRESS');

agent_attempts (
  id                        UUID PRIMARY KEY,
  ai_operation_id           UUID NOT NULL REFERENCES ai_operations(id) ON DELETE CASCADE,
  attempt_number            INT NOT NULL,
  agent_name                VARCHAR NOT NULL,
  resolved_provider         VARCHAR,
  resolved_model            VARCHAR,
  prompt_version            VARCHAR,
  correlation_id            VARCHAR,
  dispatched_at             TIMESTAMPTZ NOT NULL,
  completed_at              TIMESTAMPTZ,
  status                    VARCHAR NOT NULL CHECK (status IN ('DISPATCHED','COMPLETED','FAILED')),
  provider_request_id       VARCHAR,
  failure_code              VARCHAR,
  estimated_input_tokens    INTEGER,
  estimated_output_tokens   INTEGER,
  cost_estimate             NUMERIC(12,6),
  structured_result         JSONB,
  UNIQUE (ai_operation_id, attempt_number)
);
CREATE INDEX idx_agent_attempts_ai_operation_id ON agent_attempts(ai_operation_id);
```

`assessment_revisions.source_agent_attempt_id` references `agent_attempts(id)`, so `agent_attempts` must exist before `assessment_revisions` in migration order (see [06 — Database Migration](06-database-migration.md) for exact Flyway file ordering).

### Legacy tables

`assessment_drafts` and `agent_execution_logs` are kept, read-only, for one release after cutover — no `DROP`/`ALTER ... DROP COLUMN` in this cut.

## Naming and deprecations

- `AssessmentDraft` (domain class) is deleted after migration and cutover — not kept as dead code "just in case." Its persistence adapter and JPA entity are deleted in the same task.
- `AgentExecutionLog` (domain class) is deleted the same way, replaced by `AiOperation`/`AgentAttempt`.
- `UpdateAssessmentDraftHandler`/`UpdateAssessmentDraftCommand`/`UpdateAssessmentDraftUseCase` are deleted, replaced by `CreateHumanRevisionHandler`/`CreateHumanRevisionCommand`/`CreateHumanRevisionUseCase`.
- `PATCH /api/v1/assessments/{id}/draft` is removed from `AssessmentController` (see [03 — Operation and API Contracts](03-operation-and-api-contracts.md)).

---

← [01 — Current State and Target](01-current-state-and-target.md) | [↑ README](README.md) | [Siguiente: Operation and API Contracts →](03-operation-and-api-contracts.md)

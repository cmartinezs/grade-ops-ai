<a id="top"></a>

# LOCAL-CONTRACTS — API: Assessment Authoring Operation Foundation

**Status:** Ready — frozen. **Parent:** [README](README.md) · **Prev:** [TASKS](TASKS.md) · **Next:** [TEST-PLAN →](TEST-PLAN.md)

Field-level reference for implementation. Authoritative source: [Assessment Authoring Model](../../../../docs/99-decisions/2026-07-28-assessment-authoring-model.md), [Idempotency and Concurrency Strategy](../../../../docs/99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md), [Durable AI Operation Model](../../../../docs/99-decisions/2026-07-28-durable-ai-operation-model.md), [Authoring Operation Contract](../../../../docs/99-decisions/2026-07-28-authoring-operation-contract.md), and [02](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/02-domain-and-data-changes.md)/[03](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/03-operation-and-api-contracts.md)/[04](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/04-ai-operation-lifecycle.md) of the plan. Do not rename anything below without updating this file and the root [Shared Domain Contract](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/02-shared-domain-contract.md) in the same change.

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

No new domain methods (`open`/`approve`/`publish`). One new package-visible method: `withCurrentRevision(UUID revisionId)` (returns a new `Assessment` value with `currentRevisionId` set — used only by the revision-creation use case, under CAS).

### `AssessmentBrief` — unchanged, no fields, no behavior changes.

### `AssessmentRevision`

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

Factories: `generateFromAi(assessmentId, content..., actorId, sourceAgentAttemptId)` → `versionNumber = 1`; `regenerateFromAi(previousRevision, content..., actorId, reason, sourceAgentAttemptId)` → `versionNumber = previous + 1`, validates `previousRevision.assessmentId` matches; `createFromHumanEdit(previousRevision, content..., actorId, reason)` → same versioning, `origin = HUMAN_EDITED`, `sourceAgentAttemptId = null`; `restore(...)` — full-field reconstruction for persistence mapping. **No `applyEdit`.**

`RevisionOrigin` enum: `AI_GENERATED`, `HUMAN_EDITED`, `LEGACY_UNKNOWN`. Application factories can only produce the first two; `LEGACY_UNKNOWN` is constructible only via `restore(...)`, which only the migration script calls directly.

### `AiOperation`

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

### `AgentAttempt`

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
failureCode: String                    // nullable, typed — see § Canonical failure-code taxonomy
estimatedInputTokens, estimatedOutputTokens: Integer
costEstimate: BigDecimal               // NUMERIC, not Double
structuredResult: String               // JSON, nullable while DISPATCHED/FAILED-before-response
```

### `IdempotencyRecord` — repository-port-backed value, not an `AggregateRoot`

```java
id
scopeType            // TEACHER | ASSESSMENT
teacherUid           // set when scopeType = TEACHER
assessmentId         // set when scopeType = ASSESSMENT
operationType
idempotencyKey
requestPayloadHash   // hash of validated command fields, not raw bytes
resultReference       // id of the resource/response produced
responseStatus
createdAt
expiresAt            // createdAt + 24h; no cleanup job in this cut
```

## Invariants

1. A revision is immutable after construction — no setter, no update path, on any content field.
2. `versionNumber == 1` iff `previousRevisionId == null`.
3. `previousRevisionId`, when set, references a revision with the same `assessmentId` and `versionNumber - 1`.
4. `Assessment.currentRevisionId`, once non-null, always references a revision of that same assessment.
5. `AiOperation` has at most one `PENDING`/`IN_PROGRESS` row per `(assessmentId, operationType)` — enforced by the partial unique index, not application logic alone.
6. `AgentAttempt.attemptNumber` is unique and monotonic per `aiOperationId`.
7. Authoring use cases never read `Assessment.status`.

## Persistence changes

```sql
assessment_revisions (
  id                       UUID PRIMARY KEY,
  assessment_id            UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
  version_number           INT NOT NULL,
  previous_revision_id     UUID REFERENCES assessment_revisions(id),
  origin                   VARCHAR NOT NULL CHECK (origin IN ('AI_GENERATED','HUMAN_EDITED','LEGACY_UNKNOWN')),
  actor_id                 VARCHAR,
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

Migration file order: `V13` (`agent_attempts`, `ai_operations`) → `V14` (`assessment_revisions`, FKs `agent_attempts`) → `V15` (`assessments.current_revision_id`/`lock_version`) → `V16` (`idempotency_records`) → `V17` (backfill). `assessment_drafts`/`agent_execution_logs` are **not** dropped in this cut.

## Sequencing (Task 07B)

```text
1. Use case receives the command (generate / regenerate / retry).
2. Load Assessment. Verify ownership.
3. Idempotency check — replay or reject before anything else.
4. Pre-dispatch staleness check: expectedRevisionId vs. Assessment.currentRevisionId
   (skip for CREATE_INITIAL_REVISION, which has none).
   → mismatch: 409 STALE_REVISION, no AiOperation touched, no agent call.
5. TRANSACTION A (commits before step 6):
   - find-or-create AiOperation (PENDING → IN_PROGRESS)
   - insert AgentAttempt (status = DISPATCHED, dispatchedAt = now())
   - commit
6. Call agents/ over HTTP — NO open transaction during this call.
7. TRANSACTION B:
   - success + CAS still valid → AgentAttempt COMPLETED, AssessmentRevision inserted,
     Assessment.currentRevisionId/lockVersion CAS-updated, AiOperation SUCCEEDED
   - success + CAS now invalid → AgentAttempt FAILED (failureCode = STALE_ON_COMPLETION),
     structuredResult still persisted, AiOperation FAILED_TERMINAL
   - agent/transport failure → AgentAttempt FAILED (failureCode = mapped, see below),
     AiOperation FAILED_RETRYABLE or FAILED_TERMINAL
   - commit
```

If the process crashes between step 5's commit and step 7's commit, the `AgentAttempt` row exists in `DISPATCHED` state — this is the intended, accepted "orphaned in-flight" outcome, not a bug.

### Where provider/model get resolved

`agents/`'s `AssessmentAgentOrchestrator.generate()` resolves the provider and returns the actually-used provider/model in its response. This cut changes `api/` to persist `response.log().provider()`/`response.log().model()` onto `AgentAttempt.resolvedProvider`/`resolvedModel` — **not** what the client requested (today's defect: `api/` persists `agentCommand.provider()`, frequently `null`). See [TASKS.md § Task 07B](TASKS.md#task-07b--application-rewrite-the-generation-coordinator-the-pivot-task) for the dependency on Agents' Task 07A.

## Canonical failure-code taxonomy

| Code | Produced by | Retryable | Meaning |
|---|---|---|---|
| `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` | API | No | Same key, different request body |
| `ALREADY_GENERATED` | API | N/A | Initial generation requested twice |
| `STALE_REVISION` | API | Yes | `expectedRevisionId` no longer current |
| `NO_ACTIVE_OPERATION_TO_RETRY` | API | No | Retry called with nothing to retry |
| `OPERATION_IN_PROGRESS` | API | Yes | An attempt may still be running |
| `AGENT_UNAVAILABLE` | API, `AgentClientException.Reason.UNREACHABLE` | Yes | Transient transport failure |
| `AGENT_ERROR` | API, `AgentClientException.Reason.AGENT_ERROR` | Yes | `agents/` returned 5xx |
| `AGENT_REJECTED` | API, `AgentClientException.Reason.AGENT_REJECTED` | No | `agents/` 4xx, detail not separately available |
| `INVALID_COMMAND` | AGENTS, `AssessmentAgentException.Reason.INVALID_COMMAND`, persisted verbatim | No | Agents-side validation failure |
| `MALFORMED_OUTPUT` | AGENTS, `AssessmentAgentException.Reason.MALFORMED_OUTPUT`, persisted verbatim | Yes | LLM structured-output shape failure |
| `STALE_ON_COMPLETION` | API, CAS failure at persist time | No | Late AI success arrives after `expectedRevisionId` moved |

Read the agents/ response payload directly for `INVALID_COMMAND`/`MALFORMED_OUTPUT` — `GlobalExceptionHandler`'s current behavior of collapsing every agents/-side 4xx into `AGENT_REJECTED` is exactly the defect this task fixes; do not only inspect the HTTP status class.

**No generic synonyms** (`TIMEOUT`, `PROVIDER_UNAVAILABLE`, `RATE_LIMITED`, `INTERNAL_ERROR`, etc.) — this table is the complete, closed set for this cut.

## Canonical status taxonomy

| Layer | Values |
|---|---|
| `AiOperation.status` | `PENDING`, `IN_PROGRESS`, `SUCCEEDED`, `FAILED_RETRYABLE`, `FAILED_TERMINAL` |
| `AgentAttempt.status` | `DISPATCHED`, `COMPLETED`, `FAILED` |
| `GET .../generation-status` response `status` | `NOT_STARTED`, `IN_PROGRESS`, `FAILED_RETRYABLE`, `INDETERMINATE` (plus implicit success via `currentRevisionId` non-null) |

## Orphaned in-flight detection (no scheduler)

```text
if attempt.status == DISPATCHED and now() - attempt.dispatchedAt > (5 × agentclient.read-timeout):
    → surface as INDETERMINATE, retryable = true, distinct message from FAILED_RETRYABLE
else:
    → surface attempt.status as-is
```

Computed at read time by `GET .../generation-status`. No background job. `agentclient.read-timeout` is the existing `AgentClientConfig` value (60s per Research 02 §14).

## Idempotency

Two scopes: `(teacherUid, operationType, idempotencyKey)` for creation-type operations, `(assessmentId, operationType, idempotencyKey)` for assessment-scoped mutations. Retention: 24h from `createdAt`, no cleanup job this cut. Same key + same `requestPayloadHash` → replay original response, do not re-execute. Same key + different hash → `409 IDEMPOTENCY_KEY_PAYLOAD_MISMATCH`.

## API ↔ Web public contract

| Operation | Endpoint | Idempotency-Key | `expectedRevisionId` | Success |
|---|---|---|---|---|
| Create assessment | `POST /api/v1/assessments` | Required | n/a | `201` |
| Generate initial revision | `POST /api/v1/assessments/{id}/draft` | Required | n/a | `201` or `202` |
| Retry operation | `POST /api/v1/assessments/{id}/draft/retry` | None | n/a | `202` |
| Get generation status | `GET /api/v1/assessments/{id}/generation-status` | n/a | n/a | `200` |
| Regenerate | `POST /api/v1/assessments/{id}/draft/regenerate` | Required | Required | `201` |
| Create human revision | `POST /api/v1/assessments/{id}/revisions` | None | Required | `201` |
| Get current/history | `GET /api/v1/assessments/{id}/draft`, `GET .../draft/versions` | n/a | n/a | `200` |

`PATCH /api/v1/assessments/{id}/draft` is **removed**, not deprecated-and-kept. Full endpoint-by-endpoint status codes and error responses: [03 — Operation and API Contracts](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/03-operation-and-api-contracts.md).

## Legacy migration

Every `assessment_drafts` row migrates as `origin = LEGACY_UNKNOWN`, `provenanceComplete = false`, `actorId = NULL`, `reason = NULL`, unconditionally — the legacy schema has no edit-timestamp column, so no row can be honestly labeled `AI_GENERATED` with confidence, even a row that was never the target of a `PATCH`. `sourceAgentAttemptId` is set when the source row's `agent_execution_log_id` was non-null. Each `agent_execution_logs` row synthesizes exactly one `ai_operations` row (`status = SUCCEEDED` if `status = 'COMPLETED'` and it produced a draft, else `FAILED_TERMINAL`) and exactly one `agent_attempts` row (`attemptNumber = 1`); `operationType` inferred from the linked draft's `version_number` (`1` → `CREATE_INITIAL_REVISION`, `>1` → `REGENERATE_REVISION`). Full reasoning: [06 — Database Migration](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md).

---

← [TASKS](TASKS.md) | [↑ inicio](#top) | [Siguiente: TEST-PLAN →](TEST-PLAN.md)

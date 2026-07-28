<a id="top"></a>

# Idempotency and Concurrency Strategy

- Status: Accepted
- Date: 2026-07-28
- Decision owner: Technical
- Informed by: [Research 01](../../design-system/research/research-01-current-domain-data-model-alignment-audit.md), [Research 02](../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md)
- Depends on: [Assessment Authoring Model](2026-07-28-assessment-authoring-model.md), [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md)
- Related decisions: [Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md)

## Context

Neither idempotency nor concurrency control exists today. Research 01 §6.3 and Research 02 §15 document the same gap from two angles: no `@Version`/optimistic lock anywhere in the assessment authoring code, no idempotency key or store, "current draft" derived by `MAX(version_number)`, and the explicit warning that `@Transactional` (which the code does use correctly, per `DraftGenerationCoordinator`) is not idempotency and does not prevent duplicate LLM dispatch, duplicate `Assessment` creation, or lost updates.

## Problem

Define exactly what scope an idempotency key covers, where it is stored and for how long, what a client gets back on repeat/conflict, how optimistic concurrency is enforced on `Assessment.currentRevisionId`, and how to keep AI spend from being wasted on requests that are already obsolete by the time they'd dispatch.

## Decision

### Idempotency key scope

Two scopes, matching the two operation shapes in the [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md):

```text
Creation-type operations   →  (teacherUid, operationType, idempotencyKey)
Assessment-scoped mutations →  (assessmentId, operationType, idempotencyKey)
```

`operationType` is part of the key on purpose — the same `idempotencyKey` value reused by a client across two different operation types (a bug, but one worth defending against) must not be treated as a replay of the wrong operation.

### Storage

New table `idempotency_record`:

```text
id
scope_type            (TEACHER | ASSESSMENT)
teacher_uid            (set when scope_type = TEACHER)
assessment_id           (set when scope_type = ASSESSMENT)
operation_type
idempotency_key
request_payload_hash   (hash of the validated command fields, not raw bytes — tolerates client-side incidental differences like header order)
result_reference        (id of the resource/response produced — e.g., assessmentId, revisionId, or the AiOperation id)
response_status
created_at
expires_at
UNIQUE (scope_type, teacher_uid, assessment_id, operation_type, idempotency_key)
```

### Retention

24 hours from `created_at`. This matches common industry practice (Stripe-style idempotency windows) and comfortably covers "client retries after a network blip" and "teacher reloads and clicks retry a few minutes later." No cleanup job is introduced in this cut — pilot-scale volume makes unbounded (uncapped) row growth a non-issue for now; a scheduled cleanup is flagged as a pre-scale follow-up, not a blocker (see Open Consequences).

### Response on repeat

- Same key + same `request_payload_hash` → replay: return the exact original `response_status` and reconstruct the body from `result_reference` (do not re-execute the use case, do not call `agents/` again).
- Same key + different `request_payload_hash` → `409 Conflict`, `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH`.
- Key present but no record found (first use) → execute normally, write the record as part of the same transaction that produces the result.

### Optimistic locking on `Assessment`

`Assessment.lockVersion` (Hibernate `@Version`, `INT`) protects every write to `Assessment.currentRevisionId`. This is the DB-level safety net: two concurrent transactions racing to update the same `Assessment` row can only have one succeed; the other gets `OptimisticLockException`, mapped to `409 Conflict`.

`@Version` alone is **not** sufficient, and this decision states explicitly why, since the task brief calls out this exact conflation: optimistic locking prevents a **lost update** (two writers, one row, don't silently let the second overwrite the first) but it does **not** prevent duplicate LLM dispatch — two concurrent regenerate requests can both pass a read-time check and both call `agents/` before either commits — and it does not prevent duplicate resource creation on client retry, which is what idempotency keys solve. The two mechanisms are complementary; neither substitutes for the other:

```text
@Version / optimistic lock   →  protects against concurrent writers of the same row (lost update)
Idempotency key               →  protects against the same logical request being submitted more than once (duplicate effect)
```

### `expectedRevisionId` — domain-meaningful CAS, checked before dispatch

In addition to `lockVersion`, mutation commands (`regenerate`, `create human revision`) require the client to pass `expectedRevisionId` explicitly. Two reasons this is not redundant with `lockVersion`:

1. It is meaningful to a human/UI ("you're regenerating version 3, but version 4 now exists") where an opaque integer lock version is not.
2. It lets the use case check staleness **before** calling `agents/**, not only at persist time** — the use case loads `Assessment`, compares `expectedRevisionId` to `currentRevisionId`, and fails fast with `STALE_REVISION` if they differ, avoiding a wasted LLM call for a request that is already obsolete. This directly answers "cómo evitar consumir IA en operaciones que ya están obsoletas."

The residual race — two requests both pass the pre-check for the same `expectedRevisionId` before either commits — is closed by `lockVersion` at persist time: the second writer's CAS update fails, its content is not applied, and (per the [Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md)) its `AgentAttempt` is marked `FAILED` with `STALE_ON_COMPLETION`, not silently discarded. This narrows the wasted-LLM-call window from the full request latency to the gap between pre-check and commit, but does not claim to eliminate it under adversarial concurrency — that residual is accepted and documented, not hidden.

### Version number generation

`versionNumber = previousVersion.versionNumber + 1`, computed inside the same transaction as the `currentRevisionId` CAS update. If two requests compute `N+1` concurrently from the same base, only one write commits (the `lockVersion` CAS on `Assessment` blocks the second); the loser receives `409 STALE_REVISION` and must reload. The existing DB-level `UNIQUE (assessment_id, version_number)` constraint (`V11__add_assessment_drafts.sql`, carried forward onto `assessment_revisions`) remains as defense-in-depth against any application-level bug reaching the database.

### Concurrent regenerations / concurrent edits

Both go through the same `expectedRevisionId` + `lockVersion` CAS. Two concurrent regenerations produce exactly one success and one `409 STALE_REVISION` (never two versions claiming the same number, never a silent overwrite). Two concurrent edits produce the same outcome. This satisfies acceptance criteria #5/#6/#7 from the task brief in one mechanism, not three.

### A late AI response after the expected revision changed

If `agents/` responds successfully but the CAS at persist time fails because `currentRevisionId` moved in the meantime (a second, faster operation won), the response is **not silently dropped**: the structured result is persisted on the `AgentAttempt` record (so cost/evidence is retained — see [Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md)) but no `AssessmentRevision` is created from it, and the attempt is marked `FAILED` with a distinct typed code `STALE_ON_COMPLETION` (not the generic `AGENT_REJECTED`) so the audit trail distinguishes "the agent failed" from "the agent succeeded but the result was stale by the time it arrived."

### Client retry strategy

- The creation call (`POST /assessments`) is safe to auto-retry on transient network failure — its idempotency key makes any number of retries produce at most one `Assessment`.
- The generation/regeneration calls are **user-initiated retry only** in this cut — no blind/background automatic retry loop. Automatically re-dispatching a paid LLM call without explicit confirmation risks silent cost multiplication; this is a deliberate, conservative choice, not an oversight.

## Decision Drivers

- Research 01 §6.3, §12 "Missing Invariants"; Research 02 §15 "Concurrency, Atomicity and Idempotency" and its explicit "Important distinction" callout on `@Transactional` vs. idempotent.
- Task brief §4.3: explicit minimum scopes `(teacher + operation type + key)` for creation and `(assessment + expected revision + operation)` for mutation — both adopted as stated.
- No distributed lock manager, no queue — single-node Postgres CAS is sufficient at this scale and matches "no introduzcas Kafka, colas o microservicios sin necesidad demostrable."

## Alternatives Considered

| Option | Description | Verdict |
|---|---|---|
| A — `@Version` only, no idempotency keys | Rely purely on optimistic locking | Rejected: does not prevent duplicate `Assessment` creation or duplicate LLM dispatch on client retry — a different failure mode than lost updates |
| B — Idempotency keys only, no `expectedRevisionId`/lock | Rely purely on key replay to protect mutations | Rejected: two *different* keys can still race on the same revision (two distinct legitimate edits submitted concurrently by two tabs) — key replay does not protect against concurrent-but-distinct requests |
| C — Pessimistic locking (`SELECT ... FOR UPDATE`) on `Assessment` for every mutation | Serialize all writes to an assessment | Rejected: contention is expected to be rare (single-teacher editing their own assessment); optimistic locking gives the same correctness with no held locks and a better failure mode (409 + client-directed reload, not blocking) |
| D — Distributed idempotency store (Redis) | External cache for idempotency records | Rejected: no existing Redis/cache infra in this stack; Postgres table with a unique constraint is sufficient at this volume and keeps the operation transactionally consistent with the rest of the write |
| E — Postgres CAS + idempotency table, as decided | Combination above | **Adopted** |

## Consequences

- New table `idempotency_record`; two new columns on `assessments` (`current_revision_id`, `lock_version` — introduced by the Authoring Model decision, enforced here).
- Every mutating authoring endpoint's use case gains a pre-dispatch staleness check and a post-dispatch CAS write, replacing a single unconditional save.
- Clients (Web) must generate and track idempotency keys and the currently-displayed revision id — detailed in the implementation plan's Web migration task.

## Migration Impact

- Additive: `idempotency_record` table, `assessments.lock_version`/`current_revision_id` columns. No existing data is affected; `lock_version` starts at `0` for all existing rows via `DEFAULT 0 NOT NULL`.

## Compatibility Impact

- Breaking for the two existing generate/regenerate endpoints (idempotency key becomes mandatory) — documented in the [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md), not silently defaulted.

## Security Impact

- Idempotency key scope includes the actor (`teacherUid`) for creation-type operations specifically so keys cannot be replayed across teachers — a client cannot use another teacher's leaked/guessed key to read or affect that teacher's `Assessment`, since the scope tuple itself is part of the uniqueness constraint and the lookup is always additionally filtered by the authenticated caller.
- `request_payload_hash` is a hash, not a stored raw payload, to avoid retaining sensitive intake content longer than necessary in a secondary table.

## Testing Impact

- Concurrency tests (Testcontainers/PostgreSQL, two concurrent transactions) for: two regenerations from the same `expectedRevisionId` (exactly one succeeds), two edits from the same `expectedRevisionId` (exactly one succeeds), two creation calls with the same key (exactly one `Assessment`).
- Idempotency tests: replay with identical payload returns identical response without re-invoking the use case (verified via a spy/mock on the port that would call `agents/`, asserting zero additional invocations); replay with different payload returns `409`.
- A test proving a late/stale AI response does not create a revision but does persist its `AgentAttempt` evidence.

## Open Consequences (Explicitly Deferred)

- Scheduled cleanup of expired `idempotency_record` rows — not needed at current volume; flagged before any multi-tenant/institutional scale-up.
- Whether `expectedRevisionId` checks should also apply to a future `AssessmentBrief` edit endpoint, once one exists.

## Related Decisions

- [Assessment Authoring Model](2026-07-28-assessment-authoring-model.md)
- [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md)
- [Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md)

## References

- [Research 01 — Current Domain & Data Model Alignment Audit](../../design-system/research/research-01-current-domain-data-model-alignment-audit.md), §6.3, §12
- [Research 02 — Workflow & Lifecycle Alignment Audit](../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md), §15, §22 (Blocking Decisions)
- `api/src/main/java/cl/gradeops/ai/api/assessment/application/usecase/DraftGenerationCoordinator.java`

---

← [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md) | [↑ inicio](#top) | [Siguiente: Durable AI Operation Model →](2026-07-28-durable-ai-operation-model.md)

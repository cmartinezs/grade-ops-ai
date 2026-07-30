<a id="top"></a>

# API-A3-HANDOFF — Session A3: Authoring Mutations and Public API

**Status:** Filled in. Session A3 complete. **Parent:** [README](README.md)

```markdown
# API Session A3 Handoff — Authoring Mutations and Public API

## Session
A3 — Authoring Mutations and Public API

## Branch
feat/assessment-authoring-operation-foundation-api

## HEAD
ad265c3a2a1262c1d6c09e5e5ef7b82b6a69689e

## Starting commit
14fb8f08f9ae47a1f9de98cbe4034c7f52dfc08d (Session A2's recorded final HEAD)

## Tasks completed
08 — Done (commit 71c3ded). Human edits create immutable AssessmentRevision rows (origin
     HUMAN_EDITED) instead of mutating a draft in place. UpdateAssessmentDraftHandler/UseCase/
     Command and PATCH .../draft removed outright.
09 — Done (commit 248770c). Regenerate migrated off the legacy AssessmentDraft model onto
     AssessmentRevision/AiOperationCoordinator; expectedRevisionId now required; adjustment
     notes persisted as the new revision's `reason`.
10 — Done (commit ad265c3). Retry, generation-status, and create-human-revision public
     endpoints added; GetCurrentDraftHandler/ListDraftVersionsHandler/ListAssessmentsHandler
     migrated off the legacy AssessmentDraft table; PATCH .../draft's removal confirmed via a
     dedicated 405 test (which also surfaced and fixed a pre-existing gap — see below).

## Commits
71c3ded feat(api): human edits create immutable revisions instead of mutating in place
248770c feat(api): regenerate requires expectedRevisionId and persists adjustment reason
ad265c3 feat(api): add retry/generation-status/revisions endpoints, remove in-place draft PATCH

## Endpoints — new

### POST /api/v1/assessments/{id}/draft/retry
Auth: Bearer (Firebase). No `Idempotency-Key` header (retry is not idempotency-guarded — it is
explicitly a repeat-action-on-purpose operation).
Request body: none.
Response (always 202, regardless of whether the retried dispatch itself succeeds or fails —
Web is documented to never parse this body, only to re-poll generation-status afterward):
```json
{
  "id": "3a9e...",
  "operationType": "CREATE_INITIAL_REVISION",
  "status": "SUCCEEDED",
  "failureCode": null,
  "retryable": false,
  "resultRevisionId": "8c1f..."
}
```
`status` here is one of `AiOperation.status`'s five values (`PENDING`/`IN_PROGRESS` in practice
never observed post-dispatch since the call is synchronous; `SUCCEEDED`/`FAILED_RETRYABLE`/
`FAILED_TERMINAL` are the real outcomes). Scoped to `CREATE_INITIAL_REVISION` only — retry never
applies to `REGENERATE_REVISION` (regenerate never leaves an assessment revision-less, so there
is nothing to resume).
Errors: `404 NOT_FOUND` (no such assessment / not owned — same response either way, no
existence leak), `409 NO_ACTIVE_OPERATION_TO_RETRY` (no `AiOperation` for this assessment yet,
or the latest is `SUCCEEDED`/`FAILED_TERMINAL`), `409 OPERATION_IN_PROGRESS` (latest is
`PENDING`/`IN_PROGRESS` and its latest `AgentAttempt` is still within the indeterminate
threshold — i.e. genuinely still running, not orphaned), `401` (unauthenticated).

### GET /api/v1/assessments/{id}/generation-status
Auth: Bearer. No body.
Response (always 200):
```json
{
  "operationType": "CREATE_INITIAL_REVISION",
  "status": "IN_PROGRESS",
  "failureCode": null,
  "retryable": false,
  "currentRevisionId": null
}
```
`status` ∈ `NOT_STARTED`, `SUCCEEDED`, `IN_PROGRESS`, `INDETERMINATE`, `FAILED_RETRYABLE`,
`FAILED_TERMINAL` — see "Status codes" below for the full taxonomy note (`SUCCEEDED`/
`FAILED_TERMINAL` are additions this session made to close a gap in LOCAL-CONTRACTS.md's
originally-documented four-value taxonomy). `currentRevisionId` is non-null only when
`status == SUCCEEDED`. Computed entirely at read time (`Assessment.currentRevisionId` wins
first — this also reports `SUCCEEDED` for an assessment that only ever had a human edit, no
`AiOperation` at all — otherwise the latest `AiOperation`/`AgentAttempt` pair is projected); no
scheduler, no background reconciliation.
Errors: `404 NOT_FOUND` (no such assessment / not owned), `401`.

### POST /api/v1/assessments/{id}/revisions
Auth: Bearer. No `Idempotency-Key` header (human edit is CAS-guarded via `expectedRevisionId`,
not idempotency-guarded — two identical edits are two distinct, intentional revisions, not a
retry of the same one).
Request body:
```json
{
  "expectedRevisionId": "8c1f...",
  "title": "Loops and conditionals",
  "context": "...",
  "instructions": "...",
  "objectives": ["..."],
  "deliverables": ["..."],
  "constraints": ["..."],
  "reason": "clarified deliverable wording"
}
```
`expectedRevisionId` is `@NotNull`; `title`/`context`/`instructions` are `@NotBlank`;
`objectives`/`deliverables`/`constraints` are `@NotEmpty`; `reason` is optional/nullable.
Response (201) — same `GenerateAssessmentDraftResponse` shape every revision-producing
endpoint returns:
```json
{
  "draftId": "9f2a...",
  "title": "Loops and conditionals",
  "context": "...",
  "instructions": "...",
  "objectives": ["..."],
  "deliverables": ["..."],
  "constraints": ["..."],
  "versionNumber": 3,
  "origin": "HUMAN_EDITED",
  "actorId": "uid-teacher-1",
  "reason": "clarified deliverable wording",
  "previousRevisionId": "8c1f..."
}
```
Errors: `404 NOT_FOUND`, `409 STALE_REVISION` (`expectedRevisionId` no longer equals
`Assessment.currentRevisionId`, checked both application-side pre-write and DB-side via
`lockVersion` CAS/`UNIQUE(assessment_id, version_number)` at commit time — both paths report
identically), `422` (bean-validation failure on the request body), `401`.

## Endpoints — changed

### POST /api/v1/assessments/{id}/draft/regenerate
`expectedRevisionId` (`UUID`, `@NotNull`) is now a required request-body field alongside
`adjustmentNotes` (`@NotBlank`) — a request missing either returns `422`. `Idempotency-Key`
remains required (assessment-scoped: `(assessmentId, "REGENERATE_REVISION", idempotencyKey)`).
`adjustmentNotes` is persisted verbatim as the new `AssessmentRevision.reason`. Success is
`201` with the same response shape shown above (`origin: "AI_GENERATED"`). New error:
`409 STALE_REVISION` when `expectedRevisionId` doesn't match the current revision — checked
before any agent dispatch (no wasted LLM call), with a DB-level CAS backstop for the residual
concurrent-regenerate race (see "Conflict semantics").

## Endpoint removed
### PATCH /api/v1/assessments/{id}/draft
Confirmed gone: no `@PatchMapping` exists anywhere in `AssessmentController`. Returns
**405 Method Not Allowed** (not 404 — the path itself is still mapped for `GET`/`POST`, so
Spring's dispatcher recognizes the path and rejects only the method), verified by
`AssessmentControllerTest.patching_draft_returns_405_method_not_allowed_since_in_place_edit_was_removed`.

This session's own dedicated 405 test initially got `500`, not `405` — `GlobalExceptionHandler`
had no handler for `HttpRequestMethodNotSupportedException`, so it fell through to the
catch-all `Exception` handler. Fixed by adding a handler mapping it to
`405 { "error": "METHOD_NOT_ALLOWED", "message": "<http method>" }` (the legacy `ApiErrorResponse`
shape, not `ApiConflictErrorResponse` — this is a routing-validation failure, not an authoring
conflict). This was a real, previously-untested gap, not specific to the authoring cut.

## Request/response contract artifact for Web
The three examples above (retry, generation-status, create-human-revision) plus regenerate's
updated request shape are the literal, real JSON this branch produces — verified via
`AssessmentControllerTest`'s MockMvc assertions and this session's Testcontainers integration
tests, not hand-written from the contract doc. `GenerateAssessmentDraftResponse` (used by
create-human-revision, generate, regenerate, get-current-draft, list-draft-versions) and
`AiOperationResponse`/`GenerationStatusResponse` (retry/generation-status) are the only three
response DTOs in play; there is no separate "revision DTO" — `draftId` is genuinely the
`AssessmentRevision.id`.

## Status codes

**Corrected by A3 Contract Correction (2026-07-30) — see the addendum at the bottom of this
document.** This table and the note below it originally documented `POST .../draft` as
201-or-typed-error-only, never `202`. That was wrong: it resolved a real ambiguity in the
Authoring Operation Contract ADR in the wrong direction (see the superseded note, kept below for
audit history). The table now reflects the corrected, current behavior.

| Endpoint | Success | Typed errors |
|---|---|---|
| `POST /assessments/{id}/draft` | 201 (`RevisionCreated`), or **202** (`OperationAccepted` — a durable `AiOperation`/`AgentAttempt` was recorded but the dispatch failed: `AGENT_UNAVAILABLE`/`AGENT_ERROR`/`AGENT_REJECTED`/`INVALID_COMMAND`/`MALFORMED_OUTPUT`) | 404, 409 ALREADY_GENERATED, 409 IDEMPOTENCY_KEY_PAYLOAD_MISMATCH, 400 MISSING_HEADER |
| `POST /assessments/{id}/draft/regenerate` | 201 (unaffected by this correction — regenerate never leaves the assessment revision-less, so it keeps its 201-or-typed-error contract) | 404, 409 STALE_REVISION, 409 IDEMPOTENCY_KEY_PAYLOAD_MISMATCH, 400 MISSING_HEADER, 422, 502/503 |
| `POST /assessments/{id}/draft/retry` | **202 always** | 404, 409 NO_ACTIVE_OPERATION_TO_RETRY, 409 OPERATION_IN_PROGRESS |
| `GET /assessments/{id}/generation-status` | 200 always | 404 |
| `POST /assessments/{id}/revisions` | 201 | 404, 409 STALE_REVISION, 422 (validation) |
| `GET /assessments/{id}/draft` | 200 | 404 (no revision yet) |
| `GET /assessments/{id}/draft/versions` | 200 (possibly empty list — never 404 for zero revisions on an owned assessment) | 404 (assessment not found/owned) |
| `POST /api/v1/assessments` | 201 | 400 MISSING_HEADER, 409 IDEMPOTENCY_KEY_PAYLOAD_MISMATCH (both new in the correction) |
| `PATCH /assessments/{id}/draft` | — | **405** (path exists, method doesn't) |

**Superseded note (kept for audit history, no longer accurate — see correction above):** "Note
on generate/regenerate's '202' possibility: LOCAL-CONTRACTS.md's endpoint table lists `201 or
202` for generate. This implementation only ever returns `201` (success) or a typed error status
from generate/regenerate — never `202`. This is a deliberate, documented resolution of a real
ambiguity in the Authoring Operation Contract ADR (its prose mentions a `202`-for-durable-failure
path, but its own Typed Error Codes table marks response codes for generate/regenerate as
'(unchanged),' and its Open Consequences section explicitly defers '`202` not implemented
now'). Retry is the only endpoint that actually returns `202` in this codebase. This directly
affects Web C alignment item 3 below — flagged as REQUIRES WEB ADJUSTMENT IN D." This
resolution favored the ADR's own Typed Error Codes table and Open Consequences section over its
prose; the correction determined the prose (`POST .../draft` § "On failure ... 202 Accepted")
was in fact the intended, authoritative behavior for initial generation specifically (not
regenerate), and implements it accordingly. `422`/`502`/`503` no longer appear for generate,
since every dispatch failure now returns `202` with a typed `failureCode` in the body instead of
a bare HTTP error status — 422/502/503 remain reachable only from regenerate, unaffected by this
correction.

## Conflict semantics
All five typed 409s use the exact same body shape, `ApiConflictErrorResponse`:
```json
{ "code": "STALE_REVISION", "message": "Assessment <id>'s ... expectedRevisionId is no longer current" }
```
— **never** the legacy `{ "error": ..., "message": ... }` shape used by non-conflict errors
(404/422/etc., via `ApiErrorResponse`). Verified in `AssessmentControllerTest` with explicit
`jsonPath("$.code")`/`jsonPath("$.error").doesNotExist()` assertions for every conflict code.

- `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` — reachable (generate/regenerate, same key + different
  payload hash), tested (A1/A2).
- `ALREADY_GENERATED` — reachable (generate called twice for the same assessment), tested (A2).
- `STALE_REVISION` — reachable from regenerate and create-human-revision, both the
  application-level pre-check and the DB-level CAS/unique-constraint backstop paths, tested at
  both unit and Testcontainers-concurrency level in this and the prior two sessions.
- `NO_ACTIVE_OPERATION_TO_RETRY` — reachable (never generated; or the latest `AiOperation` is
  `SUCCEEDED`/`FAILED_TERMINAL`), tested (`RetryGenerationHandlerTest`,
  `RetryGenerationHandlerIntegrationTest`, `AssessmentControllerTest`).
- `OPERATION_IN_PROGRESS` — reachable (latest `AiOperation` is `PENDING`/`IN_PROGRESS` and its
  attempt is within the indeterminate threshold), tested at the handler-unit level (mocked
  `dispatchedAt`) and via the controller contract test; **not** separately exercised end-to-end
  against a real DB in this session (would require a genuinely long-running dispatch to hold the
  window open — the equivalent real-DB race is instead covered by the concurrent-retry test
  below, which hits the same code path from a different angle).

## Retry/resume behavior
Confirmed: `AiOperationCoordinator.retryInitialRevision` reuses the **same, already-existing**
`AiOperation` row (`existingOperation.markInProgress()`), never creates a new `AiOperation` or a
new `Assessment`. It dispatches a new `AgentAttempt` with `attemptNumber = <latest> + 1` under
that same operation id. `AiOperation.markInProgress()` was widened this session to also accept
`IN_PROGRESS → IN_PROGRESS` (previously only `PENDING`/`FAILED_RETRYABLE → IN_PROGRESS`),
narrowly for retrying an operation whose sole attempt has gone orphaned/indeterminate — RED
test `AiOperationTest.shouldAllowRetryTransitionFromInProgressBackToInProgressForAnIndeterminateOrphanedAttempt`
confirmed failing before the change, passing after.

**Concurrency backstop:** two concurrent retries of the same `FAILED_RETRYABLE` operation are
*not* protected by a dedicated CAS field on `AiOperation` (it has no `@Version`/optimistic-lock
column) — both threads' `UPDATE ai_operations SET status='IN_PROGRESS' ...` on the identical row
id succeed harmlessly (last-write-wins on the same row, not a race on distinct rows). The real
backstop is `agent_attempts`' own `UNIQUE(ai_operation_id, attempt_number)`: both threads
compute the same `nextAttemptNumber` from the same stale read, race to insert it, and the loser
gets a caught `DataIntegrityViolationException` → `StaleRevisionException` (409), propagated
uncaught from `RetryGenerationHandler.execute()` (retry's "always-202" swallow only covers
`AgentClientException`/`StaleOnCompletionException` from the coordinator's dispatch/completion
paths — a Phase-0 insert collision is a different failure class and is *not* swallowed).
Verified for real against Postgres by
`RetryGenerationHandlerIntegrationTest.shouldAllowExactlyOneOfTwoConcurrentRetriesOfTheSameFailedOperation`
(`TestTransaction`-based two-thread race, asserted exactly one `RetryGenerationResult` success
and one `StaleRevisionException`, and exactly one new `agent_attempts` row).

## OpenAPI impact
N/A — no OpenAPI generation in this codebase.

## Tests

New test classes this session (Task 10):
| Class | Tests | Covers |
|---|---|---|
| `RetryGenerationHandlerTest` | 10 | retry gating (NO_ACTIVE_OPERATION_TO_RETRY / OPERATION_IN_PROGRESS / indeterminate-past-threshold carve-out), always-202 swallow of both `AgentClientException` and `StaleOnCompletionException` |
| `RetryGenerationHandlerIntegrationTest` | 3 | real-DB happy path (second `AgentAttempt` under the same operation), no-operation-to-retry, **concurrent retry** |
| `GetGenerationStatusHandlerTest` | 9 | full status taxonomy incl. `SUCCEEDED`-via-`currentRevisionId` short-circuit, `NOT_STARTED`, `FAILED_TERMINAL`, `IN_PROGRESS` vs **`INDETERMINATE`** |
| `GetGenerationStatusHandlerIntegrationTest` | 2 | real-DB **`INDETERMINATE`** (genuinely persisted stale `DISPATCHED` attempt) vs real-DB `IN_PROGRESS` |
| `GetCurrentDraftHandlerTest` (rewritten) | 4 | migrated onto `AssessmentRevisionRepositoryPort` |
| `ListDraftVersionsHandlerTest` (rewritten) | 4 | migrated onto `AssessmentRevisionRepositoryPort` |
| `AssessmentControllerTest` (+15 methods) | 36 total | every new/changed endpoint's contract row, incl. the mandated 409 `{code,message}` shape checks and the **PATCH → 405** test |
| `AssessmentCreationFlowIntegrationTest` (extended) | 2 | whole-story flow now also exercises the three migrated read handlers post-regenerate |
| `AssessmentPersistenceAdapterIntegrationTest` (1 method fixed) | 5 | dashboard title now sourced from `AssessmentRevision`, not the legacy draft table — this was a genuine regression this session's own query rewrite caused and fixed before committing |
| `AiOperationTest` (+1 method) | 19 | `IN_PROGRESS → IN_PROGRESS` retry transition (RED confirmed, then GREEN) |

Every RED test explicitly mandated by the session prompt was written and confirmed RED before
implementation: human-edit stale revision, immutable revision chain (Task 08/09), regenerate
from an A2-created revision, provider/model in `MALFORMED_OUTPUT` (Task 09), retry without
operation, concurrent retry, generation-status `INDETERMINATE`, 409 `{code,message}`,
no-transaction-during-HTTP (pre-existing coverage from A2's `AiOperationCoordinatorTest`,
re-confirmed still green here).

## Baseline
Before this session: 405 (Session A2's recorded final count)
After Task 08 (isolated checkpoint): 405/405 — net-zero (old PATCH/update tests deleted,
replaced by equivalent new human-edit-revision tests)
After Task 09 (isolated checkpoint): 419/419
After this session (Task 10, final): **456/456**, 0 failures, 0 errors, 0 skipped
`./mvnw -f api/pom.xml clean test` → **PASS**, confirmed via a full, non-cached
(`clean test`) run against a real Testcontainers Postgres for every `@DataJpaTest` class, twice
in a row (once before, once after fixing the one regression described below).

### Docker incidents
The WSL2 Docker Desktop CLI (`/mnt/wsl/docker-desktop/cli-tools/usr/bin/docker`) intermittently
returned `Input/output error` throughout this session, as in prior sessions. Worked around by
prefixing `PATH` with `/Docker/host/bin` (the Windows-side binary via the 9p mount) before every
Testcontainers-dependent `mvn test` invocation: `export PATH="/Docker/host/bin:$PATH"`. Not a
code defect; every Testcontainers-backed test class in this session's final run passed.

### One regression found and fixed before committing
Migrating `AssessmentJpaRepository.findSummariesByTeacherUid`'s native query off the legacy
`assessment_drafts` `LEFT JOIN LATERAL` onto `assessment_revisions`/`current_revision_id` broke
`AssessmentPersistenceAdapterIntegrationTest.shouldUseCurrentDraftTitleWhenDraftExists` (it
seeded a legacy `AssessmentDraft`, which the new query no longer reads). Fixed by rewriting the
test to seed via `AssessmentRevision.restore(...)` + `Assessment.withCurrentRevision(...)`
instead — the correct fix per "the única fuente autoritativa is `Assessment.currentRevisionId`,"
not a workaround. Renamed to `shouldUseCurrentRevisionTitleWhenRevisionExists`.

## Known residual risks
- **`AssessmentDraftRepositoryPort`/`AssessmentDraftPersistenceAdapter`/`AssessmentDraftJpaRepository`/
  `AssessmentDraftPersistenceMapper` and `AgentExecutionLogRepositoryPort`/
  `AgentExecutionLogPersistenceAdapter`/`AgentExecutionLogJpaRepository`/
  `AgentExecutionLogPersistenceMapper`** are now confirmed **unreferenced by any application
  handler** (verified: `grep -rn "^import.*AssessmentDraftRepositoryPort\|^import.*AssessmentDraftPersistenceAdapter" src/main/java` and the equivalent for `AgentExecutionLogRepositoryPort` each return exactly one hit — the adapter class's own self-referencing import — plus `AssessmentConfig`'s now-orphaned `@Bean` methods for the draft adapter). `AssessmentDraft`/`AgentExecutionLog` domain classes, their JPA entities, and the `assessment_drafts`/`agent_execution_logs` tables themselves are untouched (LOCAL-CONTRACTS.md: "not dropped in this cut"). **This is the exact, complete scope for A4's Task 12/13 cleanup** — creating `V17` (backfill/migration), deleting these eight now-dead classes plus their `AssessmentConfig` bean wiring.
  **Corrected by A3 Contract Correction (2026-07-30):** A4 does **not** drop `assessment_drafts`/
  `agent_execution_logs` — both tables are retained, read-only, for one full release after the
  backfill/migration script (**V17 / API A4 Task 12** — not yet executed as of this handoff) has
  moved every row into `assessment_revisions`/`ai_operations`/`agent_attempts`. The line above
  previously stated A4 would "actually drop the two tables via a Flyway migration" — that claim
  was incorrect and is superseded by this note, not silently deleted, so the correction itself
  stays auditable. See the addendum at the bottom of this document.
- `OPERATION_IN_PROGRESS` is not exercised against a real, genuinely-still-running dispatch end
  to end (see "Conflict semantics" above) — low risk, since the same underlying state-machine
  branch is covered at the unit level and the equivalent real-DB race is covered by the
  concurrent-retry test.
- `POST /api/v1/assessments` (create-assessment-brief) has **no `Idempotency-Key` enforcement
  at all** in the current controller — `LOCAL-CONTRACTS.md`'s own endpoint table marks it
  "Required," but `AssessmentController.createAssessmentBrief` has no
  `@RequestHeader("Idempotency-Key")` parameter. This predates A3 (present since at least A1/A2)
  and was out of scope for Tasks 08-10, but is now directly relevant because Web C's own handoff
  (assumption #7, below) explicitly relies on create and generate having independent idempotency
  scopes — true for the scopes themselves (`TEACHER` vs `ASSESSMENT`), but the create leg is
  currently unenforced server-side. Flagging for A4 or a dedicated fix; not blocking, since an
  unenforced-but-sent header is harmless.
  **RESOLVED by A3 Contract Correction (2026-07-30):** `Idempotency-Key` is now required and
  enforced on `POST /api/v1/assessments` — see the addendum at the bottom of this document. This
  bullet is left in place, not deleted, as the historical record of the gap the correction closed.

## Blockers
None.

## Next session prerequisites
Confirm this handoff is complete, including the full contract artifact section above.
Re-run `./mvnw -f api/pom.xml clean test` yourself before trusting the test count above.

## Next session first command
Execute CLAUDE-API-A4-PROMPT.md.
```

## Web C (Session C) alignment — the 8 documented API assumptions

Read from `origin/feat/assessment-authoring-operation-foundation-web`'s own
`web/docs/implementation-packets/assessment-authoring-operation-foundation/HANDOFF.md`
(fetched read-only via `git fetch origin feat/assessment-authoring-operation-foundation-web`;
**never merged, never pushed to from this branch**). Authority order applied when resolving any
tension: ADRs > implementation plan (`docs/implementation-plans/...`) > `LOCAL-CONTRACTS.md` >
this A3 session prompt > the Web handoff's own assumptions.

1. **409 body shape `{ code, message }`** — **CONFIRMED.** Every typed 409 in this
   implementation (`IDEMPOTENCY_KEY_PAYLOAD_MISMATCH`, `ALREADY_GENERATED`, `STALE_REVISION`,
   `NO_ACTIVE_OPERATION_TO_RETRY`, `OPERATION_IN_PROGRESS`) uses exactly `ApiConflictErrorResponse
   { code, message }`, distinct from the legacy `{ error, message }` shape. Web's
   `ApiConflictErrorResponse` type in `src/types/assessment.ts` matches.

2. **Resume disambiguation trigger: `GET .../draft` 404 → then `GET .../generation-status`** —
   **CONFIRMED.** `GetCurrentDraftHandler` 404s when `Assessment.currentRevisionId == null`.
   `GetGenerationStatusHandler` independently verifies existence/ownership before deriving
   status, so a genuinely nonexistent/unauthorized assessment 404s from `generation-status`
   too — exactly the "second 404 disambiguates" behavior Web assumed, with no new endpoint
   needed.

3. **Initial-generate's `202` body is `AiOperationDto { id, status, failureCode? }`** —
   **CONFIRMED after A3 Contract Correction (2026-07-30).** At original A3 handoff time, `POST
   .../draft` and `POST .../draft/regenerate` never returned `202` — only `201` or a typed error
   status — and this section originally flagged Web's `202`-handling branch for generate as
   unreachable dead code, recommending Session D remove or mark it retry-only. **That
   recommendation is now reversed and Web's original assumption was correct**: the Authoring
   Operation Contract ADR's prose (`POST .../draft` § "On failure ... 202 Accepted") was the
   authoritative source, and the correction implements it — `POST .../draft` now returns `202`
   with an `AiOperationResponse`-shaped body (`id`, `operationType`, `status`, `failureCode`,
   `retryable`, `resultRevisionId`) whenever a durable `AiOperation`/`AgentAttempt` was recorded
   but the dispatch itself failed (`AGENT_UNAVAILABLE`/`AGENT_ERROR`/`AGENT_REJECTED`/
   `INVALID_COMMAND`/`MALFORMED_OUTPUT`), success still returns `201`. **Regenerate is
   unaffected** — it still only returns `201` or a typed error, since it never leaves an
   assessment revision-less to begin with (only initial generation can). Session D does **not**
   need to remove Web's generate-`202` branch after all; only regenerate's `202` branch (if Web
   built one) remains genuinely unreachable.

4. **Retry's success response is not interpreted; Web always re-polls afterward** —
   **CONFIRMED**, and this is exactly what makes item 3's shape mismatch harmless for retry
   itself: retry really does always return `202` (confirmed above), and because Web never
   branches on its body, the extra/renamed fields this implementation's `AiOperationResponse`
   carries beyond Web's assumed `AiOperationDto` cause no integration break.

5. **`expectedRevisionId` is `AssessmentDraftDto.draftId`** — **CONFIRMED.** `draftId` in
   `GenerateAssessmentDraftResponse` is genuinely `AssessmentRevision.id` — not a separate
   identifier — exactly as Web assumed, and exactly what regenerate/create-human-revision
   expect back as `expectedRevisionId`.

6. **Idempotency key format: any string accepted, no server-side format validation** —
   **CONFIRMED.** `Idempotency-Key` is read as a plain `@RequestHeader String` with no
   `@Pattern`/format constraint anywhere in the controller or `IdempotencyGuard`/
   `IdempotencyScope`; any non-empty string round-trips correctly through the idempotency-record
   lookup.

7. **Create-assessment and generate-draft use two independently generated keys, since they
   have different idempotency scopes** — **CONFIRMED after A3 Contract Correction (2026-07-30).**
   At original A3 handoff time this was "confirmed in scope semantics but not server-enforced" —
   `POST /api/v1/assessments` accepted no `Idempotency-Key` header at all server-side. The
   correction closes that gap: `Idempotency-Key` is now `@RequestHeader`-required on
   `POST /api/v1/assessments`, enforced via `IdempotencyScope.teacher(teacherUid)` +
   `CREATE_ASSESSMENT_BRIEF` (hash over `learningGoal`/`topic`/`level`/`duration`/`language`),
   fully independent from generate-draft's `IdempotencyScope.assessment(assessmentId)` +
   `CREATE_INITIAL_REVISION`. Web's original rationale ("different scopes") is now fully backed
   by real server enforcement on both legs, not just the create leg's own scope semantics.

8. **`isRecoverableDraftMutationStatus` widened to treat 409 as WARN, not ERROR** —
   **CONFIRMED** (Web-internal logging concern, no API surface implication) — consistent with
   this contract's actual design: `STALE_REVISION`/`ALREADY_GENERATED`/etc. are expected,
   user-actionable conflicts, not incidents.

9. **New instruction, not a Web C assumption — added by A3 Contract Correction (2026-07-30):**
   Session D must extend `GenerationStatusValue` (or equivalent) with `SUCCEEDED` and
   `FAILED_TERMINAL`, and render `FAILED_TERMINAL` as a non-retryable failure view distinct from
   `FAILED_RETRYABLE`. Web already resolves `SUCCEEDED` via `currentRevisionId` being non-null,
   so that value needs no new rendering path — only `FAILED_TERMINAL` is genuinely new for Web.
   See [LOCAL-CONTRACTS.md § Amendment (2026-07-30)](LOCAL-CONTRACTS.md#amendment-2026-07-30-succeeded-and-failed_terminal-made-explicit).

**Summary for Session D (updated 2026-07-30):** All 8 original Web C assumptions are now
CONFIRMED — items 3 and 7 flipped from their original A3 verdicts (REQUIRES WEB ADJUSTMENT / gap
noted) to CONFIRMED once the A3 Contract Correction closed the underlying gaps (initial-generate
`202`, create-assessment idempotency enforcement). Session D has one new, additive instruction
(#9, generation-status taxonomy) that was not part of Web's original 8 assumptions. Net effect
for Web: **no code needs to be removed** (the generate-`202`-handling branch Web already built is
not dead code after all); Web needs to **add** a `FAILED_TERMINAL` rendering path and send
`Idempotency-Key` on create (if it does not already).

## Provider/model post-resolution failure fix (A2-inherited gap, closed in Task 09)
`AgentAttempt.markFailed` was widened from a 1-arg `(failureCode)` signature to
`(failureCode, resolvedProvider, resolvedModel, structuredResult)` — a widened signature, not a
second overload, so no call site can silently keep discarding known provider/model evidence.
`resolvedProvider`/`resolvedModel` are `null` for failures before provider resolution
(`AGENT_UNAVAILABLE`, `INVALID_COMMAND`) and non-null, required, for failures known to have
resolved a provider first (`MALFORMED_OUTPUT`, `STALE_ON_COMPLETION`) — enforced by
`AgentAttempt.markFailed`'s own validation. `AiOperationCoordinator.persistDispatchFailure`
extracts these from `AgentClientException.agentError().log()` when present. Covered by
`AgentAttemptTest`, `AssessmentAgentClientTest`, and `AiOperationCoordinatorTest`'s
`MALFORMED_OUTPUT`-specific cases (all pre-existing from Task 09, re-confirmed green this
session).

## Transaction-boundary discipline
Confirmed preserved for every operation added/changed this session, per the three-phase
Durable AI Operation Model: Phase 0 (durable `AiOperation`/`AgentAttempt` evidence) commits in
its own `TransactionTemplate.executeWithoutResult` block before any HTTP call; the HTTP call to
`agents/` (`AssessmentAgentClient.generate`) happens with **no open transaction**; Phase 2
(CAS-protected persist) runs in its own separate transaction. `retryInitialRevision` reuses the
exact same `dispatchAndPersist`/`runPhase2` machinery `createInitialRevision`/`regenerateRevision`
already use — no new transaction-boundary code path was introduced, only a new caller.
`CreateHumanRevisionHandler` (Task 08) uses its own single `TransactionTemplate` for the
CAS-protected human-edit write, with no external network call in scope at all (no `agents/` call
for a human edit), so there is no "outside-transaction HTTP" concern for that path by
construction. `RetryGenerationHandler`/`GetGenerationStatusHandler` themselves hold no
transaction open around their own read/coordinate calls — verified via the existing
no-transaction-during-HTTP test coverage inherited from A2's `AiOperationCoordinatorTest`,
still green.

---

## A3 Contract Correction (2026-07-30)

Executed on the same branch, on top of A3's declared final HEAD, per
`CLAUDE-API-A3-CORRECTION-PROMPT` — no new branch, no PR, no merge. Corrects five contract
deviations found after A3's original declared completion; every section above that described
now-superseded behavior was corrected in place with an inline note, not silently rewritten.

**Previous declared final HEAD:** `3f820e34ec01c7eff39747be03de24ea9a98e897`
**Correction implementation HEAD:** `13cb613` (`fix(api): align generation and retry responses with durable operation contract`)
**Previous correction documentation commit:** `69c7f437d09276490d4045fb0b3791c629b10ec9`
(supersedes the `16ac15d` value this line previously recorded — that value was the pre-amend hash
of the same commit; a commit's hash cannot honestly be written into that commit's own content, so
recording the still-mutating hash here was a mistake this line corrects, not a mystery discrepancy)

### What changed

1. **Create-assessment idempotency applied** — `POST /api/v1/assessments` now requires
   `Idempotency-Key`, enforced via `IdempotencyGuard` (`IdempotencyScope.teacher(teacherUid)`,
   operation `CREATE_ASSESSMENT_BRIEF`, hash over `learningGoal`/`topic`/`level`/`duration`/
   `language`). Same key + same payload replays the original `assessmentId`; same key + different
   payload → `409 IDEMPOTENCY_KEY_PAYLOAD_MISMATCH`; missing header → `400`. The
   Assessment/AssessmentBrief/IdempotencyRecord triad is written atomically in one
   `TransactionTemplate`-wrapped transaction (`CreateAssessmentBriefHandler`), so a genuinely
   failed creation leaves no idempotency record. A concurrent duplicate request racing on the
   same key/payload is resolved via `idempotency_records`' own unique constraint: the loser's
   transaction rolls back entirely and it replays the winner's committed record instead of
   surfacing the raw constraint violation — exactly one `Assessment` results either way.
2. **Generate 201/202 real** — `POST /assessments/{id}/draft` now returns `201` with the new
   `AssessmentRevision` on success, or `202` with an `AiOperationResponse`-shaped body
   (`GenerateAssessmentDraftOutcome.OperationAccepted`) whenever the coordinator's Phase 0 already
   durably recorded an `AiOperation`/`AgentAttempt` pair before the dispatch itself failed
   (`AGENT_UNAVAILABLE`, `AGENT_ERROR`, `AGENT_REJECTED`, `INVALID_COMMAND`, `MALFORMED_OUTPUT`,
   or a `STALE_ON_COMPLETION` terminal race outcome). The idempotency record is written for both
   outcomes (with the outcome's own `responseStatus`, `201` or `202`), so a replay of the same key
   after a `202` returns the same operation snapshot without dispatching a new `AgentAttempt`.
   Regenerate is **unchanged** — `201` or a typed error only, never `202`, since it never leaves
   the assessment revision-less.
3. **Six-value `generation-status` taxonomy formalized** — the read model already returned
   `SUCCEEDED`/`FAILED_TERMINAL` explicitly in the code shipped with the original A3 session (see
   "Status codes" above, and `GetGenerationStatusHandler`'s own javadoc); this correction's real
   work was documenting that deliberately, as a closed six-value set, in the ADR and both contract
   packets, rather than leaving it as an unstated implementation detail contradicting
   LOCAL-CONTRACTS.md's original four-value sketch. Amended:
   `docs/99-decisions/2026-07-28-authoring-operation-contract.md`,
   `docs/implementation-packets/assessment-authoring-operation-foundation/03-cross-workspace-api-contracts.md`,
   `api/docs/implementation-packets/assessment-authoring-operation-foundation/LOCAL-CONTRACTS.md`
   — each with a dated, clearly-labeled amendment section, not a silent rewrite. Added exhaustive
   MVC-level contract tests for all six states (previously only `INDETERMINATE` was tested at the
   controller layer; the other five were unit-tested at the handler level only).
4. **Concurrent retry race corrected** — two concurrent retries of the same `FAILED_RETRYABLE`
   operation used to both surface as `409 STALE_REVISION` (via a generic Phase-0
   `DataIntegrityViolationException` → `StaleRevisionException` mapping shared with
   create/regenerate). `AiOperationCoordinator.dispatchAndPersist` now takes a
   `phase0ConflictException` supplier: `createInitialRevision`/`regenerateRevision` still map
   their Phase-0 collision (always `uq_ai_operations_in_flight`, since they always insert a
   brand-new `AiOperation` row) to `StaleRevisionException`; `retryInitialRevision` maps its
   Phase-0 collision (always `agent_attempts`' `UNIQUE(ai_operation_id, attempt_number)`, since it
   reuses an existing operation id and can never hit the in-flight index) to
   `OperationInProgressException` — an existing failure code, no new one introduced. Winner: `202`.
   Loser: `409 { "code": "OPERATION_IN_PROGRESS", "message": "..." }`. Verified for real against
   Postgres by `RetryGenerationHandlerIntegrationTest.shouldAllowExactlyOneOfTwoConcurrentRetriesOfTheSameFailedOperation`,
   updated to assert `OperationInProgressException` (never `StaleRevisionException`) for the loser.
5. **Legacy tables retained, not dropped** — `API-A3-HANDOFF.md`'s "Known residual risks" section
   previously stated A4 would "actually drop the two tables via a Flyway migration." Corrected in
   place above: A4's real scope is `V17` (backfill), deleting the eight dead legacy classes and
   their `AssessmentConfig` wiring — `assessment_drafts`/`agent_execution_logs` remain, read-only,
   for one full release.

### Test count

Before this correction: **456/456** (A3's own final baseline, re-verified clean at the start of
this session). After: **477/477**, 0 failures/errors/skipped.
`./mvnw -f api/pom.xml clean test` → PASS.

### Web adjustments remaining for Session D

- Add a `FAILED_TERMINAL` rendering path to `GenerationStatusValue` (non-retryable, distinct from
  `FAILED_RETRYABLE`); `SUCCEEDED` needs no change (Web already resolves it via
  `currentRevisionId`).
- Send `Idempotency-Key` on `POST /api/v1/assessments` if not already sent (Web's own handoff
  assumption #7 already implied it would; this correction makes the header genuinely required and
  enforced, not silently ignored).
- Web's `202`-handling branch for initial generation is **not** dead code after all — keep it. No
  code needs to be removed from Web because of this correction, only added.

### Blockers

None.

---

## A3 Final Idempotency Correction (2026-07-30)

Executed on the same branch, on top of the A3 Contract Correction's declared final HEAD, per the
"API A3 Final Idempotency Correction — Do Not Start A4" session prompt — no new branch, no PR, no
merge. Closes the last idempotency-atomicity gap: the functional result of an AI dispatch (a
revision, or a durable failure) and its `IdempotencyRecord` were written in two separate
transactions, so a crash between them could leave a completed/failed operation with no replay
record — risking a second, paid LLM dispatch for what should have been a no-op replay.

**Starting HEAD:** `69c7f437d09276490d4045fb0b3791c629b10ec9`
**Final idempotency correction implementation HEAD:** `4fadbdc` (`fix(api): persist AI idempotency outcomes atomically`)
**Final metadata/handoff commit:** This document's own commit; its hash is reported in the
external final report, not embedded here — a commit's hash cannot honestly be written into that
commit's own content (see the corrected line above this addendum for why that matters).

### What changed

1. **Atomic idempotency writes** — `AiOperationCoordinator` now takes an injected `IdempotencyGuard`
   and an optional `IdempotencyCompletionContext` (scope/operationType/idempotencyKey/payloadHash,
   built by the caller before dispatch). The `IdempotencyRecord` is written *inside* the same Phase
   2 transactional callback that produces the durable outcome it describes — success (`201`,
   `resultReference` = revision id), a dispatch failure (`202` for initial generation; the real
   original HTTP status for regenerate, derived from the failure code), or a stale-on-completion
   race — never in a separate transaction afterward. `GenerateAssessmentDraftHandler`/
   `RegenerateAssessmentDraftHandler` no longer call `idempotencyGuard.record(...)` themselves for
   the dispatch path; they only build the `IdempotencyCompletionContext` and hand it to the
   coordinator. `retryInitialRevision` passes no context at all (retry has no idempotency key of
   its own), unchanged.
2. **Same-key concurrency resolved without a spurious conflict** — a Phase 0
   `uq_ai_operations_in_flight` collision now looks up the winning in-flight `AiOperation`: if it
   carries the SAME idempotency key as the request that just lost the race, this is a genuine
   duplicate submission, not a real conflict — reported as `OperationInProgressException`
   (an existing code, not a new one) instead of `StaleRevisionException`. `GenerateAssessmentDraftHandler`
   catches this alongside `AgentClientException`/`StaleOnCompletionException` and relays the
   winner's snapshot as `202`; `RegenerateAssessmentDraftHandler` lets it propagate as
   `409 OPERATION_IN_PROGRESS` (regenerate never returns `202`). A DIFFERENT key still gets the
   unchanged `StaleRevisionException` behavior — this correction only changes the same-key case.
3. **Recovery for pre-fix orphaned operations** — before dispatching, both handlers now look up the
   latest `AiOperation` for `(assessmentId, operationType)`; if it carries the SAME idempotency key
   and has no matching `IdempotencyRecord` (data from before this correction, or a sibling request
   whose Phase 2 hasn't committed yet), its state is relayed instead of dispatching again: still
   in flight → the existing snapshot (create: `202`; regenerate: `409 OPERATION_IN_PROGRESS`);
   terminal-success → the existing revision, record backfilled; terminal-failure → the original
   outcome, record backfilled (regenerate: the exact original exception, reconstructed — see below).
   A different (or absent) key falls through to the normal precondition/dispatch path, unchanged.
4. **Regenerate failure replay reconstructs the original exception** — regenerate never returns
   `202`; a durable dispatch failure is recorded under the real HTTP status its failure code maps
   to (`AGENT_UNAVAILABLE`→503, `AGENT_ERROR`→502, `STALE_ON_COMPLETION`→409, everything else→422),
   via the new package-private `RegenerateFailureReplay` helper. A replay (via the idempotency
   record or via recovery) reconstructs and throws the equivalent `AgentClientException`/
   `StaleOnCompletionException` from the durably-stored `AgentAttempt.failureCode` — the client
   sees the same status, and `resolvedProvider`/`resolvedModel` on the original attempt are
   untouched (never overwritten by a replay), so "provider/model preservados" holds by
   construction, not by re-deriving them.

### Test count

Before this correction: **477/477** (A3 Contract Correction's own final baseline, re-verified
clean at the start of this session). After: **495/495**, 0 failures/errors/skipped.
`./mvnw -f api/pom.xml clean test` → PASS.

### Blockers

None.

---

← [README](README.md) | [↑ inicio](#top)

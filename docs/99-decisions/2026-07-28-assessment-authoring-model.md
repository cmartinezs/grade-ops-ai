<a id="top"></a>

# Assessment Authoring Model

- Status: Accepted
- Date: 2026-07-28
- Decision owner: Technical
- Informed by: [Research 01 — Current Domain & Data Model Alignment Audit](../../design-system/research/research-01-current-domain-data-model-alignment-audit.md), [Research 02 — Workflow & Lifecycle Alignment Audit](../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md)
- Constrained by: [Assessment Operations Product Redesign](2026-07-27-assessment-operations-product-redesign.md), [Assessment Domain Foundations (UI/UX)](../../design-system/decisions/2026-07-27-assessment-domain-foundations.md)
- Related decisions: [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md), [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md), [Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md)

## Context

The executable authoring slice (`Assessment`, `AssessmentBrief`, `AssessmentDraft`, `AgentExecutionLog`) was built before the 2026-07-27 product redesign and before the UI/UX discovery decisions in `design-system/decisions/`. Research 01 classified it as a valid but incomplete foundation: `Assessment` is a correct identity anchor with no behavior; `AssessmentDraft` mixes four different concerns — immutable AI proposal, current editable document, provenance and version — in one mutable row; human edits (`AssessmentDraft.applyEdit`) overwrite that row in place, destroying the AI/human provenance boundary the redesign requires.

The UI/UX discovery decisions ([`assessment-domain-foundations.md`](../../design-system/decisions/2026-07-27-assessment-domain-foundations.md), [`assessment-template-lifecycle-governance.md`](../../design-system/decisions/2026-07-27-assessment-template-lifecycle-governance.md)) already introduce a broader target vocabulary — `AssessmentTemplate`, `AssessmentTemplateVersion`, `AppliedAssessmentProfile`, and independent preparation/application/review/publication cycles — but explicitly stop short of physical schema, aggregates or endpoints ("Esta decisión no autoriza todavía clases, tablas, endpoints ni contratos físicos"). This ADR is the technical design work those decisions and the redesign ADR both call for before implementation: it resolves the authoring data model for the `Assessment Authoring Operation Foundation` cut only — identity, revisions and provenance — without building templates, preparation readiness, or publication.

## Problem

Four things must be resolved before any code changes: (1) what `Assessment` identity means versus its content, (2) how a content change — AI or human — is captured without destroying history, (3) how "the current version" is identified without a race-prone `MAX(version_number)` query, and (4) what happens to `AssessmentStatus` so nobody extends it into a universal lifecycle enum.

## Decision

### Identity, intention, and content are three separate records

```text
Assessment            — stable identity, owner, creation time. No content.
AssessmentBrief       — authoring intent (learningGoal/topic/level/duration/language). One per Assessment, immutable after creation in this cut.
AssessmentRevision    — one immutable snapshot of authored content. 1..N per Assessment.
```

`Assessment` remains a minimal aggregate root: id, owner (`teacherUid`), `status`, `createdAt`, plus two new fields introduced by this decision: `currentRevisionId` (nullable FK, set once the first revision exists) and `lockVersion` (optimistic lock — see [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md)). It gains no new domain methods (`open`, `approve`, `publish`, etc.) in this cut — those belong to future preparation/application aggregates the UI/UX discovery already anticipates (`AssessmentAdministration`, `ResultPublication`), not to this one.

`AssessmentBrief` is unchanged structurally. It stays a separate aggregate, and this cut does not add a way to edit it — see [Deferred: brief mutability](#deferred-brief-mutability).

### `AssessmentDraft` is replaced by `AssessmentRevision`

`AssessmentRevision` is the evolution of `AssessmentDraft`, corrected on the one point Research 01/02 both flagged as `CONFLICTING`: it is **immutable once created**. There is no `applyEdit()`-equivalent that mutates an existing row. Every accepted content change — AI-generated or human-edited — inserts a new row.

```text
AssessmentRevision
  id
  assessmentId
  versionNumber          (1, 2, 3… monotonic per assessment)
  previousRevisionId     (null iff versionNumber == 1)
  origin                 (AI_GENERATED | HUMAN_EDITED | LEGACY_UNKNOWN*)
  actorId                (teacherUid; nullable only for LEGACY_UNKNOWN)
  reason                 (nullable free text — regeneration adjustment notes, edit rationale)
  sourceAgentAttemptId   (nullable FK; set only when origin == AI_GENERATED — see Durable AI Operation Model)
  title, context, instructions, objectives, deliverables, constraints   (unchanged content shape)
  createdAt
```

`*LEGACY_UNKNOWN` is never written by application code — it exists only as a migration-time value for rows whose true origin cannot be reconstructed. See the implementation plan's [legacy migration honesty section](../implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md).

Content stays a full snapshot per revision, not a diff/patch. This matches the pattern `AssessmentDraft.regenerate()` already uses today and what `AssessmentTemplateVersion` publishing does in the UI/UX discovery decision ("congela su contenido"). A derived diff-from-previous for display is an acceptable additive feature later; it is not the source of truth and is not required for correctness.

### Current revision is an explicit pointer, not a derived query

Today's "current version" is `MAX(version_number)` per assessment (`AssessmentDraftPersistenceAdapter`). Research 01/02 both flag this as unenforced and race-prone under concurrent regeneration. This decision replaces it with `Assessment.currentRevisionId`, updated only inside the same transaction that inserts the new revision, guarded by `Assessment.lockVersion` (full compare-and-set semantics are specified in the [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md)). `versionNumber` remains on `AssessmentRevision` for human-readable ordering, history display, and as the value a client states when requesting a change ("I am regenerating from version 3"), but it is no longer the mechanism that determines which row is current.

### Provenance on every path

| Path | Origin | Actor | Reason | previousRevisionId | sourceAgentAttemptId |
|---|---|---|---|---|---|
| Initial generation | `AI_GENERATED` | requesting teacher | null | null | set |
| Regeneration | `AI_GENERATED` | requesting teacher | `adjustmentNotes` (now persisted — today it is sent to `agents/` but dropped, see Research 02 §11 "Regenerate draft" gaps) | prior current revision | set |
| Human edit | `HUMAN_EDITED` | editing teacher | optional edit note | prior current revision | null |

The AI-generated content of a revision is never overwritten by a subsequent human edit — the edit is a new row with `origin = HUMAN_EDITED` and `previousRevisionId` pointing at the AI-generated one. Both remain readable forever. This directly closes CRITICAL-01/CRITICAL-06 (Research 01/02): "after an edit, the stored record can no longer prove which fields came from AI."

### Revision chain integrity

1. A revision is immutable once created — no UPDATE ever touches its content columns.
2. `versionNumber == 1` iff `previousRevisionId IS NULL`.
3. When present, `previousRevisionId` must reference a revision with the same `assessmentId` and `versionNumber - 1`. Today's `assessment_drafts` self-FK does not enforce either constraint (Research 01 §6.2). This cut enforces both at the application layer, in the same transaction that creates the revision (mirrors the existing `AssessmentDraft.restore()` guard, extended with the assessment-match check), and proves it with a dedicated integration test (`AssessmentRevisionChainIntegrityTest`) rather than a database trigger — a covering index plus one authorial code path (all revision creation goes through one factory/use-case) makes this tractable without stored procedures.
4. `Assessment.currentRevisionId`, once non-null, always references a revision belonging to that same assessment — guaranteed structurally because it is only ever set to the id of a revision just inserted in the same transaction, by the same use case, for that assessment.
5. `AssessmentBrief` is immutable in this cut.
6. Authoring logic never reads or branches on `Assessment.status`.

### Aggregate boundary

`Assessment`, `AssessmentBrief`, and `AssessmentRevision` remain three separate aggregates (matching today's structure), not one merged aggregate. The invariant that crosses them — "the assessment's current-revision pointer must reference a just-created revision of that same assessment" — is enforced by the application service performing both writes in one local transaction, which is normal and does not require merging the aggregates. This resolves Research 02's BLOCKER-02 *for this cut's scope only*: assessment definition vs. run/application boundaries remain future work, explicitly out of scope here.

### `AssessmentStatus` is frozen, not extended, not repurposed

`AssessmentStatus.{DRAFT, OPEN, GRADING, CLOSED}` is untouched by this decision — no new values, no removed values, no domain transition methods added. Authoring (brief → revisions) does not gate on it and does not update it. It is explicitly reserved, conceptually, for a future *application/preparation* dimension the UI/UX discovery decision already describes (`AssessmentAdministration`'s `NOT_STARTED → OPEN → PAUSED → CLOSED`, and `Assessment`'s own `DRAFT → READY → SCHEDULED → PUBLISHED` preparation cycle) — but no code implementing that dimension is introduced here. This is a documentation clarification with zero implementation impact, and it directly forecloses the failure mode both research reports warn about: extending one enum to cover authoring, application, grading, and publication at once.

### What belongs to authoring, for this cut

Authoring has no independent status enum of its own. Its observable state is derived, not stored:

```text
brief saved                          — AssessmentBrief exists
generation available                 — brief exists, currentRevisionId is null, no non-terminal AiOperation
generation in progress               — a non-terminal AiOperation exists for this assessment
generation failed, retryable         — the latest AiOperation for this assessment is FAILED_RETRYABLE
current revision available           — currentRevisionId is non-null
```

This is a read model, detailed in the [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md), not a new column on `Assessment`.

## Decision Drivers

- Research 01 CAP-011, CRITICAL-06: human edits must stop destroying AI provenance.
- Research 02 CRITICAL-01, CRITICAL-06, WF-004: the same finding from the workflow angle, plus the warning against extending `AssessmentStatus` into a universal lifecycle.
- `docs/99-decisions/2026-07-27-assessment-operations-product-redesign.md`: "No single `status` may be treated as the authoritative state for all of them," "Later edits create a new version; they do not mutate historical attempts or results silently."
- `design-system/decisions/2026-07-27-assessment-domain-foundations.md` invariant 9: "Una propuesta de IA y una decisión docente son registros diferentes," and invariant 8: "La definición de una evaluación publicada no cambia silenciosamente" (this cut has no publication yet, but the immutability discipline must already hold for authoring).
- Keep the aggregate small (explicit user constraint: do not turn `Assessment` into a god aggregate).

## Alternatives Considered

| Option | Description | Verdict |
|---|---|---|
| A — Minimal patch | Keep `AssessmentDraft`, add `actor`/`timestamp` columns, stop `applyEdit()` from mutating in place by making it insert instead | Rejected: still derives "current" from `MAX(version_number)`, still has no explicit pointer for CAS, still conflates "content aggregate" and "revision pointer" the way Research 01 §11 describes; treats the symptom, not the structure |
| B — Event-sourced revisions | Append-only patch/diff events plus periodic snapshots | Rejected for this cut: no current consumer needs patch-level replay; full snapshots are simpler, match the existing regenerate pattern, and are sufficient for every acceptance criterion in scope. Revisit only if patch-level diffing becomes a real product requirement (e.g., collaborative editing) |
| C — Destructive rename | Rename `assessment_drafts` → `assessment_revisions` and alter columns in place | Rejected: breaks the additive-migration principle, forces a hard cutover with no path to honestly label pre-existing rows whose human-edit provenance cannot be reconstructed (Research 01 §13, "Legacy compatibility strategy") |
| D — New `AssessmentRevision` aggregate, additive migration | As decided above | **Adopted** |

## Consequences

- Every content mutation becomes an INSERT, not an UPDATE. More rows, no silent data loss, and a straightforward audit trail per assessment.
- `assessments` gains two columns (`current_revision_id`, `lock_version`); a new `assessment_revisions` table replaces `assessment_drafts` as the write path going forward.
- API response shapes for "the current draft" gain `origin`, `actorId`, `reason`, `previousRevisionId` — detailed in the [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md).
- The existing in-place `PATCH .../draft` endpoint's semantics change from "mutate the current row" to "create a new `HUMAN_EDITED` revision" — the concrete endpoint shape is an Operation Contract decision, not this one.
- Web's "current draft" read switches from "highest version returned by the API" to "the revision `currentRevisionId` points to," which the API already resolves server-side — no behavior change is required in Web's read path beyond consuming the richer response shape.

## Migration Impact

- Additive: `assessment_revisions` table and two new `assessments` columns are created before any read/write path changes.
- Existing `assessment_drafts` rows are backfilled into `assessment_revisions` in a dedicated Flyway data migration (see [06-database-migration.md](../implementation-plans/assessment-authoring-operation-foundation/06-database-migration.md)). Rows that were never touched by `PATCH .../draft` migrate as `AI_GENERATED` with confidence; the row that was the *current* version at migration time for any assessment that has ever received a `PATCH` **cannot** be honestly labeled `AI_GENERATED` (its content may have been edited, but its `agentExecutionLogId` still points at the original AI run) — it migrates as `LEGACY_UNKNOWN` with `provenanceComplete = false` recorded in a migration note column. No approvals, publications, or human decisions are fabricated for any legacy row.
- `assessment_drafts` and `agent_execution_logs` are kept, read-only, for one release after cutover, then retired in a follow-up task — not in this cut.

## Compatibility Impact

- Single internal consumer (`web/`) — there is no third-party API consumer to preserve indefinite dual contracts for. The plan updates Web in the same cut rather than maintaining two parallel contracts.
- `AssessmentId`, `TeacherId`, `AuthProvider`, the hexagonal package structure, and the "external call outside the DB transaction" pattern are explicitly kept (Research 01 §17, "What should not be discarded").

## Security Impact

- Ownership stays server-side (`OwnershipVerifier` comparing `teacherUid`), unchanged by this decision.
- `actorId` on every revision must be the authenticated teacher from the security context, never a client-supplied value — this closes a latent forgeable-provenance risk before revisions become audit-relevant.

## Testing Impact

- Unit: revision chain invariants (version 1 has no predecessor, N>1 requires a same-assessment predecessor at N-1), immutability (no setter/update path exists on content fields after construction).
- Integration (Testcontainers/PostgreSQL): `AssessmentRevisionChainIntegrityTest` proving the FK-chain and current-pointer invariants against a real schema; migration test proving `assessment_drafts` → `assessment_revisions` backfill preserves row counts and produces no fabricated `AI_GENERATED`/`HUMAN_EDITED` labels for ambiguous rows.
- No test may assert that a human edit reuses the previous revision's id/version — the current `AssessmentDraftTest`/`UpdateAssessmentDraftHandlerIntegrationTest` assertions that a `PATCH` preserves version/id are the ones this decision explicitly reverses (Research 01 test-coverage table, "Human edit creates immutable history — No — Current test asserts the opposite").

## Open Consequences (Explicitly Deferred)

- **Brief mutability** <a id="deferred-brief-mutability"></a>: whether `AssessmentBrief` can ever be edited after creation, and whether that invalidates the current revision or only affects future regenerations, is not decided here. No endpoint to mutate it is introduced in this cut.
- **`AppliedAssessmentProfile` convergence**: `AssessmentRevision.content` is a narrower, concrete precursor of the multidimensional profile `assessment-domain-foundations.md` describes. This ADR does not implement templates, profiles, or the preparation/readiness cycle; a future ADR must decide how `AssessmentRevision` content maps onto `AppliedAssessmentProfile` when templates are introduced.
- **`AssessmentStatus` retirement or reuse**: reserved conceptually for a future application/preparation aggregate per the Decision above; not scheduled here.
- **Assessment definition vs. run/application aggregate boundary**: full resolution (Research 02 BLOCKER-02) remains future work; this ADR only fixes the boundary between `Assessment` and `AssessmentRevision`.

## Related Decisions

- [Authoring Operation Contract](2026-07-28-authoring-operation-contract.md) — the endpoints/commands that create these revisions.
- [Idempotency and Concurrency Strategy](2026-07-28-idempotency-and-concurrency-strategy.md) — the CAS mechanics behind `currentRevisionId`/`lockVersion`.
- [Durable AI Operation Model](2026-07-28-durable-ai-operation-model.md) — what `sourceAgentAttemptId` points to.
- Superseded practice (not a superseded ADR — no prior ADR existed for this): the in-place-edit behavior implemented by `AssessmentDraft.applyEdit()`.

## References

- [Research 01 — Current Domain & Data Model Alignment Audit](../../design-system/research/research-01-current-domain-data-model-alignment-audit.md)
- [Research 02 — Workflow & Lifecycle Alignment Audit](../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md)
- [Assessment Operations Product Redesign](2026-07-27-assessment-operations-product-redesign.md)
- [Assessment Domain Foundations (UI/UX)](../../design-system/decisions/2026-07-27-assessment-domain-foundations.md)
- [Assessment Template Lifecycle Governance (UI/UX)](../../design-system/decisions/2026-07-27-assessment-template-lifecycle-governance.md)
- `api/src/main/java/cl/gradeops/ai/api/assessment/domain/model/Assessment.java`, `AssessmentDraft.java`, `AssessmentStatus.java`
- `api/src/main/resources/db/migration/V9__add_assessments.sql`..`V12__add_agent_execution_logs.sql`

---

← [ADR index](README.md) | [↑ inicio](#top) | [Siguiente: Authoring Operation Contract →](2026-07-28-authoring-operation-contract.md)

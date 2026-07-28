<a id="top"></a>

# 02 — Shared Domain Contract

**Parent:** [README](README.md) · **Status:** Ready — frozen · **Prev:** [01 — Workspace Responsibility Matrix](01-workspace-responsibility-matrix.md) · **Next:** [03 — Cross-Workspace API Contracts →](03-cross-workspace-api-contracts.md)

Every local packet must reproduce or reference this document precisely. No workspace defines its own name, shape, or meaning for a concept listed here. Field-level detail is in [02 — Domain and Data Changes](../../implementation-plans/assessment-authoring-operation-foundation/02-domain-and-data-changes.md) of the plan; this document is the ownership summary, not a re-derivation.

## Concept ownership table

| Concept | Owner | Purpose | Mutable | Created by | Read by |
|---|---|---|---|---|---|
| `Assessment` | API | Stable identity, `teacherUid` owner, `currentRevisionId` pointer, `lockVersion` | `currentRevisionId`/`lockVersion` only, via CAS | API (create-intent use case) | API; WEB (via API responses) |
| `AssessmentBrief` | API | Authoring intent (learningGoal/topic/level/duration/language) | No — immutable in this cut | API (create-intent use case) | API; WEB |
| `AssessmentRevision` | API | One immutable content snapshot | No | API (`generateFromAi`/`regenerateFromAi`/`createFromHumanEdit`) | API; WEB |
| `RevisionOrigin` | API (enum) | `AI_GENERATED` \| `HUMAN_EDITED` \| `LEGACY_UNKNOWN` | N/A | API | API; WEB |
| `AiOperation` | API | Durable functional intent, survives every user-initiated retry | Status transitions only | API (coordinator Phase 0) | API; WEB (via `generation-status`) |
| `AgentAttempt` | API (persistence) / AGENTS (content) | One concrete dispatch record | Status + terminal fields only | API inserts the row (Phase 0); AGENTS supplies `resolvedProvider`/`resolvedModel`/tokens/cost/`structuredResult`, API persists them (Phase 2) | API only — not exposed to WEB in this cut |
| `IdempotencyKey` | WEB generates / API enforces | Client-supplied token proving "same logical request" | N/A — opaque string | WEB (UUID per submit action) | API (idempotency guard) |
| `ExpectedRevisionId` | WEB supplies / API validates | CAS precondition for mutating calls | N/A | WEB (tracks last-fetched `currentRevisionId`) | API |
| `CorrelationId` | API originates / AGENTS echoes | Traceability across the API↔Agents boundary | N/A | API (per outbound call to `agents/`) | API (persists on `AgentAttempt`); AGENTS (logs, echoes back in response) |
| `FailureCode` | Shared taxonomy, API is persistence owner | Typed reason an operation/attempt failed | N/A | AGENTS (detailed reason, e.g. `INVALID_COMMAND`/`MALFORMED_OUTPUT`) or API's transport layer (`AGENT_UNAVAILABLE`/`AGENT_ERROR`/`AGENT_REJECTED`) | API (persists verbatim); WEB (renders) |
| `Provider` | AGENTS resolves | The actually-used LLM provider | N/A per attempt | AGENTS (`AssessmentGenerationPortSelector`) | API (persists as `resolvedProvider`, never invents or defaults it); WEB (may display, not required this cut) |
| `Model` | AGENTS resolves | The actually-used model | N/A per attempt | AGENTS | API (persists as `resolvedModel`); WEB (may display) |
| `TokenUsage` | AGENTS computes | `estimatedInputTokens`/`estimatedOutputTokens` | N/A | AGENTS | API (persists on `AgentAttempt`) — internal evidence, not exposed to WEB this cut |
| `Cost` | AGENTS computes | `costEstimate` (`NUMERIC`, never `Double`) | N/A | AGENTS | API (persists on `AgentAttempt`) — internal evidence, not exposed to WEB this cut; explicitly not a billing ledger |

## Shared invariants (identical across every workspace)

These are reproduced verbatim (not reworded) in every local packet's `LOCAL-CONTRACTS.md`.

### Revisions are immutable

A revision, once created, is never modified. Every human edit creates a new revision. Every AI regeneration creates a new revision. The original AI content is never overwritten — it remains readable through its own revision row forever.

### Provenance is explicit on every revision

Every revision records: `origin`, `actorId`, `createdAt`, `previousRevisionId`, `sourceAgentAttemptId` (when applicable), legacy-provenance completeness. Minimum origins: `HUMAN_EDITED`, `AI_GENERATED`, `LEGACY_UNKNOWN`. `LEGACY_UNKNOWN` is a migration-only value — application code never writes it.

### Current revision is an explicit pointer

`MAX(version_number)` is never the source of truth. `Assessment.currentRevisionId` is the only mechanism, protected by CAS/optimistic concurrency (`lockVersion`).

### Idempotency

Same key + same payload → same operation, same `Assessment`, no duplicate effect, no re-consumed AI call. Same key + different payload → `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH` (see [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md#canonical-failure-code-taxonomy) for the exact code name — this is the ADR's actual code, not a generic placeholder).

### Concurrency

Mutations carry `expectedRevisionId`. Mismatch → `STALE_REVISION`. There is no silent last-write-wins path anywhere in this cut.

### Evidence before dispatch

Before any call to `agents/` or a provider, a durable `AiOperation` and a durable `AgentAttempt` (status `DISPATCHED`) already exist and are committed. Dispatch happens only after that commit.

### AI output is a proposal

`agents/` never publishes, approves, or assigns academic authority. `api/` validates a structured proposal and persists it as a revision through the same creation path a human edit also uses — there is no separate "auto-approve" path.

## Explicit note on naming discipline

The concept names above (`AiOperation`, `AgentAttempt`, `RevisionOrigin`, etc.) and their field names (`resolvedProvider`, `structuredResult`, `expectedRevisionId`, ...) are the actual names decided in the ADRs and plan — see [02 — Domain and Data Changes](../../implementation-plans/assessment-authoring-operation-foundation/02-domain-and-data-changes.md) for the exact Java/SQL identifiers. No local packet may substitute a different name for the same concept. Status values and failure codes have their own canonical taxonomy, defined in [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md), including an explicit correction against a generic taxonomy sketch that does not match the ADRs — see that document's alignment note before assuming any status/code name not found there.

---

← [01 — Workspace Responsibility Matrix](01-workspace-responsibility-matrix.md) | [↑ inicio](#top) | [Siguiente: Cross-Workspace API Contracts →](03-cross-workspace-api-contracts.md)

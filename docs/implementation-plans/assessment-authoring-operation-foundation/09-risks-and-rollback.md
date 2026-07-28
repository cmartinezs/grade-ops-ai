<a id="top"></a>

# 09 — Risks and Rollback

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [08 — Implementation Sequence](08-implementation-sequence.md) · **Next:** [10 — Acceptance Criteria →](10-acceptance-criteria.md)

## Rollout strategy

- **Additive migrations first, cutover second, backfill last** — tasks 1–6 ([08](08-implementation-sequence.md)) add schema and code nothing depends on yet; task 7 is the single pivot where the write path changes; tasks 8–11 extend the pivot; task 12 (legacy backfill) runs only after the new path is proven correct.
- **No feature flag.** The task brief permits one "si son realmente necesarias" — it is not necessary here: this is a single-team, low-traffic, pilot-stage authoring slice with one internal Web consumer being updated in the same cut. A flag would add a second code path to maintain for a cutover that can be validated by tests and a green baseline before merge, per [Verification](../../../design-system/research/research-03-baseline-technical-verification.md).
- **Compatibility period:** none for the API contract (breaking changes are documented and absorbed in one cut, per [Authoring Operation Contract § Compatibility Impact](../../99-decisions/2026-07-28-authoring-operation-contract.md#compatibility-impact)). One release of read-only legacy-table retention *is* the compatibility period for data, not for contract.
- **Contract switch:** happens at task 10/11 (API + Web deployed together, since Web is the only consumer and there is no reason to stagger a two-sided internal contract change).
- **Legacy retirement:** `assessment_drafts`/`agent_execution_logs` tables and `AssessmentDraft`/`AgentExecutionLog` domain classes — code is removed in task 13; tables are removed in a follow-up task explicitly not scheduled in this plan (see [06 — Database Migration § Legacy compatibility window](06-database-migration.md#legacy-compatibility-window)).

## Per-task risk and rollback

| Task | Risk level | Primary risk | Rollback |
|---|---|---|---|
| 01–03 (schema) | Low | None — additive, unreferenced by app code | `DROP TABLE`/`DROP COLUMN` in reverse order; safe at any time before task 07 deploys |
| 04–06 (new domain code) | Low | Dead code until wired | Delete the new files; nothing else references them |
| 07 (coordinator pivot) | **High** | Regressing the 289-test API baseline; mishandling pre-existing `Assessment` rows with `currentRevisionId = null` | Revert the single commit (per commit-boundary discipline, this is one atomic commit); schema from 01–03 stays harmless if unused |
| 08 (human edit → revision) | Medium | Deleting the old handler's test without confirming the new one supersedes it correctly | Revert the commit; old `UpdateAssessmentDraftHandler` behavior returns exactly as it was |
| 09 (regenerate CAS) | Medium | Pre-dispatch check not actually preventing the LLM call (silent regression to today's behavior) | Revert the commit |
| 10 (API surface) | Medium | Breaking a Web call before Web is updated (deploy ordering) | Deploy API (task 10) and Web (task 11) together, or keep `PATCH .../draft` reachable behind the scenes for the gap between the two deploys if a true simultaneous deploy isn't operationally possible — this is the one place a short-lived, explicitly time-boxed compatibility shim is defensible, and should be removed the moment task 11 ships, not left indefinitely |
| 11 (Web) | Low-medium | Scope creep into visual redesign | Out of bounds per [05](05-web-migration.md#out-of-scope-for-this-plan); revert if it happens |
| 12 (legacy backfill) | Medium | Scale/performance on a large legacy table; mislabeling provenance | Migration is additive (new rows only, old tables untouched) — rollback is `DELETE FROM assessment_revisions WHERE origin = 'LEGACY_UNKNOWN'` equivalent cleanup, or simply re-running after a fix since V17 is idempotent-guarded per [08](08-implementation-sequence.md#task-12--database-legacy-data-backfill) |
| 13 (cleanup) | Low | Missing a reference, breaking compile | Compiler catches it immediately; not mergeable if broken |
| 14 (docs/regression) | Low | None functional | N/A |

## Cross-cutting risks

- **Residual race windows** are explicitly accepted, not hidden, in two places: the narrow pre-check-to-commit gap for wasted LLM calls, and the `INDETERMINATE` orphaned-attempt classification (both documented in [Idempotency and Concurrency Strategy](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md) and [Durable AI Operation Model](../../99-decisions/2026-07-28-durable-ai-operation-model.md)). No task in this plan claims to close these completely; closing them further (e.g., a reconciliation job) is explicitly deferred, not silently dropped.
- **No CI workflow currently runs `web/`** (Research 03 §4 finding, pre-existing). Task 11's Web changes are therefore verified locally per [08](08-implementation-sequence.md#task-11--web-migrate-to-the-new-contract) but do not get an automatic CI gate on this branch's PR the way `api/`/`agents/` changes do. This is a pre-existing gap this plan does not fix (out of scope) but the executing agent should run the Web verification commands manually before any commit touching `web/`, since CI will not catch a regression there.

---

← [08 — Implementation Sequence](08-implementation-sequence.md) | [↑ README](README.md) | [Siguiente: Acceptance Criteria →](10-acceptance-criteria.md)

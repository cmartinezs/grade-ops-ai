<a id="top"></a>

# FINAL-INTEGRATION-PROMPT — Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md)

Give this file, verbatim, as the task prompt to a fresh session for **Session D (integration)**. This session opens `api/`, `agents/`, and `web/` — it is the one session in this cut authorized to touch all three.

## Who you are and what you are doing

Sessions A (API), B (Agents), and C (Web) have each independently executed their workspace-scoped packet from `docs/implementation-packets/assessment-authoring-operation-foundation/` and pushed a branch with a filled `*-HANDOFF.md`. Your job is to merge their work into one integration branch, verify the contracts actually line up (not just that each workspace's own tests pass in isolation), run the full cross-workspace regression, confirm all 25 acceptance criteria, and open the one functional PR to `develop`.

**You do not re-implement any of A/B/C's work.** If something is wrong, you fix the specific defect (a field name mismatch, a missed test) — you do not redesign their approach. If a defect is large enough that fixing it here would mean substantially reimplementing a workspace's task, stop and report it as a blocker rather than absorbing that work into this session.

## Authority — read in this order

1. The four [2026-07-28 ADRs](../../99-decisions/README.md#active-decision-records)
2. [Assessment Authoring Operation Foundation plan](../../implementation-plans/assessment-authoring-operation-foundation/README.md) — especially [10 — Acceptance Criteria](../../implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md)
3. This root packet: [01](01-workspace-responsibility-matrix.md), [06](06-acceptance-criteria-ownership.md), [07](07-integration-and-final-verification.md)
4. `API-HANDOFF.md`, `AGENTS-HANDOFF.md`, `WEB-HANDOFF.md` (see below)

## Preflight

```bash
git remote -v
git fetch origin
git ls-remote origin feat/assessment-authoring-operation-foundation-api
git ls-remote origin feat/assessment-authoring-operation-foundation-agents
git ls-remote origin feat/assessment-authoring-operation-foundation-web
```

Confirm all three subrepo branches exist on `origin` and each has a `*-HANDOFF.md` as its last commit. If any is missing or its handoff is incomplete (a required section absent, not merely "None."), stop — you cannot integrate a session that hasn't reported its own completion.

```bash
git switch feat/assessment-authoring-operation-foundation   # the integration branch, already exists per Sessions A/B/C's preflight
git pull --ff-only
```

## Merge sequence

```bash
git merge --no-ff origin/feat/assessment-authoring-operation-foundation-api
git merge --no-ff origin/feat/assessment-authoring-operation-foundation-agents
git merge --no-ff origin/feat/assessment-authoring-operation-foundation-web
```

Order matters only in that Agents' Task 07A output (the `provider` field name/shape) is what API's Task 07B consumed — if API merged cleanly, that dependency was already resolved during Session A's own work (either via a completed Agents handoff it read, or via its own read-only verification, per its packet's "Handling discoveries" fallback). Conflicts between these three branches are not expected — `api/`, `agents/`, and `web/` are disjoint directory trees — but if one occurs, resolve it by preferring whichever branch's version matches the frozen contract in [02](02-shared-domain-contract.md)/[03](03-cross-workspace-api-contracts.md), not by guessing.

## Verify each handoff's claims, don't just trust them

For each of `API-HANDOFF.md`, `AGENTS-HANDOFF.md`, `WEB-HANDOFF.md`: re-run the test command it claims passed, on the merged integration branch, yourself. A handoff that says "289/300 tests pass" is a claim to verify, not a fact to record.

## Cross-workspace verification

Run everything in [07 — Integration and Final Verification](07-integration-and-final-verification.md):

```bash
./mvnw -f api/pom.xml clean test
./mvnw -f agents/pom.xml -Pdemo clean test
cd web && npm ci && npm run lint && npm run test && npm run build
```

Then the contract checklist, database checks, and the full [alignment check table](07-integration-and-final-verification.md#alignment-check-table) — re-run it against what was *actually implemented*, not the drafted plan; implementation frequently drifts from a plan in small ways (a real field name chosen during coding, a status value spelled slightly differently) that a documentation-only pass cannot catch.

Specifically confirm, by reading the actual merged code (not the handoffs' prose):

- The `provider` field name Agents implemented in `AgentExecutionLogPayload` matches exactly what API's coordinator deserializes.
- No workspace introduced a status or failure-code spelling outside [03 — Cross-Workspace API Contracts](03-cross-workspace-api-contracts.md).
- Web's idempotency-key and `expectedRevisionId` handling matches [03](03-cross-workspace-api-contracts.md#api--web-public-contract) exactly.
- `PATCH /api/v1/assessments/{id}/draft` returns `404`/`405` and no code path in `web/src` calls it anymore.

## Task 14 — full regression and documentation sync

Per [08 — Implementation Sequence § Task 14](../../implementation-plans/assessment-authoring-operation-foundation/08-implementation-sequence.md#task-14--full-regression-and-documentation-sync): update `docs/09-developer-guide/03-api-reference.md` and `docs/09-developer-guide/05-database-guide.md` to describe the new endpoints/tables as current, and note the retired `PATCH .../draft` as removed (one line, linking to the [Authoring Operation Contract ADR](../../99-decisions/2026-07-28-authoring-operation-contract.md)) — not silently deleted from the docs with no trace. Commit: `docs(developer-guide): sync API reference and database guide with authoring operation foundation`.

## Acceptance criteria sign-off

Walk every row of [06 — Acceptance Criteria Ownership](06-acceptance-criteria-ownership.md). Mark each `PASS` only after personally confirming its named test exists and passes on the merged branch. `NOT_IMPLEMENTED` on any row blocks the PR — go back to the owning workspace's branch/handoff (do not implement the fix yourself unless it is trivially small and clearly within what that packet already specified) and report the gap.

## Deploy-ordering decision you own

Per [05 — Execution Order § Rules during execution](05-execution-order.md#rules-during-execution) and the plan's [09 — Risks and Rollback](../../implementation-plans/assessment-authoring-operation-foundation/09-risks-and-rollback.md): API's `PATCH .../draft` removal and Web's replacement call ship together in this one PR — there is no staggered deploy in this repository's actual deployment model (one PR, one merge, one deploy), so the "short-lived compatibility shim" contingency in the plan does not apply here. Confirm this in your PR body rather than silently assuming it.

## Security check before opening the PR

```bash
git diff origin/develop...HEAD --stat
```

Scan the diff for anything resembling `GRADEOPS_GROQ_API_KEY`, `INTERNAL_API_SECRET`, Firebase credentials, or other secret values — none should appear anywhere in this diff (it is exclusively schema, domain code, application code, controllers, Web components, and documentation). If `docker compose config` needs to be run for any reason during verification, use `docker compose config --no-interpolate` only.

## Push and open the PR

```bash
git push origin feat/assessment-authoring-operation-foundation
```

Open one PR, `feat/assessment-authoring-operation-foundation` → `develop`. Title: a concise summary of the cut (e.g., "feat: assessment authoring operation foundation — immutable revisions, durable AI operations, idempotent contract"). Body must cover: summary; what changed per workspace; the 25 acceptance criteria (link to [06](06-acceptance-criteria-ownership.md), state all `PASS`); breaking API changes (link to the [Authoring Operation Contract ADR § Compatibility Impact](../../99-decisions/2026-07-28-authoring-operation-contract.md#compatibility-impact)); residual risks carried forward (link to [08 — Risk and Decision Ledger](08-risk-and-decision-ledger.md)); verification performed (the exact commands and results above); explicitly-not-implemented items (legacy table drop, feature flag, reconciliation job — all deliberately deferred, not forgotten).

## Final report to whoever invoked this session

Branch, HEAD, PR URL; per-workspace verification result (test counts before/after); the acceptance-criteria table with every row's status; any blocker found and how it was resolved (or why it wasn't); confirmation the security check found nothing.

---

← [README](README.md) | [↑ Volver al inicio](#top)

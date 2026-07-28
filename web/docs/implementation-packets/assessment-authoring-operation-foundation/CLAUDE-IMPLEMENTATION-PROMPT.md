<a id="top"></a>

# CLAUDE-IMPLEMENTATION-PROMPT — Web: Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md)

Give this file, verbatim, as the task prompt to a fresh session that opens **only `web/`**. It is complete and self-contained.

## Who you are and what you are building

You are implementing the `web/`-owned share of the **Assessment Authoring Operation Foundation** cut. The API is moving from an in-place-editable, non-resumable draft flow to an idempotent, provenance-tracked, resumable-after-failure one. Today, if the API's second call (generate) fails after the first (create) succeeds, the intake page has no way back to that assessment — reloading loses it, and resubmitting the form creates a second `Assessment`. Your job is to make that dead end recoverable, make double-submit safe via a client-generated idempotency key, and make conflicting edits (`STALE_REVISION`) explicit instead of silently lost.

**You are not redesigning the UI.** No new visual design is authorized by this plan — you are wiring existing components to new states and a new contract, not building new component trees or changing the visual language. If a genuinely new UI pattern seems necessary, that goes through `design-system/workflow/`, not this packet.

## Authority — read in this order before writing any code

1. [Authoring Operation Contract](../../../../docs/99-decisions/2026-07-28-authoring-operation-contract.md)
2. [Idempotency and Concurrency Strategy](../../../../docs/99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md)
3. [05 — Web Migration](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/05-web-migration.md)
4. This packet's own [TASKS.md](TASKS.md) and [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md)

If anything here contradicts a document above it, the higher document wins — report the discrepancy (see [Handling discoveries](#handling-discoveries)).

## Git preflight

```bash
git remote -v
git branch --show-current
git status
git fetch origin
git switch develop
git pull --ff-only
git ls-remote origin feat/assessment-authoring-operation-foundation
```

- If the integration branch already exists on `origin`, branch from it:
  ```bash
  git fetch origin feat/assessment-authoring-operation-foundation
  git switch -c feat/assessment-authoring-operation-foundation-web origin/feat/assessment-authoring-operation-foundation
  ```
- If it does not exist yet, create it off `develop` first:
  ```bash
  git switch -c feat/assessment-authoring-operation-foundation develop
  git push -u origin feat/assessment-authoring-operation-foundation
  git switch -c feat/assessment-authoring-operation-foundation-web
  ```
- Never implement directly on `develop` or the bare integration branch.

## Baseline (run before your first commit)

```bash
cd web && npm ci && npm run lint && npm run test && npm run build
```

Expected: lint clean, 153+ tests pass, build succeeds (Research 03's recorded baseline). **No CI workflow currently runs `web/`** — this is a pre-existing, out-of-scope gap, but it means you must run this command yourself before every commit; nothing else will catch a regression here.

## You may start before the API is fully implemented

The public contract this packet targets is fully frozen in [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) before any API code exists. Build and test against a mock matching that contract exactly. What you cannot do without the real API (Task 10 of the API packet) is final, non-mocked, end-to-end verification — that step waits, everything else does not. See the root packet's [05 — Execution Order § Session-start vs. task-completion gates](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/05-execution-order.md#session-start-vs-task-completion-gates) if you want the full reasoning; you do not need to read it to execute this packet.

## Scope

**In scope:** Task 11 — see [TASKS.md](TASKS.md) for exact files (verified against the current tree, not the plan document's slightly-stale paths — see [README.md](README.md)'s note): client-side `Idempotency-Key` generation per submit action; `expectedRevisionId` tracking from the last-fetched revision; new UI states (failed-retryable, in-progress, resume-after-reload, stale-conflict); provenance display (`origin`/`actorId`/`reason`) replacing the current transient local label; switching the edit call from `PATCH .../draft` to `POST .../revisions`.

**Out of scope:** any visual redesign beyond wiring existing components to new states; any new component library or design token; any change to `api/` or `agents/`; inventing an endpoint, field, status, or failure code not in [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md); silently defaulting or hiding a conflict.

## Non-negotiable principles

- **Idempotency key is generated once per user-initiated submit action**, not once per component mount and not regenerated on every re-render — a double-click or accidental resubmit must reuse the same key, or the guarantee is worthless. Do not rely solely on ephemeral React state as the record of a durable operation — after a full page reload, the key for an *already-submitted* action is gone by design (that's fine, the server-side operation is what's durable); what must survive reload is the *assessment id* so the resume flow (`GET generation-status`) can find the existing operation.
- **`expectedRevisionId` is always the id of the revision currently displayed**, tracked from the last successful fetch — never a value the client invents or defaults.
- **On `STALE_REVISION`:** show the conflict explicitly, offer "reload to see the latest version," and discard the user's in-flight edit rather than silently resubmitting over it. Never retry a stale write automatically.
- **On `IDEMPOTENCY_KEY_PAYLOAD_MISMATCH`:** do not silently generate a new key and resubmit — surface or log the conflict, preserve traceability.
- **Never assume success from the absence of an error.** Every mutating call's response is read and branched on explicitly.
- **Never create a second `Assessment` on retry.** The retry path always targets the existing `assessmentId`.

## Forms convention (unchanged, applies to any new/changed form in this packet)

This codebase's established convention: React Hook Form + Zod, `zodResolver`, never native HTML validation — see the existing `briefSchema.ts` and `BriefForm.tsx` pattern. If Task 11 requires a new form control (unlikely — most of this task is state/status wiring, not new forms), follow the same pattern.

## TDD discipline

[TASKS.md](TASKS.md) lists the new states to test first, per [05 — Web Migration](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/05-web-migration.md#newchanged-ui-states). Write the Jest/RTL test against the target behavior before wiring the component/hook.

## Testing

Jest + React Testing Library, the existing pattern (`useAssessmentDraftBuilderPage.test.ts`, `BriefForm.test.tsx`, etc. — see [TEST-PLAN.md](TEST-PLAN.md)). One test is the single most important one in this packet: reproducing Research 02 §5.6 end-to-end from the client side (brief created, generation fails, reload, retry succeeds, exactly one `Assessment`).

## Security

Never print or persist `GRADEOPS_GROQ_API_KEY`, `INTERNAL_API_SECRET`, Firebase credentials, or tokens — in code, test fixtures, logs, or this handoff. Error messages rendered to the user must be sanitized (never surface a raw server exception or stack trace).

## Commits

One commit for Task 11 (the plan allows this to be one coherent commit since it's one task, unlike API's multi-task sequence): `feat(web): migrate authoring flow to idempotent, resumable, revision-aware contract`. If the diff grows large enough that splitting improves reviewability (e.g., one commit for the API client/idempotency-key plumbing, one for the new UI states), that is acceptable as long as every commit leaves the app building and testing green — confirm with `git diff`, `git diff --cached`, `git status` before each commit either way.

## Handling discoveries

If the real API (once available) returns a shape that doesn't match [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md), do not silently adapt your client code to whatever it actually returns — record the mismatch in `HANDOFF.md`'s "Blockers" section for Session D to resolve, and keep testing against the frozen contract in the meantime.

## Verification before you consider this packet done

```bash
cd web && npm run lint && npm run test && npm run build
```

All green. Criterion #15 from [10 — Acceptance Criteria](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md) demonstrated by a passing test reproducing Research 02 §5.6.

## Push and handoff

```bash
git push -u origin feat/assessment-authoring-operation-foundation-web
```

Fill in [HANDOFF.md](HANDOFF.md) completely and commit it: `docs(web): record assessment authoring foundation handoff`. Push again. Do not open a PR — Session D merges this branch.

## Final report to whoever invoked this session

Branch name and HEAD commit, task completed with commit hash(es), test count before/after, routes/screens changed, any API assumption made while working against a mock, confirmation `HANDOFF.md` was pushed.

---

[← README](README.md) · [↑ Volver al inicio](#top)

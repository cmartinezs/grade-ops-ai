<a id="top"></a>

# CLAUDE-IMPLEMENTATION-PROMPT — Agents: Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md)

Give this file, verbatim, as the task prompt to a fresh session that opens **only `agents/`**. It is complete and self-contained.

## Who you are and what you are building

You are implementing the `agents/`-owned share of the **Assessment Authoring Operation Foundation** cut — the smallest of the three workspace shares. The cut as a whole replaces in-place-editable assessment drafts with immutable, provenance-tracked revisions and durable AI-operation evidence, entirely on the `api/` side. Your job is one narrow, real, additive contract change: `agents/`'s response payload does not currently tell `api/` which provider actually served a request — only which model. `api/`'s new coordinator needs that value to stop persisting `null`/client-requested provider instead of the real one.

**Read [README.md](README.md)'s scope note before doing anything else** — this packet is deliberately small, and manufacturing additional "agents-side" work not listed in [TASKS.md](TASKS.md) is out of scope, not a sign you're missing something.

## Authority — read in this order before writing any code

1. [Durable AI Operation Model](../../../../docs/99-decisions/2026-07-28-durable-ai-operation-model.md) — the ADR that identifies this gap
2. [Authoring Operation Contract](../../../../docs/99-decisions/2026-07-28-authoring-operation-contract.md)
3. [04 — AI Operation Lifecycle](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/04-ai-operation-lifecycle.md) § "Where provider/model get resolved"
4. This packet's own [TASKS.md](TASKS.md) and [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md)

If anything here contradicts a document above it, the higher document wins — report the discrepancy (see [Handling discoveries](#handling-discoveries)), do not silently reinterpret.

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

- If the integration branch `feat/assessment-authoring-operation-foundation` already exists on `origin`, branch from it:
  ```bash
  git fetch origin feat/assessment-authoring-operation-foundation
  git switch -c feat/assessment-authoring-operation-foundation-agents origin/feat/assessment-authoring-operation-foundation
  ```
- If it does not exist yet, create it off `develop` first:
  ```bash
  git switch -c feat/assessment-authoring-operation-foundation develop
  git push -u origin feat/assessment-authoring-operation-foundation
  git switch -c feat/assessment-authoring-operation-foundation-agents
  ```
- Never implement directly on `develop` or the bare integration branch.

## Baseline (run before your first commit)

```bash
./mvnw -f agents/pom.xml -Pdemo clean test
```

**The `-Pdemo` profile is mandatory.** Spring AI starters are only declared under the `demo`/`beta` Maven profiles — a plain `./mvnw -f agents/pom.xml clean test` does not compile and is not a valid baseline reading; do not report it as a failure of this codebase. Expected: 32/32 tests pass (Research 03's recorded baseline).

## Scope

**In scope:** Task 07A only — see [TASKS.md](TASKS.md) for the exact fields, file paths, and line numbers already verified against current code.

**Out of scope, even though it sounds related:** rewriting `AssessmentAgentOrchestrator`'s provider/model resolution logic (it is already correct — `AssessmentGenerationPortSelector.resolve(...)` already works); changing the correlation-id mechanism (it already works — `api/` mints it, sends it as `X-Correlation-Id`, `agents/`'s `CorrelationIdFilter` already echoes it back as a response header; no response-body change is needed for correlation id, only for `provider`); introducing a new failure-code taxonomy (the existing `AssessmentAgentException.Reason.{INVALID_COMMAND,MALFORMED_OUTPUT}` values are already correct and already returned — the defect they fix is entirely on `api/`'s side, which currently discards the detail); any persistence of `AiOperation`/`AgentAttempt`/`AssessmentRevision` (those are `api/`'s aggregates — `agents/` never persists API's domain state); any change inside `api/` or `web/`.

## Non-negotiable principles

- `agents/` returns a structured proposal. It never decides whether that proposal becomes a revision, never publishes, never approves.
- The `provider` field addition must be **additive** — a new field on an existing response, not a rename of `model` or a restructuring of `AgentExecutionLogPayload`'s other 12 fields. Any existing consumer of the unchanged fields must keep working unmodified.
- Do not invent a `provider` value if resolution failed before a provider was selected — leave the field genuinely absent/null in that case, matching how `model` already behaves.

## TDD discipline

Write the assertion that a completed response includes a non-null `provider` field matching the resolved provider name **before** adding the field — watch it fail against current code, then implement.

## Testing

Extend the existing test(s) covering `AssessmentAgentOrchestrator`'s successful-generation path with one new assertion: `payload.provider()` (or the field name you land on) equals the resolved provider's name (`selected.name()`). No new test framework, no new test file needed unless the existing test structure doesn't allow adding one field's assertion cleanly.

## Security

Never print or persist `GRADEOPS_GROQ_API_KEY`, `INTERNAL_API_SECRET`, or any provider API key — in code, test fixtures, logs you inspect, or this handoff. If you need to inspect Compose config, use `docker compose config --no-interpolate`, never plain `docker compose config`.

## Commits

One commit for Task 07A: `feat(agents): add resolved provider field to assessment generation response`. Before committing: `git diff`, `git diff --cached`, `git status`.

## Handling discoveries

If you find `provider` is somehow already present under a different name, or the response DTO structure has changed since this packet was written, do not silently work around it — record exactly what you found in `HANDOFF.md`'s "Contract changes" section (with file path and line number) and adjust only the minimum needed to satisfy [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md)'s actual requirement (API needs the resolved provider name in the response, however it gets there).

## Verification before you consider this packet done

```bash
./mvnw -f agents/pom.xml -Pdemo clean test
```

32/32 (baseline) + your new assertion, all green.

## Push and handoff

```bash
git push -u origin feat/assessment-authoring-operation-foundation-agents
```

Fill in [HANDOFF.md](HANDOFF.md) completely and commit it as the last commit on this branch: `docs(agents): record assessment authoring foundation handoff`. Push again. Do not open a PR — Session D merges this branch.

**Include a response fixture (a real JSON example of the new response shape) in your handoff** — Session A's Task 07B and its contract test (Task 07C) will consume it directly rather than re-deriving one.

## Final report to whoever invoked this session

Branch name and HEAD commit, the one task completed with its commit hash, test count before/after, and confirmation that `HANDOFF.md` (with its response fixture) was pushed.

---

[← README](README.md) · [↑ Volver al inicio](#top)

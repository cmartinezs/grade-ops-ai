<a id="top"></a>

# Implementation Plan — Assessment Authoring Operation Foundation

**Status:** Planned — not yet implemented. **Parent:** [docs/](../../README.md) → [Implementation Plans](../README.md)

## What this is

This is the detailed, task-by-task implementation plan for the first technical cut identified by [Research 02 §26](../../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md#26-first-implementation-boundary-recommendation) and closed by four ADRs in `docs/99-decisions/`. It turns those decisions into an executable sequence. **No functional code changes are included in this plan itself** — implementing it is the next agent's job.

## Scope

**In scope** (per the four ADRs):

- Stable `Assessment` identity with an explicit `currentRevisionId` pointer and optimistic lock.
- Immutable `AssessmentRevision` replacing in-place `AssessmentDraft` edits — every content change (AI or human) is a new row with origin, actor, timestamp, and predecessor.
- Idempotent creation and CAS-protected mutation (`expectedRevisionId`).
- Durable `AiOperation`/`AgentAttempt` evidence, written before external dispatch, with typed failure codes, resolved provider/model, and correlation id.
- Resumable generation: retry and resume without creating a duplicate `Assessment`.
- Honest migration of legacy `assessment_drafts`/`agent_execution_logs` data.

**Out of scope** (unchanged from the task brief and the ADRs — do not add during implementation):

- Organizations/membership, academic periods, sections, students, participation, submissions, grading, academic results, publication, correction, appeals, batch processing, pedagogical analytics.
- A full credits/billing ledger — only usage/cost *evidence* (`AgentAttempt` fields) is in scope.
- Cloud/infra redesign, microservices, message brokers.
- `AssessmentTemplate`/`AssessmentTemplateVersion`/`AppliedAssessmentProfile` (the UI/UX discovery's broader template model) — this plan's `AssessmentRevision` is a precursor, not that model.

## Read this in order

| # | Document | Purpose |
|---|---|---|
| 00 | [Executive Summary](00-executive-summary.md) | One-page why/what/how |
| 01 | [Current State and Target](01-current-state-and-target.md) | What exists today vs. what this cut builds |
| 02 | [Domain and Data Changes](02-domain-and-data-changes.md) | Aggregates, entities, value objects, invariants |
| 03 | [Operation and API Contracts](03-operation-and-api-contracts.md) | Endpoint-by-endpoint current/target contract |
| 04 | [AI Operation Lifecycle](04-ai-operation-lifecycle.md) | `AiOperation`/`AgentAttempt` sequencing in implementation terms |
| 05 | [Web Migration](05-web-migration.md) | What Web stops orchestrating, new states, recovery UX |
| 06 | [Database Migration](06-database-migration.md) | Schema changes, legacy backfill, honest provenance labeling |
| 07 | [Testing Strategy](07-testing-strategy.md) | Test matrix per task/layer |
| 08 | [Implementation Sequence](08-implementation-sequence.md) | The actual task list — Task ID, steps, tests-first, verification, commit boundary |
| 09 | [Risks and Rollback](09-risks-and-rollback.md) | What can go wrong, and how each step is reversible |
| 10 | [Acceptance Criteria](10-acceptance-criteria.md) | The 25 minimum criteria from the task brief, mapped to tests |

## Governing decisions

- [Assessment Authoring Model](../../99-decisions/2026-07-28-assessment-authoring-model.md)
- [Authoring Operation Contract](../../99-decisions/2026-07-28-authoring-operation-contract.md)
- [Idempotency and Concurrency Strategy](../../99-decisions/2026-07-28-idempotency-and-concurrency-strategy.md)
- [Durable AI Operation Model](../../99-decisions/2026-07-28-durable-ai-operation-model.md)

This plan does not repeat the *why* already argued in those ADRs — it links to them. If a task here seems to contradict an ADR, the ADR wins; fix the task, do not reinterpret the ADR.

## Evidence base

- [Research 01 — Current Domain & Data Model Alignment Audit](../../../design-system/research/research-01-current-domain-data-model-alignment-audit.md)
- [Research 02 — Workflow & Lifecycle Alignment Audit](../../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md)
- [Research 03 — Baseline Technical Verification](../../../design-system/research/research-03-baseline-technical-verification.md)

## Ground rules for whoever executes this plan

- Every task must leave the repository compilable. Most tasks must leave it testable (a small number of purely additive schema/domain tasks are compile-only checkpoints; this is called out per task).
- Follow [superpowers:test-driven-development] discipline where the task lists "Tests to write first" — write those tests before the implementation they verify.
- One commit per task, at the boundary stated in that task. Do not batch multiple tasks into one commit.
- Do not invent scope. If a task reveals a gap this plan didn't anticipate, stop and raise it rather than silently expanding scope.

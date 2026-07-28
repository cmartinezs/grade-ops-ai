<a id="top"></a>

# 01 — Current State and Target

**Parent:** [README](README.md) · **Status:** Planned · **Prev:** [00 — Executive Summary](00-executive-summary.md) · **Next:** [02 — Domain and Data Changes →](02-domain-and-data-changes.md)

Full evidence lives in [Research 01](../../../design-system/research/research-01-current-domain-data-model-alignment-audit.md) and [Research 02](../../../design-system/research/research-02-workflow-lifecycle-alignment-audit.md), re-verified against current `HEAD` (`ce3d31b`) in [Research 03](../../../design-system/research/research-03-baseline-technical-verification.md). This is a condensed pointer, not a restatement.

## Current state (verified against code, not documentation)

```text
Teacher
  └─ Assessment (id, teacherUid, status=DRAFT always, createdAt)
       ├─ AssessmentBrief (learningGoal, topic, level, duration, language) — 1:1
       ├─ AssessmentDraft 1..N (version_number, previous_version_id, content, agent_execution_log_id)
       │     "current" = MAX(version_number) — not a stored pointer
       │     human edit = UPDATE same row (applyEdit) — CONFLICTING with target
       └─ AgentExecutionLog 1..N (provider, model, prompt_version, hashes, tokens, cost, status, error_code)
             written AFTER the external call returns — CRITICAL-03
```

Flyway schema: V1–V12. `assessments`/`assessment_briefs`/`assessment_drafts`/`agent_execution_logs` introduced in V9–V12. No `@Version` anywhere in this slice. No idempotency table. No optimistic locking. `web/` orchestrates create-then-generate as two unguarded HTTP calls.

## Target for this cut

```text
Assessment (id, teacherUid, status — frozen, unread by authoring, currentRevisionId, lockVersion)
  ├─ AssessmentBrief — unchanged, immutable
  ├─ AssessmentRevision 1..N — immutable, origin/actor/reason/previousRevisionId/sourceAgentAttemptId
  └─ AiOperation 1..N (operationType, idempotencyKey, expectedRevisionId, status)
        └─ AgentAttempt 1..N — written before dispatch; resolved provider/model/correlationId/cost/failureCode
```

Full field-level detail: [02 — Domain and Data Changes](02-domain-and-data-changes.md). Full endpoint-level detail: [03 — Operation and API Contracts](03-operation-and-api-contracts.md).

## What this cut deliberately does not touch

Everything Research 01/02 classify `MISSING` beyond authoring — organizations, academic structure, curriculum P1, assessment components/rubrics, participation, submission, evaluation, results, approval, publication, correction, appeals, pedagogical analysis, batch processing, credits ledger — stays exactly as absent as it is today. This plan does not lay speculative groundwork for any of it beyond what the four ADRs already require (e.g., `AssessmentRevision.origin` is an enum precisely large enough for this cut's two real values plus one migration-only value — it is not pre-stocked with hypothetical future origins).

---

← [00 — Executive Summary](00-executive-summary.md) | [↑ README](README.md) | [Siguiente: Domain and Data Changes →](02-domain-and-data-changes.md)

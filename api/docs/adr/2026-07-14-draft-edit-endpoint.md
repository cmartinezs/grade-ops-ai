# ADR: Draft edit endpoint — in-place update mechanism

**Date:** 2026-07-14
**Status:** Accepted
**Planning:** 003-assessment-creation / story-01-assessment-creation-persistence / task-09-draft-edit-endpoint

## Context

Unlike task-08's regeneration (always appends a new, non-destructive version), a manual teacher edit is not an AI execution and should update the current version's row in place — no new version, no new `AgentExecutionLog`. `AssessmentDraft` has no setters: every field is set only via the private constructor plus the static factories (`generate`/`regenerate`/`restore`), matching every other aggregate in this story. Task-03's own inline doc additionally describes `AssessmentDraftRepositoryPort.save` as "insert-only."

## Decision

Added `AssessmentDraft.applyEdit(title, context, instructions, objectives, deliverables, constraints)`, which returns a **new** domain object reusing the current draft's `id`/`versionNumber`/`previousVersionId`/`agentExecutionLogId`/`createdAt`; any non-null parameter overrides the corresponding field, `null` keeps the current value (partial-update semantics). `UpdateAssessmentDraftHandler` calls `assessmentDraftRepository.save(...)` with this object. Because the object's `id` already exists in the database, Spring Data JPA's `save()` — which always upserts by id — issues a genuine SQL `UPDATE`, not an `INSERT`. This is the same mechanism task-07 already relies on to back-fill `AgentExecutionLog.draftId` after the draft row is created.

This reconciles with task-03's "insert-only" framing: that statement described this story's design *intent* up to task-08 (every call site always passed a brand-new id), not a technical limitation of `save()` itself. Task-09 is the first call site that intentionally reuses an existing id to obtain an update, and no new port method or entity-level mutation was required.

`UpdateAssessmentDraftRequest` uses `@Size(min = 1)` on the three string fields (Bean Validation's own convention: `null` always passes regardless of the constraint, so an absent field still means "don't change it"; only an explicitly-sent `""` is rejected) and `List<@NotBlank String>` on the three list fields (rejects a blank element in a provided list, while `null` stays valid). `@Valid` is on the controller parameter, matching every other endpoint in this controller.

## Consequences

No new persistence port method, no domain mutation, no schema change. `UpdateAssessmentDraftHandlerIntegrationTest` asserts the observable effect of an in-place update — same `draftId`, same `versionNumber`, a single row in `assessment_drafts`, and an unchanged `AgentExecutionLog` count — locking in the "in-place, no side effects" guarantee this task exists to provide. (The test doesn't assert SQL text directly; a local run additionally showed the expected `update assessment_drafts ... where id=?` in the Hibernate log, consistent with this assertion.) `NoPriorDraftException` (task-08) and `GenerateAssessmentDraftResponse`/`GenerateAssessmentDraftResult` (task-07) were reused rather than duplicated, since their shapes already matched exactly.

## Alternatives Considered

Adding setters (or a general-purpose mutation method) directly to `AssessmentDraft` was considered, but rejected — it would break the immutable-after-construction pattern every other aggregate in this story follows, for a benefit (avoiding one `restore(...)` call inside `applyEdit`) that doesn't outweigh the consistency cost.

A new `AssessmentDraftRepositoryPort.update(...)` method, distinct from `save`, was considered to make the "this is an update, not an insert" intent more explicit at the port level. Rejected as unnecessary — `save()` already upserts correctly by id, and adding a second method with overlapping behavior would be duplication without a corresponding behavioral difference to justify it.

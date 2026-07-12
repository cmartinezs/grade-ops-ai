# ⚛️ TASK 08 — Draft regeneration endpoint

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-07
> [← story file](../story-01-assessment-creation-persistence.md)

---

## Objective

`POST` endpoint that regenerates the draft with teacher-provided adjustment notes, creating a new non-destructive version — US-012.

---

## Technical Design

- **Approach:** near-identical flow to task-07's `GenerateAssessmentDraftHandler`, reused via a shared internal helper rather than duplicated — both call `agentclient` outside a transaction, then persist a new `AgentExecutionLog` + new `AssessmentDraft` version inside one. Because the `AgentExecutionLog` persistence is the shared helper (not duplicated per handler), this task automatically inherits task-07's full 2026-07-12 field set (`agentExecutionId`, `agentName`, `provider`, `promptVersion`, `inputHash`, `outputHash`, `estimatedInputTokens`, `estimatedOutputTokens`, `errorCode` as its own column, etc. — see this story's Inconsistency #3) with no separate schema decision needed here. The differences: the command includes `adjustmentNotes`, `previousDraftId` (the current draft's ID, for correlation only), and **`previousDraft`** (the current draft's content, rendered to text — see below); the new draft's `versionNumber` is `currentVersion + 1` with `previousVersionId` pointing at the current draft.
- **`previousDraft` content (added 2026-07-10, see task-05):** `agents/`'s `AssessmentCommand.previousDraft` needs the prior draft's actual content, not just its ID — `agents/` never persists data or calls back into `api/`, so `api/` is the only side that can supply it. This handler renders the loaded `AssessmentDraft` (title, context, instructions, objectives, deliverables, constraints) into a single text block before building the command — a small package-private mapper method (e.g. `AssessmentDraft.toPromptSummary()` on the domain entity, or a dedicated mapper in `application`, whichever keeps domain free of prompt-formatting concerns — decide during implementation) is sufficient; no new persistence or endpoint is needed since the draft is already loaded in step 1 below.
- **Affected files / components:**
  - `assessment/application/command/RegenerateAssessmentDraftCommand.java`, `application/port/in/RegenerateAssessmentDraftUseCase.java`, `application/usecase/RegenerateAssessmentDraftHandler.java` (new — internally delegates the shared call/persist logic to a small package-private helper extracted from task-07's handler, avoiding duplicating the transaction/ordering logic)
  - `assessment/infrastructure/adapter/in/web/AssessmentController.java` (**modify** — add `POST /api/v1/assessments/{id}/draft/regenerate`)
  - request/response DTOs (request includes `adjustmentNotes`)
- **Interfaces / contracts:** `POST /api/v1/assessments/{id}/draft/regenerate` — body `{adjustmentNotes}`, response: the new draft version, same shape as task-07's generation response plus `versionNumber`.
- **Risk:** H (story-level risk R-02) — a bug here could overwrite/lose the previous version; mitigated by never issuing an `UPDATE` on `assessment_drafts` (append-only, enforced by task-03's design), and by a dedicated test asserting the previous version's row is unchanged after regeneration.
- **Design notes:** if no current draft exists for the assessment (regeneration requested before any generation), return 409/422 — regeneration requires a prior draft.

---

## Implementation Steps

1. Extract the shared "call agent outside transaction, then persist log+draft" logic from task-07's `GenerateAssessmentDraftHandler` into a small internal helper method/class both handlers call, so the two handlers do not duplicate that ordering-sensitive logic.
2. Create `RegenerateAssessmentDraftHandler.java`: load the current draft (404/409 if none), load the brief, render the current draft's content to text (`previousDraft`), build the command with `adjustmentNotes` + `previousDraftId` + `previousDraft`, call the shared helper with `versionNumber = current + 1` and `previousVersionId = current draft id`.
3. Add `POST /api/v1/assessments/{id}/draft/regenerate` to `AssessmentController.java`.
4. Create request/response DTOs.
5. Wire the new handler in `AssessmentConfig.java`.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Regeneration creates a new version without altering the previous one | Integration test: generate v1, regenerate to get v2, assert v1's row is byte-for-byte unchanged and both are retrievable |
| 2 | Regeneration without a prior draft is rejected | Test calling regenerate on an assessment with no draft yet, assert 409/422 |
| 3 | Regeneration produces its own distinct `AgentExecutionLog` | Test: assert two separate log rows exist after generate + regenerate, each referencing its own draft |
| 4 | `./mvnw test` passes | Full suite green |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `docker compose up db`; `agents/` running locally for full end-to-end |
| 2 | App compiles and starts | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without errors |
| 3 | Connectivity or schema validation succeeds | No new migration in this task |
| 4 | Changed surface responds correctly | With both services running: generate then regenerate via curl, confirm 2 versions exist |
| 5 | No startup or migration regressions are visible | App logs clean |

### Database / ORM Consistency Check

N/A — no schema change in this task (reuses `V9`–`V12`).

---

## Done Criteria

- [ ] `POST /api/v1/assessments/{id}/draft/regenerate` creates a new version without altering any previous version.
- [ ] Regeneration without a prior draft returns a clean 409/422, not a 500 or NPE.
- [ ] Each regeneration produces its own `AgentExecutionLog`.
- [ ] `./mvnw test` passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-creation-persistence.md)

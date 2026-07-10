# ⚛️ TASK 01 — AssessmentCommand / AssessmentResult contracts

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Define the `AssessmentCommand` and `AssessmentResult` records that form the Assessment Agent's public contract, used by both initial generation (US-011) and regeneration (US-012).

---

## Technical Design

- **Approach:** model both as immutable Java records — idiomatic for Java 21 DTOs with no behavior, and matches the project rule that agents receive `{Agent}Command` and return `{Agent}Result` without persisting anything. `AssessmentCommand` carries the brief fields plus optional regeneration fields (`adjustmentNotes`, `previousDraftId`) so both US-011 and US-012 share one contract instead of a second command/agent.
- **Affected files / components:** new package `agents/src/main/java/cl/gradeops/ai/agents/assessment/`; new files `AssessmentCommand.java`, `AssessmentResult.java`.
- **Interfaces / contracts:**
  - `AssessmentCommand(String learningGoal, String topic, String level, String duration, String language, String adjustmentNotes, String previousDraftId)` — `adjustmentNotes`/`previousDraftId` are `@Nullable`, present only when `api/` requests a regeneration.
  - `AssessmentResult(String title, String context, String instructions, List<String> objectives, List<String> deliverables, List<String> constraints)`.
- **Risk:** Low — routine change, pure data definition, no external calls.
- **Design notes:** field names must exactly match what `api/`'s child planning (`api/.planning/003-assessment-creation`) persists as `AssessmentBrief`/`AssessmentDraft` — cross-check against the enriched user stories before finalizing, since a mismatch here breaks the cross-repo contract silently.

---

## Implementation Steps

1. Create package `agents/src/main/java/cl/gradeops/ai/agents/assessment/`.
2. Create `AssessmentCommand.java` — record with `learningGoal`, `topic`, `level`, `duration`, `language`, plus optional `adjustmentNotes`, `previousDraftId`.
3. Create `AssessmentResult.java` — record with `title`, `context`, `instructions`, `objectives` (`List<String>`), `deliverables` (`List<String>`), `constraints` (`List<String>`).
4. Add Javadoc on both records cross-referencing US-010/US-011/US-012 and the no-persistence rule.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Records compile | `./mvnw -Pbeta compile` succeeds |
| 2 | Field names match the source user stories | Diff manually against `docs/02-product/user-stories/epic-02-assessment-creation/01-assessment-brief-intake.md` and `02-assessment-draft-generation.md` (present in this worktree) |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | None required for this task |
| 2 | App compiles and starts | `./mvnw -Pbeta compile` succeeds; existing `GradeOpsAgentsApplicationTest#contextLoads` still passes |
| 3 | Connectivity or schema validation succeeds | N/A — no new wiring introduced by this task |
| 4 | Changed surface responds correctly | N/A — no runtime surface yet, contracts only |
| 5 | No startup regressions are visible | `./mvnw -Pbeta test -Dtest=GradeOpsAgentsApplicationTest` passes |

### Database / ORM Consistency Check

N/A — no database or ORM involved; agents never persist domain entities.

---

## Done Criteria

- [ ] `AssessmentCommand` and `AssessmentResult` exist under `cl.gradeops.ai.agents.assessment` with exactly the fields specified above.
- [ ] `./mvnw -Pbeta compile` succeeds with no errors.
- [ ] Field names verified against the US-010/US-011/US-012 source docs.
- [ ] Existing `GradeOpsAgentsApplicationTest#contextLoads` still passes.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)

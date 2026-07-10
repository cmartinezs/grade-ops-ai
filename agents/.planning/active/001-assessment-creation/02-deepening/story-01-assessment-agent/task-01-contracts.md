# ⚛️ TASK 01 — AssessmentCommand / AssessmentResult contracts

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Define the `AssessmentCommand` and `AssessmentResult` records that form the Assessment Agent's public contract, used by both initial generation (US-011) and regeneration (US-012).

---

## Technical Design

- **Approach:** model both as immutable Java records — idiomatic for Java 21 DTOs with no behavior, and matches the project rule that agents receive `{Agent}Command` and return `{Agent}Result` without persisting anything. `AssessmentCommand` carries the brief fields plus optional regeneration fields (`adjustmentNotes`, `previousDraftId`) so both US-011 and US-012 share one contract instead of a second command/agent. Both records carry Lombok's `@Builder` (`api/docs/gradeops-ai-java-guidelines/07-lombok.md:76-84` lists this as the recommended pattern for Command/Result records), which required adding the `lombok` dependency plus the standard Spring Boot Maven/compiler-plugin wiring to `agents/pom.xml` (mirroring `api/pom.xml`, since this is the first Lombok use in `agents/`).
- **Affected files / components:** new packages `agents/src/main/java/cl/gradeops/ai/agents/assessment/application/command/` and `.../assessment/application/result/`, per `api/docs/gradeops-ai-java-guidelines/01-arquitectura-hexagonal-y-paquetes.md`'s package template (`<feature>.application.command`, `<feature>.application.result`); new files `AssessmentCommand.java`, `AssessmentResult.java`; `agents/pom.xml` (Lombok dependency + plugin wiring).
- **Interfaces / contracts:**
  - `AssessmentCommand(String learningGoal, String topic, String level, String duration, String language, String adjustmentNotes, String previousDraftId, String previousDraft)` — `adjustmentNotes`/`previousDraftId`/`previousDraft` may be `null`, present only when `api/` requests a regeneration. **`previousDraft` added post-hoc on 2026-07-10**, after task-02's code review found that `assessment-generation.st` needed the prior draft's actual content and `previousDraftId` alone cannot provide it — `agents/` never persists data or calls back into `api/`, so `api/` must resolve the ID on its own side and send the content directly. `previousDraftId` stays for correlation/audit only (e.g. in `AgentExecutionLogPayload`). See `RETROSPECTIVE-RAW.md` for the full finding.
  - `AssessmentResult(String title, String context, String instructions, List<String> objectives, List<String> deliverables, List<String> constraints)`.
- **Risk:** Low — routine change, pure data definition, no external calls.
- **Design notes:** field names must exactly match what `api/`'s child planning (`api/.planning/003-assessment-creation`) persists as `AssessmentBrief`/`AssessmentDraft` — cross-check against the enriched user stories before finalizing, since a mismatch here breaks the cross-repo contract silently.

---

## Implementation Steps

1. Create packages `agents/src/main/java/cl/gradeops/ai/agents/assessment/application/command/` and `.../assessment/application/result/`.
2. Add the `lombok` dependency and the Spring Boot Maven/compiler-plugin annotation-processor wiring to `agents/pom.xml`, copied from `api/pom.xml`.
3. Create `AssessmentCommand.java` in `application.command` — record with `learningGoal`, `topic`, `level`, `duration`, `language`, plus optional `adjustmentNotes`, `previousDraftId`; annotated `@Builder`.
4. Create `AssessmentResult.java` in `application.result` — record with `title`, `context`, `instructions`, `objectives` (`List<String>`), `deliverables` (`List<String>`), `constraints` (`List<String>`); annotated `@Builder`.
5. Add Javadoc on both records cross-referencing US-010/US-011/US-012 and the no-persistence rule.

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

- [x] `AssessmentCommand` and `AssessmentResult` exist under `cl.gradeops.ai.agents.assessment.application.command` / `.application.result` respectively, per the project's hexagonal package template, with exactly the fields specified above.
- [x] Both records carry `@Builder` per `07-lombok.md`; neither record, nor any code in this task, throws a Java API exception (`12-excepciones-y-manejo-de-errores.md`).
- [x] `./mvnw -Pbeta compile` succeeds with no errors.
- [x] Field names verified against the US-010/US-011/US-012 source docs.
- [x] Existing `GradeOpsAgentsApplicationTest#contextLoads` still passes.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)

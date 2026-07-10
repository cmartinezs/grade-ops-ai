# ⚛️ TASK 02 — Prompt template `assessment-generation.st`

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Create the versioned StringTemplate prompt `assessment-generation.st`, covering both initial generation (no adjustment notes) and regeneration (with adjustment notes) in a single file.

---

## Technical Design

- **Approach:** use StringTemplate (`.st`) syntax per project convention — prompts are versioned file-based templates, never inlined in Java. One template with a conditional block (`<if(adjustmentNotes)>...<endif>`) serves both US-011 and US-012, matching `AssessmentCommand`'s nullable `adjustmentNotes`/`previousDraftId` fields from task-01.
- **Affected files / components:** `agents/src/main/resources/prompts/assessment-generation.st`; add Maven dependency `org.antlr:ST4` to `pom.xml` (StringTemplate engine — not yet a project dependency) so task-03 can render it.
- **Interfaces / contracts:** template attributes: `learningGoal`, `topic`, `level`, `duration`, `language`, `adjustmentNotes` (optional), `previousDraft` (optional, rendered summary of the prior version when regenerating). Instructs the model to produce strict JSON matching `AssessmentResult`'s fields (`title`, `context`, `instructions`, `objectives`, `deliverables`, `constraints`).
- **Risk:** M — a poorly structured prompt produces malformed JSON that task-03's validator must catch; mitigated by being explicit about the required JSON shape and field names in the template text itself.
- **Design notes:** keep the template strict about JSON-only output (no prose wrapper), since Spring AI's structured-output conversion in task-03 will attempt to deserialize the response directly into `AssessmentResult`.

---

## Implementation Steps

1. Add `org.antlr:ST4` dependency to `pom.xml` (latest stable 4.x compatible with Java 21).
2. Create `agents/src/main/resources/prompts/assessment-generation.st`.
3. Write the template body: instruction framing (programming-assessment context), the brief attributes, a conditional block for `adjustmentNotes` + `previousDraft` when regenerating, and explicit strict-JSON output instructions matching `AssessmentResult`.
4. Add a header comment in the `.st` file documenting which agent/contract version it belongs to.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Template is syntactically valid StringTemplate | Render it with the ST4 library from a scratch snippet passing sample attributes for both the generation and regeneration cases — no `ST4` parse exceptions |
| 2 | Regeneration branch only renders when `adjustmentNotes` is present | Render twice (with and without `adjustmentNotes`) and diff the two outputs manually |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | None required for this task |
| 2 | App compiles and starts | `./mvnw -Pbeta compile` succeeds after adding the ST4 dependency |
| 3 | Connectivity or schema validation succeeds | N/A — no new Spring wiring, dependency addition only |
| 4 | Changed surface responds correctly | N/A — no runtime surface yet, template file only |
| 5 | No startup regressions are visible | `./mvnw -Pbeta test -Dtest=GradeOpsAgentsApplicationTest` passes |

### Database / ORM Consistency Check

N/A — no database or ORM involved.

---

## Done Criteria

- [ ] `assessment-generation.st` exists under `agents/src/main/resources/prompts/` and is never duplicated inline in Java.
- [ ] Template renders successfully for both the initial-generation case (no `adjustmentNotes`) and the regeneration case (with `adjustmentNotes`).
- [ ] `org.antlr:ST4` dependency added to `pom.xml`; `./mvnw -Pbeta compile` succeeds.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)

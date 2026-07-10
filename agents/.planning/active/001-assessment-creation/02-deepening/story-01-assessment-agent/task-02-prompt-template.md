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
- **Affected files / components:** `agents/src/main/resources/prompts/assessment-generation.st`; add Maven dependency `org.antlr:ST4` to `pom.xml` (StringTemplate engine — not yet a project dependency) so task-03 can render it; `AssessmentGenerationTemplateTest.java` (new — see Verification; `00-principios-rectores.md` #10 requires an automated test, not an ad hoc check, before a feature/task counts as done).
- **Interfaces / contracts:** template attributes: `learningGoal`, `topic`, `level`, `duration`, `language`, `adjustmentNotes` (optional), `previousDraft` (optional, rendered summary of the prior version when regenerating). Instructs the model to produce strict JSON matching `AssessmentResult`'s fields (`title`, `context`, `instructions`, `objectives`, `deliverables`, `constraints`). Header comment format: single line, machine-parseable, e.g. `// assessment-generation.v1` — task-03's `AgentExecutionLogPayload.promptVersion` reads this string verbatim, so the format must stay stable once task-03 depends on it.
- **Risk:** M — a poorly structured prompt produces malformed JSON that task-03's validator must catch; mitigated by being explicit about the required JSON shape and field names in the template text itself.
- **Design notes:** keep the template strict about JSON-only output (no prose wrapper), since Spring AI's structured-output conversion in task-03 will attempt to deserialize the response directly into `AssessmentResult`.

---

## Implementation Steps

1. Add `org.antlr:ST4` dependency to `pom.xml` (latest stable 4.x compatible with Java 21).
2. Create `agents/src/main/resources/prompts/assessment-generation.st`.
3. Write the template body: instruction framing (programming-assessment context), the brief attributes, a conditional block for `adjustmentNotes` + `previousDraft` when regenerating, and explicit strict-JSON output instructions matching `AssessmentResult`.
4. Add a header comment in the `.st` file in the `// assessment-generation.v1` format documenting which agent/contract version it belongs to.
5. Create `AssessmentGenerationTemplateTest.java` under `src/test/java/cl/gradeops/ai/agents/assessment/` (no production class exists yet to mirror — this test targets the classpath resource directly via the ST4 API) — a committed, repeatable JUnit test, not a throwaway script.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Template is syntactically valid StringTemplate | `AssessmentGenerationTemplateTest` loads the resource with the ST4 library and renders it with sample attributes for both the generation and regeneration cases — no `ST4` parse exceptions |
| 2 | Regeneration branch only renders when `adjustmentNotes` is present | Same test class: render twice (with and without `adjustmentNotes`) and assert the regeneration-only text is present/absent accordingly |
| 3 | Header comment is present and matches the documented format | Same test class: assert the first line of the loaded resource matches `// assessment-generation.v1` (or current version) |

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
- [ ] `AssessmentGenerationTemplateTest` (committed, automated) verifies the template renders successfully for both the initial-generation case (no `adjustmentNotes`) and the regeneration case (with `adjustmentNotes`), and verifies the header-comment version format.
- [ ] `org.antlr:ST4` dependency added to `pom.xml`; `./mvnw -Pbeta compile` succeeds.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)

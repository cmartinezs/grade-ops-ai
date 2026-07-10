# ⚛️ TASK 02 — Prompt template `assessment-generation.st`

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-01-assessment-agent.md)

---

## Objective

Design and select the versioned StringTemplate prompt `assessment-generation.st`, covering both initial generation (no adjustment notes) and regeneration (with adjustment notes) in a single file — by exploring multiple candidate variants, comparing them, and validating the winner with real model runs, per `CLAUDE.md`'s "Prompt template tasks" rule.

---

## Technical Design

- **Approach:** use StringTemplate (`.st`) syntax per project convention — prompts are versioned file-based templates, never inlined in Java. One template with a conditional block (`<if(adjustmentNotes)>...<endif>`) serves both US-011 and US-012, matching `AssessmentCommand`'s nullable `adjustmentNotes`/`previousDraftId` fields from task-01. Per `CLAUDE.md`'s prompt-template-task rule, this is not a single-draft task: at least 2-3 meaningfully different candidate variants must be drafted, documented (what/how/why), compared against each other, and the winner validated with real `opencode` CLI runs before it is committed as the final `.st` file.
- **Candidate variants to explore** (draft all, commit only the winner):
  - **Variant A — instruction-heavy:** long-form explicit instructions listing every rule and constraint up front, JSON shape spelled out last.
  - **Variant B — schema-first:** leads with the exact JSON shape/field descriptions as the anchor, brief in framing before it, keeping the "what to return" close to where the model starts generating.
  - **Variant C — few-shot:** includes one short worked example (a compact sample `AssessmentResult` JSON for an unrelated topic) before asking for the real one, to anchor format and tone.
  - A fourth variant is acceptable if a clearly different strategy emerges during drafting; fewer than 2 variants does not satisfy the rule.
- **Comparison criteria:** for each variant, evaluate (against `opencode` CLI runs for both the initial-generation and regeneration cases):
  - Strict JSON-shape adherence (no prose wrapper, no missing/renamed fields).
  - Resistance to malformed/incomplete output on repeated runs.
  - Quality/specificity of generated `objectives`/`instructions`/`constraints` (not generic filler).
  - Prompt length/token cost (affects `AgentExecutionLogPayload.costEstimate` from task-03).
  - Clarity of the regeneration branch's effect (does `adjustmentNotes` visibly change the output vs. initial generation).
- **Affected files / components:** `agents/src/main/resources/prompts/assessment-generation.st` (the selected variant only — the others are documented in this task file's "Prompt Variants Explored" section below, not committed as separate resource files); add Maven dependency `org.antlr:ST4` to `pom.xml` (StringTemplate engine — not yet a project dependency); `AssessmentGenerationTemplateTest.java` (new — see Verification; `00-principios-rectores.md` #10 requires an automated test, not an ad hoc check, before a feature/task counts as done — this covers structural/rendering validity only, not prompt quality, which the `opencode` comparison covers).
- **Interfaces / contracts:** template attributes: `learningGoal`, `topic`, `level`, `duration`, `language`, `adjustmentNotes` (optional), `previousDraft` (optional, rendered summary of the prior version when regenerating). Instructs the model to produce strict JSON matching `AssessmentResult`'s fields (`title`, `context`, `instructions`, `objectives`, `deliverables`, `constraints`). Header comment format: single line, machine-parseable, e.g. `// assessment-generation.v1` — task-03's `AgentExecutionLogPayload.promptVersion` reads this string verbatim, so the format must stay stable once task-03 depends on it.
- **Risk:** M — a poorly structured prompt produces malformed JSON that task-03's validator must catch; mitigated by the variant comparison and `opencode` validation above, not just by being explicit about the JSON shape in the text.
- **Design notes:** keep the template strict about JSON-only output (no prose wrapper), since Spring AI's structured-output conversion in task-03 will attempt to deserialize the response directly into `AssessmentResult`.

---

## Prompt Variants Explored

*Filled in during execution: one subsection per candidate variant (what it does, how, why it's plausible), the `opencode` CLI transcripts/outputs used to evaluate it, and the final comparison table plus the reasoning for the winning variant.*

---

## Implementation Steps

1. Add `org.antlr:ST4` dependency to `pom.xml` (latest stable 4.x compatible with Java 21).
2. Draft the candidate variants (Variant A/B/C above, or the emergent set) as local scratch `.st` content — not yet committed as the final resource.
3. For each variant, render it with representative sample attributes (both the initial-generation and regeneration cases) and run it against the configured model via the `opencode` CLI; capture the raw responses.
4. Write the what/how/why note and fill the comparison table for each variant in this task file's "Prompt Variants Explored" section above, using the criteria listed in Technical Design.
5. Select the winning variant and create `agents/src/main/resources/prompts/assessment-generation.st` with its content.
6. Add a header comment in the `.st` file in the `// assessment-generation.v1` format documenting which agent/contract version it belongs to.
7. Create `AssessmentGenerationTemplateTest.java` under `src/test/java/cl/gradeops/ai/agents/assessment/` (no production class exists yet to mirror — this test targets the classpath resource directly via the ST4 API) — a committed, repeatable JUnit test, not a throwaway script.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | At least 2-3 candidate variants were drafted, documented, and compared | "Prompt Variants Explored" section in this task file is filled in with what/how/why per variant and a comparison table |
| 2 | The winning variant was validated with real model runs, not just static rendering | `opencode` CLI transcripts for both the initial-generation and regeneration cases are captured in "Prompt Variants Explored" and referenced in the task's PR description |
| 3 | Template is syntactically valid StringTemplate | `AssessmentGenerationTemplateTest` loads the resource with the ST4 library and renders it with sample attributes for both the generation and regeneration cases — no `ST4` parse exceptions |
| 4 | Regeneration branch only renders when `adjustmentNotes` is present | Same test class: render twice (with and without `adjustmentNotes`) and assert the regeneration-only text is present/absent accordingly |
| 5 | Header comment is present and matches the documented format | Same test class: assert the first line of the loaded resource matches `// assessment-generation.v1` (or current version) |

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

- [ ] At least 2-3 candidate prompt variants were drafted, each with a documented what/how/why, and compared against each other using the criteria in Technical Design.
- [ ] The winning variant was validated with real `opencode` CLI runs (both initial-generation and regeneration cases) before being committed — not selected on static rendering alone.
- [ ] `assessment-generation.st` exists under `agents/src/main/resources/prompts/` and is never duplicated inline in Java.
- [ ] `AssessmentGenerationTemplateTest` (committed, automated) verifies the template renders successfully for both the initial-generation case (no `adjustmentNotes`) and the regeneration case (with `adjustmentNotes`), and verifies the header-comment version format.
- [ ] `org.antlr:ST4` dependency added to `pom.xml`; `./mvnw -Pbeta compile` succeeds.
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)

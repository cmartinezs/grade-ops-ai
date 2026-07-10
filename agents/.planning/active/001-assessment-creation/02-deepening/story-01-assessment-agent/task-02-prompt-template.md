# ⚛️ TASK 02 — Prompt template `assessment-generation.st`

> **Status:** DONE
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
- **Interfaces / contracts:** template attributes: `learningGoal`, `topic`, `level`, `duration`, `language`, `adjustmentNotes` (optional), `previousDraft` (optional, rendered summary of the prior version when regenerating — backed directly by `AssessmentCommand.previousDraft()`, added to the command post-hoc during this task's code review since `previousDraftId` alone cannot supply content; see task-01-contracts.md). Instructs the model to produce strict JSON matching `AssessmentResult`'s fields (`title`, `context`, `instructions`, `objectives`, `deliverables`, `constraints`). Header comment format: single line, machine-parseable, e.g. `// assessment-generation.v1` — task-03's `AgentExecutionLogPayload.promptVersion` reads this string verbatim, so the format must stay stable once task-03 depends on it.
- **Risk:** M — a poorly structured prompt produces malformed JSON that task-03's validator must catch; mitigated by the variant comparison and `opencode` validation above, not just by being explicit about the JSON shape in the text.
- **Design notes:** keep the template strict about JSON-only output (no prose wrapper), since Spring AI's structured-output conversion in task-03 will attempt to deserialize the response directly into `AssessmentResult`.

---

## Prompt Variants Explored

**Testing method:** the `opencode` CLI in this environment has no Gemini/Vertex AI provider configured (`opencode providers list` shows only `opencode/*` hosted models and `openai/*`) — the production model stays Gemini per `CLAUDE.md`, but for this task `opencode run "<rendered prompt>" -m opencode/deepseek-v4-flash-free --format json` was used as an available, fast, free-tier proxy to test prompt *structure* compliance (JSON-shape adherence, fencing behavior, scoping), which is largely model-agnostic instruction-following behavior. Each variant was rendered with a fixed sample brief (`learningGoal="Evaluate whether students can implement iterative algorithms correctly"`, `topic="Array manipulation and loops"`, `level="introductory"`, `duration="60 minutes"`, `language="Python"`) and, for the regeneration case, a fixed `previousDraft`/`adjustmentNotes` pair. 13 total runs across all variants; outputs validated for JSON-parseability and field completeness with `jq`. **Full rendered prompts and raw outputs for every run are committed** in [`task-02-evidence-variant-a.md`](task-02-evidence-variant-a.md), [`task-02-evidence-variant-b.md`](task-02-evidence-variant-b.md), and [`task-02-evidence-variant-c.md`](task-02-evidence-variant-c.md) (this last file also covers the final refined version) — not only summarized here, so the comparison is re-reviewable locally without depending on a PR description.

### Variant A — instruction-heavy

**What/how:** long-form numbered rule list (8 explicit rules) up front, brief attributes, JSON shape described in prose as rule 7, no worked example. **Why plausible:** maximally explicit — nothing about the required behavior is left implicit.

Results (3 runs: gen×2, regen×1): all 3 valid JSON with all 6 required fields. 1/3 wrapped in a ` ```json ` code fence despite rule 6 explicitly forbidding it (the regeneration run). Generated content was appropriately scoped (single function per assessment). Full transcripts: [`task-02-evidence-variant-a.md`](task-02-evidence-variant-a.md).

### Variant B — schema-first

**What/how:** leads with the literal JSON shape (`{ "title": string, ... }`) before any framing text, brief closing constraint about fit-to-duration. **Why plausible:** puts the exact output contract closest to where generation starts, a common technique for structured-output reliability.

Results (3 runs: gen×2, regen×1): all 3 valid JSON with all 6 fields. 1/3 fenced (first gen run). More notably, both generation runs asked for 4-5 separate functions with edge-case handling inside a stated 60-minute window — over-scoped relative to the brief's own duration constraint, a real quality defect independent of JSON-shape compliance. Full transcripts: [`task-02-evidence-variant-b.md`](task-02-evidence-variant-b.md).

### Variant C — few-shot

**What/how:** opens with one complete worked example (`AssessmentResult` JSON for an unrelated "array rotation" topic) shown bare, no code fence, before asking for the real one. **Why plausible:** "show, don't just tell" — demonstrating the exact bare-JSON formatting tends to be a stronger signal than describing it in prose.

Results (3 runs: gen×2, regen×1): all 3 valid JSON with all 6 fields, 0/3 fenced. Generated content matched the example's single-function complexity level (no over-scoping). Full transcripts: [`task-02-evidence-variant-c.md`](task-02-evidence-variant-c.md).

### Final refinement and validation

Variant C won on both criteria that actually differentiated the candidates (fence-avoidance, appropriate scoping), so it was extended with an explicit scoping line — "prefer a single focused task over multiple functions/parts" — to further guard against Variant B's over-scoping failure mode, and re-validated with 4 more runs (gen×2, regen×2) before being committed as `assessment-generation.st`.

Results (final, 4 runs): all 4 valid JSON with all 6 fields; 1/4 fenced (second regen run) — a reminder that no static prompt design eliminates fencing 100%; Spring AI's structured-output entity mapping (task-03) is expected to tolerate a markdown-wrapped JSON response, so this residual risk is handled at that layer, not solely by the prompt. Full transcripts: [`task-02-evidence-variant-c.md`](task-02-evidence-variant-c.md#final-refinement--added-scoping-line-re-validated).

### Comparison table

| Criterion | A — instruction-heavy | B — schema-first | C — few-shot (winner) |
|---|---|---|---|
| JSON-shape adherence | 3/3 valid | 3/3 valid | 3/3 valid (+4/4 on final) |
| Fence-avoidance (no ` ``` ` wrapper) | 2/3 | 2/3 | 3/3 (6/7 on final incl. refinement) |
| Appropriate scoping to stated duration | Yes | **No** — routinely over-scoped (4-5 functions in 60 min) | Yes |
| Regeneration reflects previous draft + adjustment | Yes | Yes | Yes |
| Relative prompt length | Longest (8 explicit rules) | Medium | Medium (one worked example) |

### Decision

Variant C (few-shot), refined with the explicit single-task scoping line, is the winner — committed as `agents/src/main/resources/prompts/assessment-generation.st`. Raw `opencode` transcripts for all 13 runs (9 across the three initial variants + 4 validating the final refined version) are committed in this task's evidence files (linked above), not only in the PR description.

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
| 2 | The winning variant was validated with real model runs, not just static rendering | `opencode` CLI transcripts for both the initial-generation and regeneration cases are committed in `task-02-evidence-variant-*.md` (rendered prompt inputs, exact command, raw outputs) — reproducible from the repo, not only summarized in this task file or a PR description |
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

- [x] At least 2-3 candidate prompt variants were drafted, each with a documented what/how/why, and compared against each other using the criteria in Technical Design.
- [x] The winning variant was validated with real `opencode` CLI runs (both initial-generation and regeneration cases) before being committed — not selected on static rendering alone.
- [x] `assessment-generation.st` exists under `agents/src/main/resources/prompts/` and is never duplicated inline in Java.
- [x] `AssessmentGenerationTemplateTest` (committed, automated) verifies the template renders successfully for both the initial-generation case (no `adjustmentNotes`) and the regeneration case (with `adjustmentNotes`), and verifies the header-comment version format.
- [x] `org.antlr:ST4` dependency added to `pom.xml`; `./mvnw -Pbeta compile` succeeds.
- [x] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-01-assessment-agent.md)

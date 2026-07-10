# Code Review: story-01-assessment-agent / task-02-prompt-template.md

Re-review date: 2026-07-10

Scope reviewed:

- `.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-02-prompt-template.md`
- `.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-02-evidence-variant-a.md`
- `.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-02-evidence-variant-b.md`
- `.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-02-evidence-variant-c.md`
- `.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-01-contracts.md`
- `.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-03-assessment-agent-service.md`
- `.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-05-unit-tests.md`
- `.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent.md`
- `.planning/active/001-assessment-creation/TRACEABILITY.md`
- `pom.xml`
- `src/main/resources/prompts/assessment-generation.st`
- `src/test/java/cl/gradeops/ai/agents/assessment/AssessmentGenerationTemplateTest.java`
- `src/main/java/cl/gradeops/ai/agents/assessment/application/command/AssessmentCommand.java`
- `src/test/java/cl/gradeops/ai/agents/assessment/application/command/AssessmentCommandTest.java`

## Findings

No blocking findings remain after the latest correction pass.

## Resolved Findings

### Resolved: Model-run evidence count is internally consistent

The previous re-review found that the task claimed 17 runs while the evidence files only contained 13 outputs. The current revision now states 13 runs in the task decision section (`.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-02-prompt-template.md:77`) and in the evidence summary (`.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-02-evidence-variant-c.md:150-158`). Direct recount from the evidence files matches that total: `grep -c "^### Output"` reports 3 outputs in Variant A, 3 in Variant B, and 7 in Variant C/final, for 13 total. The table now reports 3/13 fenced (23%) and 2/13 over-scoped, both Variant B. The retrospective records the arithmetic mistake and correction (`.planning/active/001-assessment-creation/RETROSPECTIVE-RAW.md:36`).

### Resolved: Model-run evidence is no longer external-only

The prior review found that the `opencode` evidence was only summarized in the task and allegedly preserved in a PR description. The current revision adds repo-local evidence files for variants A/B/C and links them from the task file (`.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-02-prompt-template.md:39`, `:45`, `:51`, `:57`, `:63`, `:77`, `:98`). This resolves the original reproducibility-location issue.

### Resolved: Regeneration prompt expected previous draft content that the command/pipeline contract could not provide

The prior review found that `assessment-generation.st` rendered `<previousDraft>` but `AssessmentCommand` only exposed `previousDraftId`. The current revision adds `previousDraft` to `AssessmentCommand` (`src/main/java/cl/gradeops/ai/agents/assessment/application/command/AssessmentCommand.java:39-47`), documents that `api/` must resolve the ID and send the prior draft content (`src/main/java/cl/gradeops/ai/agents/assessment/application/command/AssessmentCommand.java:8-16`), updates task-01's public contract (`.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-01-contracts.md:20-22`), updates task-02's template attributes (`.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-02-prompt-template.md:31`), and updates task-03/task-05 to validate and test the full `adjustmentNotes`/`previousDraftId`/`previousDraft` regeneration triple (`.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-03-assessment-agent-service.md:83-87`, `.planning/active/001-assessment-creation/02-deepening/story-01-assessment-agent/task-05-unit-tests.md:34-40`).

## Non-blocking Notes

- `AssessmentGenerationTemplateTest` verifies the structural contract expected from this task: header format, initial rendering, and regeneration branch rendering.
- `pom.xml` adds `org.antlr:ST4` at compile scope, which matches the planned production use in task-03.
- TRACEABILITY updates are aligned with task-02 and the now-explicit `previousDraft` contract.
- Minor documentation cleanup remains outside this task-02 blocker: task-01's implementation step 3 still says the command has optional `adjustmentNotes` and `previousDraftId`, while its Interfaces/contracts section correctly includes `previousDraft`.
- Evidence files include rendered prompts and raw outputs locally; the documented run count now matches the committed evidence.

## Verification

Commands executed:

```bash
./mvnw -Pbeta compile
./mvnw -Pbeta test -Dtest=AssessmentGenerationTemplateTest
./mvnw -Pbeta test -Dtest=GradeOpsAgentsApplicationTest
./mvnw -Pbeta test
```

All commands passed. Final full suite result: 10 tests, 0 failures, 0 errors, 0 skipped.

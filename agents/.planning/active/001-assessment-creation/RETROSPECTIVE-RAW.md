# Retrospective Raw Notes: [Planning Name]

> [← README](README.md)

Working log for events that were unexpected, corrective, risky, or useful for the final retrospective.

Use `/plan-edge-case <planning-id> -- <what happened>` to add manual entries. Commands may also append entries when they encounter blockers, corrections, skipped work, recovery actions, validation failures, or other non-linear events.

---

## How To Use This File

Capture facts while they are fresh. Do not polish entries here. The final retrospective belongs in `README.md`.

Each entry should answer as many of these as possible:

- What happened?
- What was expected instead?
- How was it resolved or contained?
- What should be carried forward?

---

## Log

<!-- Add newest entries at the top. -->

### 2026-07-10 13:05 - AssessmentCommand also used Objects.requireNonNull and IllegalArgumentException, missed in the first correction pass

- **Source:** human code review (task-01 PR #25), requested correction
- **Related story/task:** story-01-assessment-agent, task-01-contracts
- **What happened:** the immediately preceding correction fixed `AssessmentResult`'s compact constructor but did not check `AssessmentCommand`, which had the same `Objects.requireNonNull` calls for the five required fields, plus an `IllegalArgumentException` for the `adjustmentNotes`/`previousDraftId` pairing check — both banned by `12-excepciones-y-manejo-de-errores.md`.
- **Expected instead:** same reasoning as `AssessmentResult`, mirrored on the input side: `task-03-assessment-agent-service.md`'s `validate(AssessmentCommand)` step already owns rejecting a blank required field with `AssessmentAgentException(INVALID_COMMAND)`. `AssessmentCommand` is the target of Jackson deserialization at the internal REST endpoint (task-04), so constructor-level throwing would fail during deserialization instead of inside that dedicated validation step.
- **Resolution:** removed the compact constructor from `AssessmentCommand` entirely (reverting to the plain record originally specified in this task's Technical Design, which never called for constructor validation). Extended `task-03-assessment-agent-service.md`'s `validate(AssessmentCommand)` bullet to explicitly include the `adjustmentNotes`/`previousDraftId` pairing check, so that invariant isn't silently dropped when task-03 is executed. Updated `AssessmentCommandTest` accordingly.
- **Retrospective signal:** when a correction is scoped to "the record that was just flagged," check sibling records in the same task for the identical pattern before considering the correction done — `AssessmentCommand` and `AssessmentResult` were added together and shared the same (wrong) template.

### 2026-07-10 12:53 - AssessmentResult compact constructor used Objects.requireNonNull, violating the project's exception guideline

- **Source:** human code review (task-01 PR #25), requested correction
- **Related story/task:** story-01-assessment-agent, task-01-contracts
- **What happened:** a prior correction to `AssessmentResult` (see the code-review entry immediately below) added `Objects.requireNonNull` calls to the compact constructor to enforce required fields. `api/docs/gradeops-ai-java-guidelines/12-excepciones-y-manejo-de-errores.md` explicitly lists `throw new NullPointerException(...)` as prohibited in any layer, and states `DomainInvariantViolationException` "reemplaza ... a `Objects.requireNonNull`" — this guideline was not consulted when writing that fix, and the sibling guideline edits added under "Contratos públicos entre artifacts y agentes" (`03-use-cases-orquestadores-y-pasos.md`, `14-checklists.md`) recommended `Objects.requireNonNull` too, contradicting the project's own established rule.
- **Expected instead:** required-field validation for `AssessmentResult` should never throw a raw Java API exception. It also should not happen in the record's constructor at all: `task-03-assessment-agent-service.md` already designs `AssessmentAgentService.validateOutput()` as the single place that rejects an incomplete Gemini structured-output result with `AssessmentAgentException(MALFORMED_OUTPUT)`. Throwing from the record constructor would fail during Spring AI's Jackson deserialization, before that dedicated validation step ever runs — duplicating the check and surfacing the wrong exception type.
- **Resolution:** removed all `requireNonNull` calls from `AssessmentResult`'s compact constructor; it now only normalizes `null` list fields to an empty immutable list (`List.copyOf`) and never throws. Updated the two affected java-guidelines files to stop recommending `Objects.requireNonNull` for cross-artifact `Command`/`Result` contracts, and to state explicitly that a `Result` deserialized from an untrusted external source (LLM output) must not validate required fields in its constructor — that belongs to the pipeline's dedicated validation step, using the artifact's own exception type. Updated `AssessmentResultTest` accordingly (removed the two NPE-rejection tests, added a null-to-empty-list normalization test).
- **Retrospective signal:** when adding validation to a record/DTO in this codebase, check `12-excepciones-y-manejo-de-errores.md` first — "fail fast with `Objects.requireNonNull`" is a common Java default that is explicitly banned here. For agent `Result` records specifically, also check whether a later task in the same story already owns required-field validation as a dedicated pipeline step before adding it to the record itself.

### 2026-07-10 01:49 - AssessmentResult scope narrower than docs/03-ai-agents/assessment-agent.md output contract

- **Source:** `/plan-task` — CHECK-AGNOSTIC-BOUNDARY
- **Related story/task:** story-01-assessment-agent, task-01-contracts
- **What happened:** `docs/03-ai-agents/assessment-agent.md`'s "Output Contract" JSON example describes a much richer shape (`summary`, `learning_objectives`, `student_instructions`, `allowed_resources`, `estimated_duration_minutes`, `difficulty`, `programming_language`, `expected_evidence`, `rubric_seed`, `warnings`, `uncertainty_flags`, `requires_teacher_approval`, `next_action`, `schema_version`) than the `AssessmentResult` record just implemented (`title`, `context`, `instructions`, `objectives`, `deliverables`, `constraints`), and uses different names for two overlapping fields (`learning_objectives`→`objectives`, `student_instructions`→`instructions`).
- **Expected instead:** the task's own Technical Design deliberately scoped `AssessmentResult` to exactly the fields listed in US-011's acceptance criteria ("title, context, instructions, objectives, expected deliverables, and constraints") and the story's Done Criteria — a narrower MVP contract than the aspirational one in `docs/03-ai-agents/assessment-agent.md`.
- **Resolution:** treated as a documentation gap, not an implementation error — the task file and source user stories (US-010/011/012) are the authoritative scope for this story, and `AssessmentResult` was kept as designed. Not fixed in this task; flagged here and in the story's `Inconsistencies Found` table for a deliberate decision on whether `docs/03-ai-agents/assessment-agent.md` should be narrowed to the MVP contract or the richer fields deferred/tracked as residuals.
- **Retrospective signal:** `docs/03-ai-agents/[agent-name].md` files may describe a longer-term/aspirational contract rather than the MVP slice actually atomized into tasks — worth reconciling before the next agent's contract task, or explicitly noting in each agent doc which fields are MVP vs. future.

### 2026-07-10 01:49 - Task branch name collided with story branch ref (git ref prefix conflict)

- **Source:** `/plan-task` — git branch setup (step 3c)
- **Related story/task:** story-01-assessment-agent, task-01-contracts
- **What happened:** `git checkout -b gradeops-agents/story-01-assessment-agent/task-01-contracts` failed with `cannot lock ref ... 'refs/heads/gradeops-agents/story-01-assessment-agent' exists; cannot create '.../task-01-contracts'` — git refs can't have a branch that is simultaneously a leaf and a directory prefix, and the story branch `gradeops-agents/story-01-assessment-agent` already occupies that path.
- **Expected instead:** the documented task-branch convention `<story-branch>/<task-NN-slug>` assumes the story branch name never needs to also be a path segment of a longer branch name.
- **Resolution:** asked the user; adopted `<story-branch>--task-NN-slug` (double-dash separator instead of a further `/`) for all task branches in this story, e.g. `gradeops-agents/story-01-assessment-agent--task-01-contracts`.
- **Retrospective signal:** this collision will recur for every task in every story under the `<worktree-prefix>/story-NN-<slug>` branch naming scheme — worth fixing the convention itself (e.g. always using `--task-NN` instead of `/task-NN`) in the planning template rather than re-deciding it per story.

---

> [← README](README.md)

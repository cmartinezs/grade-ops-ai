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

### 2026-07-10 14:10 - Pre-implementation guideline alignment pass on tasks 02-05 (before any of them were executed)

- **Source:** human direction, after PR #25 (task-01) merged — explicit request to review the story and remaining tasks against `api/docs/gradeops-ai-java-guidelines/` before executing them, to avoid repeating task-01's four reactive-correction rounds
- **Related story/task:** story-01-assessment-agent, tasks 02, 03, 04, 05
- **What happened:** re-read all 15 guideline files against each task's Technical Design. Found: task-02's verification relied on an uncommitted "scratch snippet" instead of an automated test (violates `00-principios-rectores.md` #10); task-03 called Spring AI's `ChatClient` directly from a single flat `AssessmentAgentService`, with no `UseCase` port/`Handler` split (violates `03-use-cases-orquestadores-y-pasos.md`'s "Regla base") and no Gemini-adapter isolation (violates `01-arquitectura-hexagonal-y-paquetes.md`'s own `agents/` package example); `AgentExecutionLogPayload` (5 fields: model/costEstimate/status/startedAt/finishedAt) fell well short of `11-seguridad-observabilidad-y-auditoria.md`'s "Auditoría de agentes AI" minimum (agentExecutionId, agentName, promptVersion, inputHash/outputHash, tokens, errorCode); task-04 had no correlation-ID handling at all despite `11-seguridad...`'s explicit "todo request debe tener correlation ID" rule.
- **Decision process:** the exception/package/naming items were mechanical, fixed directly. Three items were genuine architecture forks with real tradeoffs (KISS/ceremony vs. guideline-literal compliance, cross-repo contract impact) — asked the human explicitly via three questions rather than picking unilaterally. All three were resolved toward full guideline compliance: (1) dedicated `AssessmentGenerationPort`/`GeminiAssessmentGenerationAdapter` instead of direct `ChatClient` use; (2) explicit `GenerateAssessmentDraftUseCase`/`Handler`/`AssessmentAgentOrchestrator` instead of one flat service; (3) `AgentExecutionLogPayload` expanded to the full audit field set now, not deferred as a residual.
- **Resolution:** rewrote task-03 (renamed from "AssessmentAgentService" to "Assessment draft generation"), task-04, and task-05 in full; task-02 got a smaller fix (committed `AssessmentGenerationTemplateTest` replacing the scratch-snippet verification, plus a documented prompt-version-comment format task-03 now depends on for `promptVersion`). Introduced a `shared` package (`cl.gradeops.ai.agents.shared.*`) for the internal-auth filter, correlation-ID filter, and the agents/-wide exception handler — these are inherently cross-cutting (a servlet filter and `@RestControllerAdvice` are app-global by nature in Spring MVC), not a speculative abstraction, even though only one feature (`assessment`) exists so far.
- **Cross-repo impact:** `AssessmentExecutionOutcome`'s JSON shape (now nested under `AssessmentExecutionResponse` at the transport level, with `AgentExecutionLogPayload` far richer than originally specified) is what `api/`'s `agentclient` module (sibling child planning `api/.planning/003-assessment-creation`) will deserialize. That planning was not updated in this pass — flag it before task-04 is executed, since `api/`'s side needs to know the final field list to persist `AgentExecutionLog` correctly.
- **Retrospective signal:** doing this pass *before* any of tasks 02-05 were executed cost one review cycle up front instead of four reactive ones after the fact (task-01's pattern). This is the model to repeat for the next story atomized in this workspace: cross-check `01`, `03`, `06`, `07`, `11`, `12` against every task's Technical Design during `/plan-atomize` itself, before the first `/plan-task` run.

### 2026-07-10 13:40 - Tests did not follow the exhaustive-assertions strategy

- **Source:** human code review (task-01 PR #25), requested correction
- **Related story/task:** story-01-assessment-agent, task-01-contracts
- **What happened:** `AssessmentCommandTest`/`AssessmentResultTest` only asserted the one or two fields each test's name called out, not the full constructed object. `10-testing-calidad-y-automatizacion.md`'s "Estrategia de assertions exhaustivas" requires, in order: not-null result, not-null attributes, expected values per field, state — every test, not just tests that are "about" a specific field.
- **Resolution:** rewrote both test classes so every test asserts the full object: not-null check on the result, not-null checks on every attribute expected to be populated, then the expected value of every field (including fields intentionally left `null`, asserted as such).
- **Retrospective signal:** this is the fourth correction round on the same two files. All four were guideline sections that exist and were simply not consulted before writing the original code/tests. Reinforces the prior entry's conclusion: cross-check the relevant `api/docs/gradeops-ai-java-guidelines/` sections *before* writing, not after.

### 2026-07-10 13:32 - Root cause identified: story/tasks were never written against the java-guidelines; full guideline pass applied to task-01 before closing

- **Source:** human direction, after three consecutive correction rounds on the same task (exceptions, then package structure)
- **Related story/task:** story-01-assessment-agent (all tasks), task-01-contracts
- **What happened:** three separate corrections were needed on `AssessmentCommand`/`AssessmentResult` (banned Java exceptions, then wrong package placement) because `story-01-assessment-agent.md` and its tasks were atomized without cross-checking `api/docs/gradeops-ai-java-guidelines/`. Each fix was reactive — found by human review after the fact — rather than caught by design.
- **Resolution for task-01:** applied the remaining guideline item that had only been noted as optional: added Lombok (`agents/pom.xml`, mirroring `api/pom.xml`'s dependency + plugin wiring) and `@Builder` on both records per `07-lombok.md:76-84`'s explicit Command/Result example. Updated both test classes to construct fixtures via the builder. task-01 is now believed fully aligned with the guideline set (see the full-file review two turns earlier in this log for the section-by-section check).
- **Next step (explicit, deferred until this task/PR is closed):** review `story-01-assessment-agent.md` and tasks 02-05 against the java-guidelines *before* they are executed, so the same categories of mismatch (exceptions, package structure, Lombok usage, DDD layering for task-03's service/exception/log-payload types) don't have to be caught reactively again. Do this as a dedicated pass, not folded into task-02's execution.
- **Retrospective signal:** for any future story in `agents/` or `api/`, cross-check the atomized task files against `api/docs/gradeops-ai-java-guidelines/` (particularly 01, 03, 06, 07, 12) *during atomization*, not after implementation. A short "guideline compliance" line in each task's Technical Design (which sub-packages, which exception type, which Lombok annotations) would have caught all three corrections at plan time instead of after three review rounds.

### 2026-07-10 13:20 - AssessmentCommand/AssessmentResult were placed in the feature root package, not application.command/application.result

- **Source:** human code review (task-01 PR #25), requested correction
- **Related story/task:** story-01-assessment-agent, task-01-contracts
- **What happened:** both records were created directly under `cl.gradeops.ai.agents.assessment`, and the "Contratos públicos entre artifacts y agentes" section added to `03-use-cases-orquestadores-y-pasos.md` during an earlier correction claimed this flat placement was an intentional exception for `agents/` ("se permite ubicar el Command/Result en el package público de la feature"). `01-arquitectura-hexagonal-y-paquetes.md`'s own package template (lines 97-110) places `Command` under `<feature>.application.command` and `Result` under `<feature>.application.result`, and its `agents/` example (`ai.gradeops.agents.grading.domain/application/infrastructure`) shows the same layered structure applies to this artifact too. The carve-out note was not actually grounded in that document — it was an ad-hoc rationalization written without checking it.
- **Expected instead:** `AssessmentCommand` under `assessment.application.command`, `AssessmentResult` under `assessment.application.result`, matching every other feature's contract placement in the guideline.
- **Resolution:** moved both records (and their tests) into `application/command/` and `application/result/` respectively. Removed the incorrect carve-out paragraph from `03-use-cases-orquestadores-y-pasos.md` and replaced it with a pointer to the actual template. Updated task-01-contracts.md's Technical Design, Implementation Steps, and Done Criteria to reference the correct packages.
- **Retrospective signal:** when writing a "here's why this deviates from the guideline" note in a shared doc, verify against the referenced document's actual content before writing it — don't infer/invent a carve-out to justify code that was already written. Task-03 (`AssessmentAgentService`, `AssessmentAgentException`, `AgentExecutionLogPayload`, `AssessmentExecutionOutcome`) has not yet assigned these to specific sub-packages either — worth getting right the first time when that task executes, using this same template (`AssessmentAgentService`/`AssessmentAgentException` likely `application`, `AgentExecutionLogPayload`/`AssessmentExecutionOutcome` likely `application.result` or a dedicated sub-package — decide during task-03, not retrofitted after).

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

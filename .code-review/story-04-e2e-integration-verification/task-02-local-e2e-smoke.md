# Code Review: story-04 / task-02-local-e2e-smoke

## Scope

- Branch: `story-04-e2e-integration-verification--task-02-local-e2e-smoke`
- Initial reviewed commit: `60d4f8b` (`docs(e2e-integration-verification): local e2e smoke script and evidence`)
- Re-reviewed commit: `daac47c` (`fix(e2e-integration-verification): verify persisted AgentExecutionLog for task-02`)
- Task file: `.planning/active/008-assessment-creation/02-deepening/story-04-e2e-integration-verification/task-02-local-e2e-smoke.md`
- Primary changed surface: `scripts/smoke-e2e-local.sh`

## Findings

### Resolved - P1 - The smoke script now verifies the persisted `AgentExecutionLog`

The follow-up commit resolves the prior blocker. After draft generation, `scripts/smoke-e2e-local.sh` now parses `draftId`, queries the compose Postgres database through `docker compose exec -T db psql`, and reads `status`, `model`, `agent_execution_id`, and `draft_id` from `agent_execution_logs` for the generated `assessmentId` (`scripts/smoke-e2e-local.sh:146-173`). It fails if no row exists, if status is not `COMPLETED`, if model or `agent_execution_id` is empty, or if the backfilled `draft_id` does not match the generated draft.

The status literal is correct for this codebase: `agents/` emits `COMPLETED` for successful execution (`agents/src/main/java/cl/gradeops/ai/agents/assessment/application/orchestrator/AssessmentAgentOrchestrator.java:106`), and api-side tests assert the same persisted status. The task evidence was also corrected to document the direct DB verification and two successful real runs (`task-02-local-e2e-smoke.md:103-116`, `task-02-local-e2e-smoke.md:122-127`).

### P1 - The smoke script never verifies the persisted `AgentExecutionLog`

The task and parent story explicitly require evidence that the local real flow produces both a genuine `AssessmentDraft` and a persisted `AgentExecutionLog` (`task-02-local-e2e-smoke.md:12`, `story-04-e2e-integration-verification.md:43-44`). The implementation only validates the public draft response: it checks `title`, `objectives`, and then compares the retrieved draft title (`scripts/smoke-e2e-local.sh:140-159`). The task evidence then marks the run as complete while acknowledging that model/cost/log evidence is not exposed through the response (`task-02-local-e2e-smoke.md:101`, `task-02-local-e2e-smoke.md:107-110`).

That leaves the core objective partially unverified. The API code does persist a log in the success path and then back-fills `draftId` (`DraftGenerationCoordinator.java:79-93`), but this smoke test is supposed to prove the real compose flow, not rely on handler-level code inspection or older integration tests. A regression where the real compose stack writes the draft but fails to persist or back-fill the log would still pass this script and the captured evidence.

Recommended fix: after parsing `draftId` from the generation response, query the compose Postgres service for a matching `agent_execution_logs` row, for example by `draft_id = <draftId>` and `assessment_id = <assessmentId>`, and assert `status = 'SUCCEEDED'` plus non-empty `model`/`agent_execution_id` where available. Print those fields in the pass summary so the task evidence directly satisfies the story-level Done Criteria.

## Verification

- Re-review: `bash -n scripts/smoke-e2e-local.sh` passed.
- Re-review: confirmed `scripts/smoke-e2e-local.sh` remains executable and tracked as mode `100755`.
- Re-review: confirmed `COMPLETED` is the success status used by agents/api code and tests.
- Did not re-run the full e2e smoke locally because it requires live Docker services plus Firebase/Groq credentials; the task file includes fresh real-run evidence for the corrected DB assertion.

## Review Result

Approved for task-02. The prior blocker is resolved; no new findings.

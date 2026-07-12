# Re-Code Review: story-01-groq-provider-adapter / task-03-provider-selection

## Scope

- Task: `agents/.planning/active/002-groq-genai-provider/02-deepening/story-01-groq-provider-adapter/task-03-provider-selection.md`
- Branch reviewed: `gradeops-agents/story-01-groq-provider-adapter--task-03-provider-selection`
- Compared against: `origin/gradeops-agents/story-01-groq-provider-adapter`
- Review date: 2026-07-12
- Re-review date: 2026-07-12

## Findings

No blocking findings remain for `task-03-provider-selection.md`.

## Resolved Findings

### Resolved - `api/` child planning notification exists in the real API worktree

- Files:
  - `agents/.planning/active/002-groq-genai-provider/02-deepening/story-01-groq-provider-adapter/task-03-provider-selection.md:67`
  - `agents/.planning/active/002-groq-genai-provider/TRACEABILITY.md:29`
  - `/home/carlos/projects/gradeops-api/api/.planning/active/003-assessment-creation/02-deepening/story-01-assessment-creation-persistence.md:14`
  - `/home/carlos/projects/gradeops-api/api/.planning/active/003-assessment-creation/02-deepening/story-01-assessment-creation-persistence.md:71`
  - `/home/carlos/projects/gradeops-api/api/.planning/active/003-assessment-creation/TRACEABILITY.md:31`
- Status: Resolved

The previous re-review checked the `api/` directory inside `/home/carlos/projects/gradeops-agents`, which is not the API worktree branch where the notification was made. The real sibling worktree is `/home/carlos/projects/gradeops-api`, currently on `gradeops-api/story-01-assessment-creation-persistence`, and `HEAD` equals `origin/gradeops-api/story-01-assessment-creation-persistence` at `dee9e61d823971ecd9b5980682a99dda250d2c07`.

That real API planning does contain the durable notification: the story cross-repo dependency note references `002-groq-genai-provider`, the Inconsistencies Found table records `provider`/`model` and the Groq default, and API traceability says the `agentclient` mirror DTO must carry nullable `provider`/`model`.

Non-blocking follow-up observed while checking the real API worktree: `task-05-agentclient.md` still contains one stale phrase saying `model` is "currently unused by `agents/`'s pipeline itself"; the story-level inconsistency row and traceability are correct that `model` is now forwarded. That stale phrase belongs to the API planning branch, not to this task-03 closeout.

### Resolved - `AssessmentCommand.model` is no longer silently ignored

The orchestrator now forwards `command.model()` to the selected port (`AssessmentAgentOrchestrator.java:82`), the port contract now accepts `generate(String renderedPrompt, String model)` (`AssessmentGenerationPort.java:25`), and both adapters apply a non-null model as a per-call Spring AI option (`GeminiAssessmentGenerationAdapter.java:39-40`, `GroqAssessmentGenerationAdapter.java:30-31`).

The Gemini adapter test now captures the per-call option and asserts the requested model reaches `GoogleGenAiChatOptions` (`GeminiAssessmentGenerationAdapterTest.java:99-122`). I did not re-run a live Groq call, but the previous silent-ignore path is fixed in the shared port/orchestrator path and both provider adapters compile with their provider-specific option builders.

### Resolved - Manual provider runbook now describes task-03 routing state

The runbook now states that omitting `provider` uses the Groq default, `provider: "gemini"` routes explicitly to Gemini, invalid providers are rejected before a model call, and `model` can override the resolved provider's configured default (`MANUAL-PROVIDER-TESTING.md:57-77`).

## Checks Performed

- `git status --short --branch`
- `git ls-files --others --exclude-standard`
- `git diff --name-status origin/gradeops-agents/story-01-groq-provider-adapter...HEAD`
- `git -C /home/carlos/projects/gradeops-api status --short --branch`
- `git -C /home/carlos/projects/gradeops-api rev-parse HEAD origin/gradeops-api/story-01-assessment-creation-persistence`
- Re-read the real API planning files under `/home/carlos/projects/gradeops-api/api/.planning/active/003-assessment-creation`.
- Re-read prior review findings, task, story, agents traceability, manual provider runbook, API planning dependency files, command/orchestrator/selector/config/adapters/tests.
- `./mvnw -Pbeta compile` - pass
- `./mvnw -Pbeta test` - 21 tests, all passing
- Beta profile non-web startup with dummy provider env values:
  - `timeout 30s env GRADEOPS_GEMINI_API_KEY=dummy GRADEOPS_GEMINI_MODEL=gemini-2.0-flash GRADEOPS_GROQ_API_KEY=dummy GRADEOPS_GROQ_MODEL=llama-3.3-70b-versatile ./mvnw -Pbeta spring-boot:run -Dspring-boot.run.profiles=beta -Dspring-boot.run.arguments=--spring.main.web-application-type=none`
  - Result: started successfully
- Not run in this re-review: live `POST /internal/agents/assessment` against a real provider; this pass did not use real provider credentials.

## Conclusion

Approved. The code-level provider/model selection issue is fixed, the manual runbook is current, the beta suite/startup path is green, and the API child-planning notification exists in the real sibling API worktree/branch.

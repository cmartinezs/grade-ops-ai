# Raw Evidence — task-04: Render post-deploy smoke script and evidence

> Raw, unedited command output backing the findings in [task-04-render-post-deploy-smoke.md](../task-04-render-post-deploy-smoke.md#evidence).
> Two real runs against the live `beta` environment on Render, both captured 2026-07-16.

---

## Run 1 — before the agents/ warm-up fix (script's first version)

```
==> Setting Render CLI workspace...
==> Checking Render service status (grade-ops-ai-api, grade-ops-ai-agents)...
    grade-ops-ai-api (srv-d8oqvejeo5us73b41a80): latest deploy live at commit cd771a0c0c77
    grade-ops-ai-agents (srv-d8oqosernols73erqc3g): latest deploy live at commit 6a9c8fd4e743
==> Warm-up request to https://gradeops-api.onrender.com (cold-start tolerant, up to 90s)...
    warm-up response: HTTP curl: (28) Operation timed out after 90000 milliseconds with 0 bytes received
000 (any HTTP response confirms the service answered, not a 200 requirement)
==> Provisioning test teacher smoke-e2e-1784238255@gradeops.test...
    provisioned firebaseUid=fsCw0IeClidcyntutcIoxo32RtC3
==> Setting test teacher password...
    password set.
==> Obtaining a real Firebase ID token...
    idToken obtained (1021 chars).
==> POST /api/v1/assessments (brief intake)...
    assessmentId=91558009-f0f6-4574-b58d-6588ae7b9cfe
==> POST /api/v1/assessments/91558009-f0f6-4574-b58d-6588ae7b9cfe/draft (triggers agents/ over the real network)...
FAIL: draft generation returned HTTP 422: {"error":"AGENT_CALL_FAILED","message":"AGENT_REJECTED"}
```

This run surfaced two real issues:

1. **Script bug**: the warm-up `curl` call redirected stderr into the captured status variable (`2>&1`), garbling the printed output (a genuine curl connection-level failure, not a clean HTTP status). Fixed in the script by keeping stderr separate and checking for empty/`000` status explicitly.
2. **Only `api/` was warmed up, not `agents/`**: `agents/`'s last activity before this run was a graceful shutdown at `2026-07-16T18:27:19Z` (idle timeout) — fully cold for the whole run. Confirmed separately via a direct timed `curl` to `agents/`'s root: `HTTP 404, time 52.321769s` (cold start took ~52s). `api/`'s root, by contrast, responded in `0.625798s` on a follow-up check — the warm-up failure was a one-off transient hiccup on the very first request, not a real cold-start problem for `api/` itself (matches the same transient-first-request behavior task-02 documented locally).

## Investigation — api/ logs around the Run 1 failure (`2026-07-16T21:45:19Z`)

```
Caused by: org.springframework.web.client.HttpClientErrorException$TooManyRequests: 429 Too Many Requests: "Too Many Requests"
	at org.springframework.web.client.HttpClientErrorException.create(HttpClientErrorException.java:133)
	...
	at cl.gradeops.ai.api.agentclient.AssessmentAgentClient.generate(AssessmentAgentClient.java:37)
```

`agents/` had zero log entries in the entire hour surrounding this request (`render logs --resources srv-d8oqosernols73erqc3g --start 2026-07-16T21:00:00Z --end 2026-07-16T22:00:00Z` → 0 results) — the request never reached application code. Render's edge returned `429` while the instance was still waking from a full cold sleep. This confirmed the fix needed: warm `agents/` up directly, not just `api/`, before triggering the real flow.

---

## Run 2 — after the agents/ warm-up fix

```
==> Setting Render CLI workspace...
==> Checking Render service status (grade-ops-ai-api, grade-ops-ai-agents)...
    grade-ops-ai-api (srv-d8oqvejeo5us73b41a80): latest deploy live at commit cd771a0c0c77
    grade-ops-ai-agents (srv-d8oqosernols73erqc3g): latest deploy live at commit 6a9c8fd4e743
==> Warm-up request to agents/ (https://gradeops-agents.onrender.com), up to 90s...
    agents/ responded: HTTP 404 (any HTTP response confirms the service answered, not a 200 requirement)
==> Warm-up request to api/ (https://gradeops-api.onrender.com/), up to 90s...
    api/ responded: HTTP 401 (any HTTP response confirms the service answered, not a 200 requirement)
==> Provisioning test teacher smoke-e2e-1784241230@gradeops.test...
    provisioned firebaseUid=BErtOqNThOhKbpYJXOFGkXt3mME3
==> Setting test teacher password...
    password set.
==> Obtaining a real Firebase ID token...
    idToken obtained (1021 chars).
==> POST /api/v1/assessments (brief intake)...
    assessmentId=8362f597-b9b3-46d1-8451-809733bddb1f
==> POST /api/v1/assessments/8362f597-b9b3-46d1-8451-809733bddb1f/draft (triggers agents/ over the real network)...
FAIL: draft generation returned HTTP 422: {"error":"AGENT_CALL_FAILED","message":"AGENT_REJECTED"}
```

Both services now warm and responding in well under a second (see confirmatory direct checks below), yet the draft-generation call failed identically. This ruled out cold start as the (sole) cause and pointed to a real, separate problem.

### Confirmatory direct warm-state checks (run immediately before Run 2)

```
$ time curl -sS -o /dev/null -w 'HTTP %{http_code}, time %{time_total}s\n' --max-time 30 "https://gradeops-api.onrender.com/"
HTTP 401, time 0.625798s

$ time curl -sS -o /dev/null -w 'HTTP %{http_code}, time %{time_total}s\n' --max-time 30 "https://gradeops-api.onrender.com/api/v1/assessments"
HTTP 401, time 0.291911s
```

## Investigation — api/ logs around the Run 2 failure (`2026-07-16T22:34:20Z`)

```
Caused by: org.springframework.web.client.HttpClientErrorException$UnprocessableContent: 422 Unprocessable Entity: "{"errorCode":"MALFORMED_OUTPUT","message":"Assessment generation response could not be parsed: 401: Invalid API Key","log":{"agentExecutionId":"4f0d3d00-648c-446d-83ac-4af168a1aa90","agentName":"assessment","model":null,"promptVersion":"// assessment-generation.v1","inputHash":"c1c5190fa085926655452d5cc48c7c5caf5278a04db222cd6c73a74d199ac1fb","outputHash":null,"estimatedInputTokens":null,"estimatedOutputTokens":null,"costEstimate":null,"status":"FAILED","errorCode":"MALFORMED_OUTPUT","startedAt":"2026-07-16T22:34:00.573496129Z","finishedAt":"2026-07-16T22:34:20.084969162Z"},"correlationId":"ea1dade4-3f5d-409b-881b-d15825ebaae0"}"
	at org.springframework.web.client.HttpClientErrorException.create(HttpClientErrorException.java:139)
	at org.springframework.web.client.StatusHandler.createException(StatusHandler.java:132)
	...
	at cl.gradeops.ai.api.agentclient.AssessmentAgentClient.generate(AssessmentAgentClient.java:37)
```

Root cause: `agents/`'s configured LLM provider credential is itself rejected with `401: Invalid API Key`. `agents/src/main/resources/application.yml:16` sets `app.agents.llm.default-provider: groq`, and `agents/src/main/resources/application-beta.yml` maps the `groq` provider's key to `${GRADEOPS_GROQ_API_KEY}` against `https://api.groq.com/openai/v1`. This points to the `grade-ops-ai-agents` Render service's `GRADEOPS_GROQ_API_KEY` environment variable being invalid, expired, or unset in the actual deployed beta environment — a real infra/config gap, not a script defect. `agents/` itself logged nothing at INFO level for this request (`render logs --resources srv-d8oqosernols73erqc3g` for the same window → 0 results); the structured error body embedded in `api/`'s exception is the only available record of what `agents/` returned.

## Root-cause confirmation — actual Render env vars vs. current code (real Render API call)

```
$ curl -sS -H "Authorization: Bearer ${RENDER_API_KEY}" -H "Accept: application/json" \
  "https://api.render.com/v1/services/srv-d8oqosernols73erqc3g/env-vars"
[
  { "envVar": { "key": "INTERNAL_API_SECRET", "value": "[REDACTED — matches the local INTERNAL_API_SECRET value]" } },
  { "envVar": { "key": "AI_MODEL_NAME", "value": "gemini-2.0-flash" } },
  { "envVar": { "key": "GOOGLE_AI_API_KEY", "value": "[REDACTED]" } },
  { "envVar": { "key": "SPRING_PROFILES_ACTIVE", "value": "beta" } }
]
```

*(Values for secret-bearing keys are redacted in this committed evidence file — only key names and non-secret values like `AI_MODEL_NAME`/`SPRING_PROFILES_ACTIVE` are shown. The finding below only depends on which key names exist, not their values.)*

`GRADEOPS_GROQ_API_KEY` is not merely invalid — it is **completely absent** from this list, along with `GRADEOPS_GEMINI_API_KEY`/`GRADEOPS_GEMINI_MODEL`. What *is* set (`AI_MODEL_NAME`, `GOOGLE_AI_API_KEY`) matches `docs/04-architecture/beta-environment-design.md` exactly (lines 93-103, 177, 202-203 reference these same two names). This is design-doc-vs-code drift: the design doc documents the original variable names, `agents/src/main/resources/application-beta.yml` was refactored during `002-groq-genai-provider` to the current `GRADEOPS_*`-prefixed names for both providers, but neither the design doc nor the Render service's actual env vars were updated to match. Both providers (Gemini and Groq) are broken on the deployed service as a result — Groq is simply the one that surfaced, since it's the unconditional `default-provider` in `application.yml:16`.

---

## Run 3 — after the human added `GRADEOPS_GROQ_API_KEY`/`GRADEOPS_GROQ_MODEL` on Render

Verified via a real Render API call that the correctly-named vars are now present (values redacted), and that the service redeployed (`manual` trigger, `live`, `2026-07-16T23:47:54Z`, same commit `6a9c8fd4e743` — a restart to pick up the new env vars, not a code change) before re-running:

```
==> Setting Render CLI workspace...
==> Checking Render service status (grade-ops-ai-api, grade-ops-ai-agents)...
    grade-ops-ai-api (srv-d8oqvejeo5us73b41a80): latest deploy live at commit cd771a0c0c77
    grade-ops-ai-agents (srv-d8oqosernols73erqc3g): latest deploy live at commit 6a9c8fd4e743
==> Warm-up request to agents/ (https://gradeops-agents.onrender.com), up to 90s...
    agents/ responded: HTTP 404 (any HTTP response confirms the service answered, not a 200 requirement)
==> Warm-up request to api/ (https://gradeops-api.onrender.com/), up to 90s...
    first attempt got no response — retrying once (matches the transient first-request behavior task-02 also observed locally)...
    api/ responded: HTTP 401 (any HTTP response confirms the service answered, not a 200 requirement)
==> Provisioning test teacher smoke-e2e-1784245893@gradeops.test...
    provisioned firebaseUid=65KFE3RZa3fiSG7ZSAqFyw6LirF3
==> Setting test teacher password...
    password set.
==> Obtaining a real Firebase ID token...
    idToken obtained (1021 chars).
==> POST /api/v1/assessments (brief intake)...
    assessmentId=5a1a9221-36af-4740-b2a2-6c35879740b0
==> POST /api/v1/assessments/5a1a9221-36af-4740-b2a2-6c35879740b0/draft (triggers agents/ over the real network)...
    draft generated: title="Recursive Factorial Calculation", objectives=2
==> GET /api/v1/assessments/5a1a9221-36af-4740-b2a2-6c35879740b0/draft (confirms persistence)...
    retrieval matches generated draft.

PASS: real brief -> generate -> retrieve flow completed against the deployed beta
environment on Render (https://gradeops-api.onrender.com), confirming grade-ops-ai-api reached
grade-ops-ai-agents over the real deployed network path.
  teacher email     : smoke-e2e-1784245893@gradeops.test
  assessmentId      : 5a1a9221-36af-4740-b2a2-6c35879740b0
  draft title       : Recursive Factorial Calculation
  draft objectives  : 2
  full draft payload:
{
  "draftId": "4c9a28e7-2012-448d-ae43-9f6175b134c3",
  "title": "Recursive Factorial Calculation",
  "context": "Intermediate Python programmers practice implementing recursive algorithms",
  "instructions": "Implement a recursive function that calculates the factorial of a given non-negative integer",
  "objectives": [
    "Understand the concept of recursion",
    "Apply recursion to solve a mathematical problem"
  ],
  "deliverables": [
    "A single Python file containing the recursive factorial function"
  ],
  "constraints": [
    "Must use recursion",
    "Must handle non-negative integers",
    "Must not use iteration",
    "Submission deadline: 60 minutes",
    "Must be implemented in Python"
  ],
  "versionNumber": 1
}
```

Notably, the `api/` warm-up's first-attempt-fails-retry-succeeds path (added after Run 1's investigation) fired for real here and self-healed automatically, exactly as designed.

## Run 4 — immediate re-run, confirms re-runnability (Verification #4)

```
==> Setting Render CLI workspace...
==> Checking Render service status (grade-ops-ai-api, grade-ops-ai-agents)...
    grade-ops-ai-api (srv-d8oqvejeo5us73b41a80): latest deploy live at commit cd771a0c0c77
    grade-ops-ai-agents (srv-d8oqosernols73erqc3g): latest deploy live at commit 6a9c8fd4e743
==> Warm-up request to agents/ (https://gradeops-agents.onrender.com), up to 90s...
    agents/ responded: HTTP 404 (any HTTP response confirms the service answered, not a 200 requirement)
==> Warm-up request to api/ (https://gradeops-api.onrender.com/), up to 90s...
    api/ responded: HTTP 401 (any HTTP response confirms the service answered, not a 200 requirement)
==> Provisioning test teacher smoke-e2e-1784246229@gradeops.test...
    provisioned firebaseUid=3SlYexswVHVDID79ReS3pmheDVg1
==> Setting test teacher password...
    password set.
==> Obtaining a real Firebase ID token...
    idToken obtained (1021 chars).
==> POST /api/v1/assessments (brief intake)...
    assessmentId=3f770426-83b1-4d47-addd-69219b6a3066
==> POST /api/v1/assessments/3f770426-83b1-4d47-addd-69219b6a3066/draft (triggers agents/ over the real network)...
    draft generated: title="Recursive Fibonacci Calculation", objectives=2
==> GET /api/v1/assessments/3f770426-83b1-4d47-addd-69219b6a3066/draft (confirms persistence)...
    retrieval matches generated draft.

PASS: real brief -> generate -> retrieve flow completed against the deployed beta
environment on Render (https://gradeops-api.onrender.com), confirming grade-ops-ai-api reached
grade-ops-ai-agents over the real deployed network path.
  teacher email     : smoke-e2e-1784246229@gradeops.test
  assessmentId      : 3f770426-83b1-4d47-addd-69219b6a3066
  draft title       : Recursive Fibonacci Calculation
  draft objectives  : 2
  full draft payload:
{
  "draftId": "379afca1-7cea-4334-bad3-afd59dceba24",
  "title": "Recursive Fibonacci Calculation",
  "context": "Intermediate Python students practice recursive algorithms under a data structures course.",
  "instructions": "Implement a function that calculates the nth Fibonacci number using recursion.",
  "objectives": [
    "Understand recursive function calls",
    "Apply recursive problem-solving strategies"
  ],
  "deliverables": [
    "A single Python file with the Fibonacci function",
    "A short explanation of the recursion process in comments"
  ],
  "constraints": [
    "Must use recursive function calls",
    "Must run within 60 minutes of development time",
    "No iteration allowed",
    "Submission deadline: 60 minutes from start of assessment"
  ],
  "versionNumber": 1
}
```

Different, equally real generated draft — confirms the script is re-runnable without depending on state from the previous run (fresh timestamp-suffixed teacher email each time, same pattern as task-02's local script).

---

## Local regression check — `smoke-e2e-local.sh` post-refactor (2026-07-17)

Docker was unavailable throughout this task's original implementation (WSL2/Docker Desktop `Input/output error`). Once Docker was available again, ran the refactored `scripts/smoke-e2e-local.sh` twice for real to confirm the `scripts/lib/e2e-smoke-flow.sh` extraction introduced no regression — including the local-only `AgentExecutionLog` Postgres check, which the code review alone couldn't exercise.

```
==> Checking docker compose services (db, api, agents)...
    db, api, agents are running.
==> Provisioning test teacher smoke-e2e-1784249097@gradeops.test...
    provisioned firebaseUid=NQrr1Fn6p9XjYMklFhZT0YCukV32
==> Setting test teacher password...
    password set.
==> Obtaining a real Firebase ID token...
    idToken obtained (1021 chars).
==> POST /api/v1/assessments (brief intake)...
    assessmentId=5438284e-6f4e-4752-a969-316190ae170b
==> POST /api/v1/assessments/5438284e-6f4e-4752-a969-316190ae170b/draft (triggers agents/ over the real network)...
    draft generated: title="Recursive Sequence Calculation", objectives=2
==> GET /api/v1/assessments/5438284e-6f4e-4752-a969-316190ae170b/draft (confirms persistence)...
    retrieval matches generated draft.
==> Verifying persisted AgentExecutionLog in Postgres...
    persisted log confirmed: status=COMPLETED, model=llama-3.3-70b-versatile, agent_execution_id=bf51c371-1bb3-4d1e-b1aa-aa0a54f8bc9b, draft_id backfilled correctly

PASS: real brief -> generate -> retrieve flow completed against the local compose stack,
with a persisted AgentExecutionLog confirmed directly in Postgres (not inferred from shape).
  draft title          : Recursive Sequence Calculation
  log status           : COMPLETED
  log model            : llama-3.3-70b-versatile
```

**Second run** (re-runnability):

```
==> Checking docker compose services (db, api, agents)...
    db, api, agents are running.
==> Provisioning test teacher smoke-e2e-1784249132@gradeops.test...
    provisioned firebaseUid=p5U6Jr2DaAdBxZ2IOMocPNSXAIk1
==> Setting test teacher password...
    password set.
==> Obtaining a real Firebase ID token...
    idToken obtained (1021 chars).
==> POST /api/v1/assessments (brief intake)...
    assessmentId=77cd427d-cea4-47bb-ba35-7bc679bfbe1d
==> POST /api/v1/assessments/77cd427d-cea4-47bb-ba35-7bc679bfbe1d/draft (triggers agents/ over the real network)...
    draft generated: title="Recursive Tree Traversal", objectives=2
==> GET /api/v1/assessments/77cd427d-cea4-47bb-ba35-7bc679bfbe1d/draft (confirms persistence)...
    retrieval matches generated draft.
==> Verifying persisted AgentExecutionLog in Postgres...
    persisted log confirmed: status=COMPLETED, model=llama-3.3-70b-versatile, agent_execution_id=5bc130f1-8f82-46e3-ab16-abb1f0569c73, draft_id backfilled correctly

PASS: real brief -> generate -> retrieve flow completed against the local compose stack,
with a persisted AgentExecutionLog confirmed directly in Postgres (not inferred from shape).
  draft title          : Recursive Tree Traversal
  log status           : COMPLETED
  log model            : llama-3.3-70b-versatile
```

Both runs passed cleanly with distinct real drafts and distinct `AgentExecutionLog` rows, confirming the code-review-based verification was correct — the refactor introduced no regression.

---

> [← task file](../task-04-render-post-deploy-smoke.md)

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

---

> [← task file](../task-04-render-post-deploy-smoke.md)

<a id="top"></a>

# API-A3-HANDOFF — Session A3: Authoring Mutations and Public API

**Status:** Template — fill in and commit as `docs(api): record session A3 handoff` once [CLAUDE-API-A3-PROMPT.md](CLAUDE-API-A3-PROMPT.md)'s tasks are complete. **Parent:** [README](README.md)

Handoff gate fields per [09 — Session Handoff Protocol](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/09-session-handoff-protocol.md#-handoffmd-format), specialized for an intermediate API session. Session A4 will not proceed if any section below is missing or contradicted by the actual branch state — **this handoff is also the contract artifact Session C (Web) consumes**, so its endpoint documentation must be genuinely complete, not a summary.

```markdown
# API Session A3 Handoff — Authoring Mutations and Public API

## Session
A3 — Authoring Mutations and Public API

## Branch
feat/assessment-authoring-operation-foundation-api

## HEAD
<commit hash>

## Starting commit
<should match Session A2's recorded final HEAD>

## Tasks completed
08 — <status>
09 — <status>
10 — <status>

## Commits
<hash> <message>
...

## Endpoints — new
POST /api/v1/assessments/{id}/draft/retry
  Request: <exact shape>
  Response (success): <exact shape, status code>
  Response (errors): <every typed error code this endpoint can return>
GET /api/v1/assessments/{id}/generation-status
  Response: <exact shape>
POST /api/v1/assessments/{id}/revisions
  Request: <exact shape>
  Response: <exact shape>

## Endpoints — changed
POST /api/v1/assessments/{id}/draft/regenerate
  <what changed: expectedRevisionId now required, exact request/response shape>

## Endpoint removed
PATCH /api/v1/assessments/{id}/draft
  <confirm it returns 404/405, and that this is covered by an explicit test>

## Request/response contract artifact for Web
<a concrete, real example request/response for every endpoint above — not a reference back to
 LOCAL-CONTRACTS.md. Session C consumes this directly.>

## Status codes
<full table actually implemented, cross-checked against LOCAL-CONTRACTS.md>

## Conflict semantics
<STALE_REVISION, ALREADY_GENERATED, NO_ACTIVE_OPERATION_TO_RETRY, OPERATION_IN_PROGRESS —
 confirm each is reachable and tested>

## Retry/resume behavior
<confirm retry creates a new AgentAttempt under the same AiOperation, never a new AiOperation
 or Assessment>

## OpenAPI impact
<if this codebase generates/maintains an OpenAPI spec, confirm it was regenerated/updated;
 if not applicable, state "N/A — no OpenAPI generation in this codebase" explicitly>

## Tests
<list new test classes and their pass/fail status, mapped to which contract-table row each
 covers>

## Baseline
Before this session: <test count>
After this session: <test count>
./mvnw -f api/pom.xml clean test → <PASS/FAIL, confirm no regression>

## Known residual risks
<"None new." if none new beyond what A1/A2 already recorded>

## Blockers
<"None." if none>

## Next session prerequisites
Confirm this handoff is complete, including the full contract artifact section.
 Re-run ./mvnw -f api/pom.xml clean test yourself before trusting the test count above.

## Next session first command
Execute CLAUDE-API-A4-PROMPT.md.
```

---

← [README](README.md) | [↑ inicio](#top)

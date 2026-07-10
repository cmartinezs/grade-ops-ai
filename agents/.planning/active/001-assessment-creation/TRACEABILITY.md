# 🔗 Traceability: 001-assessment-creation

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| AG | Agent Runtime (`src/`) |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

<!-- MATRIX-HEADER: plan-init adds one column per area between "Term / Concept" and "Notes" -->
| Term / Concept | AG | W | Notes |
|---------------|----|---|-------|
| `AssessmentCommand` | ❌ | ✅ | Contract record — task-01. Fields must match `api/`'s `AssessmentBrief`/`AssessmentDraft`. |
| `AssessmentResult` | ❌ | ✅ | Contract record — task-01. |
| `assessment-generation.st` | ❌ | ✅ | Versioned StringTemplate prompt under `src/main/resources/prompts/` — task-02. |
| `org.antlr:ST4` | ❌ | ✅ | New Maven dependency (StringTemplate engine), added by task-02. |
| `AssessmentAgentService` | ❌ | ✅ | Fixed-pipeline service — task-03. Owns schema validation and execution-log capture (merged from original candidates 4/5 during atomization). |
| `AgentExecutionLogPayload` | ❌ | ✅ | Execution metadata record returned to `api/` for `AgentExecutionLog` persistence — task-03. |
| `AssessmentExecutionOutcome` | ❌ | ✅ | Bundles `AssessmentResult` + `AgentExecutionLogPayload` — task-03. |
| `AssessmentAgentException` | ❌ | ✅ | Reason-coded exception (`INVALID_COMMAND`, `MALFORMED_OUTPUT`) — task-03. |
| `POST /internal/agents/assessment` | ❌ | ✅ | Internal endpoint consumed by `api/`'s `agentclient` — task-04. Internal-auth header name to be confirmed against `api/`'s existing convention. |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D-01 | Merge original task candidates 4 (schema validation) and 5 (execution-log capture) into task-03 | Both are inseparable steps of the same pipeline call — neither is independently verifiable without `AssessmentAgentService` already existing; `[CHECK-ATOMICITY]` fragment rule applies | Story 01 task breakdown | 2026-07-10 |
| D-02 | Reuse the existing shared-secret internal-auth (`app.internal.secret`) instead of building OIDC | Already scaffolded in `application.yml`; matches the analogous internal-auth pattern used elsewhere in the monorepo. `CLAUDE.md` describes agent endpoints as "OIDC" — flagged as a doc inconsistency, not followed literally | task-04 | 2026-07-10 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R-01 | `CLAUDE.md`'s architecture section says agent endpoints use "service-to-service OIDC auth"; the actual scaffold (`app.internal.secret`) uses a shared secret, not OIDC | None — informational | OPEN | Resolve during task-04 via `RECORD-INCONSISTENCY`; either update `CLAUDE.md` or implement real OIDC, whichever the team decides |

---

> [← planning/README.md](../../README.md)

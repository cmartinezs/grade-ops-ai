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
| `AssessmentCommand` | ✅ | ✅ | Contract record — task-01. Fields match `api/`'s `AssessmentBrief`/`AssessmentDraft` (cross-checked against `api/.planning/003-assessment-creation`). |
| `AssessmentResult` | ✅ | ✅ | Contract record — task-01. Narrower than `docs/03-ai-agents/assessment-agent.md`'s Output Contract example — see Inconsistencies Found #1 in the story file. |
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
| D-02 | Layer the shared-secret internal-auth (`app.internal.secret`) as defense-in-depth on top of Cloud Run's real IAM invoker enforcement, rather than building an app-level OIDC-token validator | Checked `infra/terraform/environments/demo/cloud_run.tf`: `agents/` is `INGRESS_TRAFFIC_INTERNAL_ONLY` and grants `roles/run.invoker` only to `api/`'s service account — this **is** real OIDC-based service-to-service auth, enforced by the Cloud Run platform before a request reaches the app. `CLAUDE.md`'s "OIDC" description is accurate at the infra layer, not a doc error. The shared secret adds a defense-in-depth check that also works in local dev, where there's no real Cloud Run IAM | task-04 | 2026-07-10 (revised, see R-01) |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R-01 | *(Revised 2026-07-10 — superseded by D-02)* Originally flagged as "`CLAUDE.md` says OIDC, scaffold uses a shared secret" — corrected after checking `infra/terraform/environments/demo/cloud_run.tf`: Cloud Run's IAM invoker binding between `api/`'s and `agents/`' service accounts **is** the real OIDC mechanism `CLAUDE.md` describes, enforced at the platform level. The app-level shared secret is intentional defense-in-depth, not a substitute for missing OIDC | None — informational | RESOLVED | No action needed; `agents/`'s app code does not need to independently validate an identity token since Cloud Run enforces it before the request arrives |

---

> [← planning/README.md](../../README.md)

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
| `AssessmentCommand` | ✅ | ✅ | Contract record — task-01. Fields match `api/`'s `AssessmentBrief`/`AssessmentDraft` (cross-checked against `api/.planning/003-assessment-creation`). Gained `previousDraft` (content) on 2026-07-10, post-hoc, per task-02's code review — `previousDraftId` alone can't supply the regeneration prompt's content since `agents/` never persists or calls back into `api/`. `api/`'s child planning needs to know about this new field before it implements `agentclient`'s request shape. |
| `AssessmentResult` | ✅ | ✅ | Contract record — task-01. Narrower than `docs/03-ai-agents/assessment-agent.md`'s Output Contract example — see Inconsistencies Found #1 in the story file. |
| `assessment-generation.st` | ✅ | ✅ | Versioned StringTemplate prompt under `src/main/resources/prompts/` — task-02. Few-shot variant, selected after comparing 3 candidates via `opencode` CLI runs (see task-02's "Prompt Variants Explored"). |
| `org.antlr:ST4` | ✅ | ✅ | Maven dependency (StringTemplate engine), added by task-02 — `4.3.4`. |
| `AssessmentGenerationTemplateTest` | ✅ | ✅ | Structural/rendering test for the template (both cases + header format) — task-02. |
| `GenerateAssessmentDraftUseCase` / `GenerateAssessmentDraftHandler` / `AssessmentAgentOrchestrator` | ❌ | ✅ | Nivel-2 pipeline (port in / thin handler / orchestrator) that replaces the originally-planned single `AssessmentAgentService` class — task-03, decided 2026-07-10 per the pre-implementation guideline alignment pass. |
| `AssessmentGenerationPort` / `GeminiAssessmentGenerationAdapter` | ❌ | ✅ | Isolates the Gemini/`ChatClient` call behind a port + adapter (`infrastructure.adapter.out.gemini`) — task-03. |
| `AgentExecutionLogPayload` | ❌ | ✅ | Execution metadata record returned to `api/` for `AgentExecutionLog` persistence — task-03. Expanded 2026-07-10 to the full `11-seguridad-observabilidad-y-auditoria.md` audit field set (`agentExecutionId`, `agentName`, `promptVersion`, `inputHash`/`outputHash`, tokens, `errorCode`), beyond the original 5-field draft. |
| `AssessmentExecutionOutcome` | ❌ | ✅ | Bundles `AssessmentResult` + `AgentExecutionLogPayload` — task-03. |
| `AssessmentAgentException` | ❌ | ✅ | Reason-coded exception (`INVALID_COMMAND`, `MALFORMED_OUTPUT`) that also carries a partial `AgentExecutionLogPayload` for the failure path — task-03. |
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

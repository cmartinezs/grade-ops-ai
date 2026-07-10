# 🔗 Traceability: 003-assessment-creation

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| AP | `src/` — Java / Spring Boot 4 API |
| DO | `docs/` — documentación |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

<!-- MATRIX-HEADER: plan-init adds one column per area between "Term / Concept" and "Notes" -->
| Term / Concept | AP | DO | W | Notes |
|---------------|----|----|---|-------|
| `Assessment` (real, replaces stub) | ❌ | N/A | ✅ | Aggregate root — task-01. Replaces `StubAssessmentPersistenceAdapter`, whose own comment predicted this exact epic. |
| `AssessmentBrief` | ❌ | N/A | ✅ | task-02. Field names must mirror `agents/`'s `AssessmentCommand`. |
| `AssessmentDraft` | ❌ | N/A | ✅ | task-03. Versioned, append-only. Field names must mirror `agents/`'s `AssessmentResult`. |
| `AgentExecutionLog` | ❌ | N/A | ✅ | task-07. First-class evidence entity per `CLAUDE.md`, kept as its own table (not folded into `AssessmentDraft`). |
| `agentclient` module | ❌ | N/A | ✅ | task-05. Only module allowed to call `agents/`. Plain `RestClient`, no Spring AI dependency needed on this side. |
| `POST /api/v1/assessments` | ❌ | N/A | ✅ | task-06 — brief intake, creates `Assessment` + `AssessmentBrief`. |
| `POST /api/v1/assessments/{id}/draft` | ❌ | N/A | ✅ | task-07 — initial generation. |
| `POST /api/v1/assessments/{id}/draft/regenerate` | ❌ | N/A | ✅ | task-08. |
| `PATCH /api/v1/assessments/{id}/draft` | ❌ | N/A | ✅ | task-09 — in-place edit, no new version. |
| `GET /api/v1/assessments/{id}/draft`, `.../versions` | ❌ | N/A | ✅ | task-10. |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D-01 | Reuse the existing `assessment` bounded context and its `AssessmentStatus`/`AssessmentRepositoryPort`/`ListAssessmentsHandler` rather than inventing a parallel concept | The stub explicitly says "Epic 02 will replace return type with domain Assessment objects" — this story IS that epic | task-01, task-10 | 2026-07-10 |
| D-02 | `Assessment` has no `title` column; dashboard title is derived from the current draft (or brief topic as fallback) | Avoids redundant storage and drift between `Assessment.title` and the draft's actual title | task-01, task-10 | 2026-07-10 |
| D-03 | `agentclient` uses a plain `RestClient` with a shared-secret header (`X-Internal-Key`), not a real fetched Cloud Run OIDC identity token, even though `infra/` grants `roles/run.invoker` between the two services' service accounts | Real OIDC token-fetching is meaningful extra scope (new dependency, no local-dev equivalent); Cloud Run's IAM invoker policy is the primary protection in `demo`, the shared secret is defense-in-depth that also works locally | task-05 | 2026-07-10 |
| D-04 | `AssessmentDraft`'s `objectives`/`deliverables`/`constraints` are stored as JSONB columns, not child tables | These lists have no independent lifecycle outside their draft version | task-03 | 2026-07-10 |
| D-05 | Draft editing (task-09) updates the current version's row in place; only regeneration (task-08) is append-only | A manual teacher edit is not an AI execution and doesn't need version history in this epic's scope | task-09 | 2026-07-10 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R-01 | `agentclient`'s auth uses a shared secret, not the real Cloud Run OIDC identity token the IAM invoker binding implies | None — informational, see D-03 | OPEN | Revisit post-MVP if a stronger service-to-service auth guarantee is needed beyond Cloud Run's network-level IAM enforcement |

---

> [← planning/README.md](../../README.md)

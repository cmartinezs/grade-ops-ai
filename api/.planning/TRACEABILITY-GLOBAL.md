# 🌐 Global Traceability Matrix

> [← planning/README.md](README.md)

Consolidated view of all terms and decisions mapped across the project's repository areas, drawn from all plannings.

> **Status:** Draft

---

## How to Read This Matrix

Each row is a term, concept, or decision introduced in a planning. The columns represent the project's repository areas, configured during `/plan-init`.

<!-- AREAS-REF: populated by plan-init — keep in sync with GUIDE.md AREAS-TABLE -->
| Code | Area |
|------|------|
| `AP` | `src/` — Java / Spring Boot 4 API |
| `DO` | `docs/` — documentación |
| `W` | Planning System (`.planning/`) |

Cell values:
- `✅` — term/concept explicitly present and consistent
- `⚠️` — present but needs review or update
- `❌` — not yet present (gap)
- `N/A` — area not applicable for this term
- `(blank)` — not yet evaluated

---

## Global Matrix

<!-- MATRIX-HEADER: plan-init adds area columns after "Source Planning" -->
| Term / Concept | Source Planning | AP | DO | W |
|---------------|----------------|----|----|---|
| Planning System | framework bootstrap | N/A | N/A | ✅ |
| Workflow (meta) | framework bootstrap | N/A | N/A | ✅ |
| Fundamental Rule | framework bootstrap | N/A | N/A | ✅ |
| `AggregateRoot` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `DomainEvent` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `DomainException` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `HexagonalArchitectureTest` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `PasswordResetCode` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `RawCode` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `TeacherIdentity` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `SignInProvider` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `AuthPort` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `TeacherRepositoryPort` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `PasswordResetCodeRepositoryPort` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `EmailNotificationPort` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `Teacher` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `TeacherId` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `AuthProvider` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `ProvisionTeacherUseCase` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `UpdatePilotFlagsUseCase` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `AssessmentStatus` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `ListAssessmentsUseCase` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `AssessmentRepositoryPort` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `AssessmentSummaryResult` | 001-hexagonal-refactor | ✅ | N/A | ✅ |
| `PasswordResetCodeJpaEntity` (PK fix) | 002-drop-old-password-recovery-requests | ✅ | N/A | ✅ |
| `deleteAllClosedCreatedBefore` | 002-drop-old-password-recovery-requests | ✅ | N/A | ✅ |
| `CleanupPasswordResetCodesUseCase` | 002-drop-old-password-recovery-requests | ✅ | N/A | ✅ |
| `CleanupPasswordResetCodesHandler` | 002-drop-old-password-recovery-requests | ✅ | N/A | ✅ |
| `PasswordResetCodeCleanupJob` | 002-drop-old-password-recovery-requests | ✅ | N/A | ✅ |
| `app.auth.reset-code-retention-days` | 002-drop-old-password-recovery-requests | ✅ | N/A | ✅ |
| `Clock` (bean Spring, testabilidad) | 002-drop-old-password-recovery-requests | ✅ | N/A | ✅ |
| `V8__add_index_prc_created_at` | 002-drop-old-password-recovery-requests | ✅ | N/A | ✅ |
| `Assessment` (real, replaces stub) | 003-assessment-creation | ✅ | N/A | ✅ |
| `AssessmentBrief` | 003-assessment-creation | ✅ | ✅ | ✅ |
| `AssessmentDraft` | 003-assessment-creation | ✅ | ✅ | ✅ |
| `AgentExecutionLog` | 003-assessment-creation | ✅ | N/A | ✅ |
| `agentclient` module | 003-assessment-creation | ✅ | N/A | ✅ |
| `POST /api/v1/assessments` | 003-assessment-creation | ✅ | N/A | ✅ |
| `POST /api/v1/assessments/{id}/draft` | 003-assessment-creation | ✅ | N/A | ✅ |
| `POST /api/v1/assessments/{id}/draft/regenerate` | 003-assessment-creation | ✅ | N/A | ✅ |
| `PATCH /api/v1/assessments/{id}/draft` | 003-assessment-creation | ✅ | N/A | ✅ |
| `GET /api/v1/assessments/{id}/draft`, `.../versions` | 003-assessment-creation | ✅ | N/A | ✅ |

---

## Consolidated Residuals

*Terms or decisions deferred from individual plannings that require global resolution.*

| ID | Term / Issue | Source Planning | Status | Notes |
|----|-------------|----------------|--------|-------|
| R-01 | Migrar cleanup a Cloud Scheduler / Cloud Run Job | 002-drop-old-password-recovery-requests | OPEN | Activar si `min-instances` baja a 0 o se requiere ejecución garantizada en multi-réplica |
| R-02 | `agentclient` authenticates with a shared-secret header, not a fetched Cloud Run OIDC identity token | 003-assessment-creation | OPEN | Revisit post-MVP if a stronger service-to-service auth guarantee is needed beyond Cloud Run's network-level IAM enforcement |
| R-03 | `docs/04-architecture/api-design.md` and `data-model.md` (root `grade-ops-ai-docs` repo) are stale relative to the implemented 5-field brief payload and the hexagonal `Assessment`/`AssessmentBrief`/`AssessmentDraft` split | 003-assessment-creation | OPEN | Documentation-sync pass needed in the docs repo; a PDR recording the hexagonal-split-vs-monolithic-table decision is recommended before or alongside that pass |
| R-04 | `agents/`'s `MALFORMED_OUTPUT` reason code conflates genuine malformed output with live provider failures (rate limits, quota, network errors) | 003-assessment-creation | OPEN | Blocked on `agents/` adding a dedicated reason code — not fixable from `api/`'s side |

---

## Changelog

| Date | Planning | Change |
|------|----------|--------|
| 2026-06-11 | — | Matrix initialized. Area codes configured by plan-init based on project structure. |
| 2026-06-26 | 001-hexagonal-refactor | Planning DONE — 5 stories completed; 21 domain terms registered (shared kernel, auth BC, teacher BC, assessment BC stub, cleanup). |
| 2026-06-30 | 002-drop-old-password-recovery-requests | Planning DONE — 1 story, 10 tasks; 9 términos nuevos (cleanup job, port extension, Clock bean, Flyway V8); 1 residual abierto (Cloud Scheduler migration). |
| 2026-07-14 | 003-assessment-creation | Planning DONE — 1 story, 11 tasks; 10 domain terms registered (Assessment/AssessmentBrief/AssessmentDraft/AgentExecutionLog aggregates, agentclient module, 6 REST endpoints); 3 residuals opened (agentclient auth hardening, docs-repo staleness/PDR recommendation, agents/'s MALFORMED_OUTPUT conflation). |

---

> [← planning/README.md](README.md)

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
| `Assessment` (real, replaces stub) | ✅ | N/A | ✅ | Aggregate root — task-01 DONE. Replaces `StubAssessmentPersistenceAdapter`, whose own comment predicted this exact epic. |
| `AssessmentBrief` | ✅ | ✅ | ✅ | task-02 DONE. Field names verified to mirror `agents/`'s `AssessmentCommand` exactly (checked against source). Inline doc: `docs/guides/003-assessment-creation/story-01-assessment-creation-persistence/task-02-assessment-brief.md`. |
| `AssessmentDraft` | ✅ | ✅ | ✅ | task-03 DONE. Versioned, append-only via `generate`/`regenerate` factories. Field names verified to mirror `agents/`'s `AssessmentResult` exactly (checked against source). JSON list columns mapped via Hibernate's native `@JdbcTypeCode(SqlTypes.JSON)`. Inline doc: `docs/guides/003-assessment-creation/story-01-assessment-creation-persistence/task-03-assessment-draft.md`. |
| `AgentExecutionLog` | ✅ | N/A | ✅ | task-07 DONE. First-class evidence entity per `CLAUDE.md`, kept as its own table (`V12`, not folded into `AssessmentDraft`). Full 16-column field set incl. separate `status`/`errorCode`. `cost_estimate` is `DOUBLE PRECISION` (not `NUMERIC` — Hibernate maps `Double` to `float(53)`; the mismatch failed `ddl-auto=validate` at real startup, caught by the smoke test, now locked in by `AgentExecutionLogPersistenceAdapterIntegrationTest`, which deliberately uses `ddl-auto=validate` unlike its siblings). |
| `agentclient` module | ✅ | N/A | ✅ | task-05 DONE. Only module allowed to call `agents/`. Plain `RestClient`, no Spring AI dependency. `AssessmentCommand`/`AssessmentAgentResponse` verified 2026-07-13 against `agents/`'s actual `AssessmentCommand` (10 fields, incl. `provider`/`model`) and `AssessmentExecutionOutcome`/`AgentExecutionLogPayload` (13-field log). `X-Correlation-Id` generated per call; `X-Internal-Key` shared secret per D-03. |
| `POST /api/v1/assessments` | ✅ | N/A | ✅ | task-06 DONE — brief intake, creates `Assessment` (status `DRAFT`) + `AssessmentBrief` in one `@Transactional` handler, no agent call. Request payload is the narrower 5-field shape (`learningGoal`, `topic`, `level`, `duration`, `language`), matching `AssessmentBrief`/`AssessmentCommand` exactly — see R-02. Validation failures return 422 (project-wide `MethodArgumentNotValidException` convention), not 400. |
| `POST /api/v1/assessments/{id}/draft` | ✅ | N/A | ✅ | task-07 DONE — initial generation. Verifies the authenticated teacher owns the assessment (404 on mismatch, via `OwnershipVerifier`) before calling `agents/`. `agentclient` call happens fully outside any DB transaction (programmatic `TransactionTemplate`, not `@Transactional` — see task-07's Technical Design for why self-invocation would have silently broken this). Failure mapping: `AgentClientException` → 503/422/502 (`UNREACHABLE`/`AGENT_REJECTED`/`AGENT_ERROR`) via `GlobalExceptionHandler`. |
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
| R-02 | `docs/04-architecture/api-design.md`'s `POST /assessments` example payload (root docs repo) has 8 fields — `learningGoal`, `topic`, `language`, `level`, `durationMinutes` (number), `studentCountEstimate`, `constraints`, `teacherNotes` — but `agents/`'s actual `AssessmentCommand` (verified 2026-07-13 against `agents/src/main/java/.../assessment/application/command/AssessmentCommand.java`, ground truth) and this story's task-02 `AssessmentBrief` (mirrors it exactly) only carry 5: `learningGoal`, `topic`, `level`, `duration` (`String`, not `durationMinutes`), `language`. `studentCountEstimate`/`constraints`/`teacherNotes` exist nowhere in the implemented contract or persistence model. **Decided 2026-07-13 (task-06):** `CreateAssessmentBriefRequest` implements the narrower 5-field shape (`learningGoal`, `topic`, `level`, `duration`, `language`, all `String`), matching `AssessmentBrief`/`AssessmentCommand` exactly — avoids silently-dropped fields. | OPEN | `docs/04-architecture/api-design.md` still needs updating to match the implemented 5-field shape. Resolution belongs to the `docs/` repo (`grade-ops-ai-docs`, out of scope for this `api/`-only planning, same as R-03) — flag for a documentation-sync pass once this story closes |
| R-03 | `docs/04-architecture/data-model.md`'s `Assessment` entity (root docs repo, verified 2026-07-13) documents a single monolithic table — `organization_id`, `title`, `learning_goal`, `topic`, `level`, `language`, `duration_minutes`, `student_count_estimate`, `assessment_draft_json` (one JSON column, not versioned), `teacher_notes` all flattened onto one row — which is a materially different shape than this story's actual implemented design: a hexagonal split across `Assessment` (task-01, no title/brief/draft fields), `AssessmentBrief` (task-02, 1:1 FK), and `AssessmentDraft` (task-03, versioned, append-only, one row per generation/regeneration, never a single overwritten JSON blob). No `organization_id`/multi-tenant column exists anywhere in the implemented schema. This is broader than R-02 (which is scoped to the intake payload) — it's the canonical entity-relationship doc itself being stale relative to this whole story's schema. | OPEN | This was a deliberate divergence (see D-01, D-02, D-04 — reuse the existing bounded context, no redundant title column, JSONB for draft list fields), not an oversight, but `docs/04-architecture/data-model.md` was never updated to match. Resolution belongs to the `docs/` repo (`grade-ops-ai-docs`, out of scope for this `api/`-only planning) — flag for a documentation-sync pass once this story closes, ideally backed by a PDR recording the split-table decision |

---

> [← planning/README.md](../../README.md)

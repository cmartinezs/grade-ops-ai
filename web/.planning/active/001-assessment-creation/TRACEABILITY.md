# 🔗 Traceability: 001-assessment-creation

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| WB | Web App (`src/`) |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

<!-- MATRIX-HEADER: plan-init adds one column per area between "Term / Concept" and "Notes" -->
| Term / Concept | WB | W | Notes |
|---------------|----|---|-------|
| Intake screen | ✅ | | `/assessments/new` — brief form entry point that kicks off draft generation (US-010). Introduced by story-02 task breakdown. |
| Draft Builder screen | ✅ | | `/assessments/[id]/draft` — single screen combining draft editor, regenerate action, and version history (US-011 + US-012). Introduced by story-02 task breakdown. |
| Screen Data Facade | ✅ | | `loadAssessmentDraftBuilderPage(assessmentId)` — combines the current-draft and version-list GETs into one page view model per `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md` §7. First concrete use of this pattern in `web/`. |
| AssessmentDraftDto | ✅ | | Mirrors `api/`'s `GenerateAssessmentDraftResponse` verbatim (verified in task-01) — `draftId, title, context, instructions, objectives[], deliverables[], constraints[], versionNumber`. |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| PDR-001 | DS form primitives: `Form`/`Field`/`Input`/`Textarea`/`Select`/`Checkbox`, plus a separate declarative `DynamicForm` for simple linear forms — `Field` centralizes label/required/error/hint; not all forms must go through `DynamicForm`. | Prevents per-screen form duplication (already found twice: login/register's duplicated email validation, and `Field.tsx`/`Input.tsx` splitting label/error inconsistently) before more screens (Intake, Draft Builder, future rubric/question screens) repeat it. | WB | 2026-07-15 |
| PDR-002 | Real end-to-end verification for `web/`↔`api/` seams uses a Firebase Auth Emulator (`api/compose.smoke.yml`) + `@playwright/test`, not mocks: a reusable `authenticatedPage`/`teacher` fixture pair (`web/e2e/fixtures/auth.ts`) programmatically creates+verifies a teacher and logs in via the real UI; `web/scripts/e2e-test.sh` boots the whole stack, runs the suite, and tears down. | Jest-mocked tests never exercise the browser↔proxy↔CORS↔auth-whitelist seam — task-13's manual walkthrough found 3 real bugs there (rewrite dropping `/api`, `EmailVerifiedFilter`'s whitelist matching the wrong path, CORS missing `PATCH`) that no prior test caught. task-15 made that verification deterministic and reusable instead of a one-off manual pass, so future authenticated screens get the same coverage for free. | WB | 2026-07-21 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| — | *None* | — | — | — |

---

> [← planning/README.md](../../README.md)

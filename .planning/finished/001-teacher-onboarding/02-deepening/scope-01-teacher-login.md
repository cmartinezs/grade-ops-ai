# 🔍 DEEPENING: Scope 01 — Teacher Login

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

As a programming educator, I want to access my teacher workspace so I can manage my assessments securely.

> Source: `docs/02-product/user-stories/epic-01-teacher-onboarding/01-teacher-login.md` — US-001

## Area

WB (`web/`), AP (`api/`)

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [FirebaseTokenFilter en api/](scope-01-teacher-login/task-01-firebase-token-filter.md) | GENERATE-DOCUMENT | DONE | `FirebaseTokenFilter.java`, `AuthenticatedTeacher` record |
| 2 | [Página de login en web/](scope-01-teacher-login/task-02-sign-in-page.md) | GENERATE-DOCUMENT | DONE | `web/src/app/login/page.tsx` |
| 3 | [AuthGuard (ruta protegida)](scope-01-teacher-login/task-03-protected-route-wrapper.md) | GENERATE-DOCUMENT | DONE | `web/src/components/auth/AuthGuard.tsx` |

---

## Done Criteria

- [x] Teacher can sign in.
- [x] Teacher can see their assessments.
- [x] Teacher can create a new assessment (entry point reachable; creation flow is Epic 02).
- [x] Teacher cannot see another teacher's private assessment data.
- [x] (DoD) Sign-in flow implemented in `web/` with error state for invalid credentials.
- [x] (DoD) Authentication endpoint and session handling implemented in `api/`; credentials never logged or exposed client-side.
- [x] (DoD) All assessment queries scoped server-side to the authenticated teacher, verified by an integration test.
- [x] (DoD) Tests cover: successful sign-in, failed sign-in, unauthenticated access to a protected route.
- [x] (DoD) No agent involvement — no `AgentExecutionLog` required.
- [x] TRACEABILITY.md updated with new terms from this scope

---

## Technical Notes

- Auth mechanism: Firebase Authentication (Google Identity Platform); the API validates Firebase ID tokens and never stores credentials. Record the ADR in `docs/99-decisions/`.
- Authorization enforced in `api/`, never only in the frontend; types flow API → Web.
- Students never log in (signed token links — Epic 13); auth model is teacher-only.
- Cross-teacher denial behavior is specified in detail by scope-07.

## Dependencies

| Depends on | Reason |
|------------|--------|
| scope-06 or scope-08 | An account must exist before sign-in is possible |

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../../README.md)

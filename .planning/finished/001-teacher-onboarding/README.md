# Planning 001 — Teacher Onboarding and Workspace

> [← planning/README.md](../../README.md)
>
> **Status:** COMPLETED — 2026-06-13
> **Source:** `docs/02-product/user-stories/epic-01-teacher-onboarding/` (9 stories → 11 scopes)

---

## Intent

Establish the teacher's authenticated entry point and operational home base: registration, email verification, sign-in/out, dashboard, account provisioning, workspace isolation, and pilot flagging.

---

## Scope Outcomes

| Scope | Status | Key Output |
|-------|--------|-----------|
| scope-01 teacher-login | DONE | `LoginPage`, `AuthGuard`, `AuthenticatedTeacher` principal |
| scope-02 assessment-dashboard | DONE | `GET /assessments` (stub), `DashboardPage`, `AssessmentCard` |
| scope-03 pilot-account-flag | DONE | `V2__add_pilot_flag_columns.sql`, `PATCH /internal/teachers/{uid}/flags` |
| scope-04 sign-out-session-expiry | DONE | `POST /auth/sign-out`, `apiClient` interceptor, `SignOutButton`, expired-session banner |
| scope-05 dashboard-empty-state | DONE | `EmptyDashboard` component wired into `DashboardPage` |
| scope-06 teacher-account-provisioning | DONE | `POST /internal/teachers`, `ProvisionTeacherService` |
| scope-07 cross-teacher-access-denial | DONE | `OwnershipVerifier`, `logback-spring.xml` JSON logging |
| scope-08 teacher-self-registration | DONE | `POST /auth/register`, `RegisterPage` |
| scope-09 email-verification | DONE | `EmailVerifiedFilter`, `VerifyEmailPage` |
| scope-10 firebase-identity-platform-setup | DONE | Terraform: Identity Platform, Firebase Admin SA, web app config |
| scope-11 firebase-auth-adr | DONE | `docs/99-decisions/2026-06-12-firebase-authentication.md` |

---

## Retrospective

### What went well

- **Parallel scope execution.** Running 3 agents in parallel for 4 scopes (scope-02+07, scope-03, scope-04) worked without file conflicts because the grouping was chosen to avoid overlapping files.
- **Filter chain design is clean.** `InternalAuthFilter → FirebaseTokenFilter → EmailVerifiedFilter` with explicit `request.setAttribute("firebaseToken", decodedToken)` handoff gives a clear, testable chain with no hidden coupling.
- **Ownership denial via 404, not 403.** `OwnershipVerifier` throwing `ResourceNotFoundException` (404) is the right call — it doesn't reveal resource existence to unauthorized callers. This was a deliberate design decision, not a default.
- **Stub-first assessment list.** Returning `[]` from `AssessmentService.listForTeacher()` was the right MVP choice — the endpoint contract is established and Epic 02 will fill it in without changing the API surface.
- **ADR captured all four auth decisions upfront** (D-01 through D-04 in TRACEABILITY.md), preventing scope drift in later scopes.

### What didn't work / required on-the-fly correction

- **Wrong Java package on initial scaffolding.** The first agents used `com.gradeops.api` instead of `cl.gradeops.ai.api`. Required a full find-and-replace migration of 26 Java files. Root cause: the correct base package wasn't surfaced before agent prompts were written.
- **Terraform provider attribute mismatches.** Three resource types (`google_project_service`, `google_service_account_key`, `google_secret_manager_secret`) don't accept a `description` attribute, and `google_identity_platform_default_supported_idp_config` for the "password" IDP required `client_id`/`client_secret` that don't apply to email/password sign-in. These had to be discovered and corrected manually.
- **SecurityConfig owned by two parallel agents.** `FirebaseTokenFilter` (scope-01) and `EmailVerifiedFilter` (scope-09) both needed to register in `SecurityConfig`. Handled by designating scope-01 as the owner and scope-09 reporting its required changes for manual application — worked, but required coordination.
- **`firebaseToken` request attribute not set initially.** `FirebaseTokenFilter` did not set `request.setAttribute("firebaseToken", decodedToken)` in the first iteration, breaking `EmailVerifiedFilter` which depends on it. Added manually.

### Deviations from original scope

- **scope-02 assessment list is a stub.** The task spec said "no new migration needed" and the agents created `AssessmentService` returning an empty list. The `assessment` table is Epic 02's domain. No deviation from intent, but worth noting that the endpoint is not yet backed by real data.
- **`logstash-logback-encoder` was already in `pom.xml`** when scope-07 tried to add it (scope-07 agent added it after the dependency was already present from an earlier agent). The duplicate was not written because the agent read the file first.

### Open improvements for future plannings

- **Surface the Java base package in the agent briefing template** — or create a project-level CLAUDE.md note that is automatically included in every agent prompt. Prevents the `com.gradeops.*` error class.
- **Terraform resource attribute validation should be a pre-execution step** — a lightweight `terraform validate` run before applying would catch provider schema mismatches without deploying.
- **SecurityConfig ownership rule** — when multiple scopes touch the security filter chain, the first scope to execute should own `SecurityConfig` and subsequent scopes should patch it via a coordinated step, not silently skip.
- **`OwnershipVerifier` has no call sites yet** — it will be first used in Epic 02 when single-resource endpoints are added. The component is wired and tested; just no callers today.

---

> [← planning/README.md](../../README.md)

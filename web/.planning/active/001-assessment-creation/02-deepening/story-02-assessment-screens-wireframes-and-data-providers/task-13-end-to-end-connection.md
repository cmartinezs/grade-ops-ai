# ⚛️ TASK 13 — end-to-end-connection

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-06, task-12
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

The full Intake → Draft Builder flow works end-to-end against a real local `api/` instance with real ids, no fake/mock data remains anywhere in `src/features/assessment-creation/`, and story-01's Done Criteria that this story's scope covers are demonstrably satisfiable on top of it.

---

## Technical Design

- **Approach:** This task does not add new UI or API functions — `task-06` and `task-12` already connected each screen independently. This task's job is the **seam between them**: confirm the redirect from Intake actually lands on a working Draft Builder screen with real data, and do a final repo-wide sweep for anything `task-04`/`task-09` left behind (fake timers, hardcoded fixtures, dead feature flags).
- **Affected files / components:** No new files expected. Possible small fixes if the sweep finds residue: any leftover fake-data code, unused imports, or a `features/assessment-creation/mocks/` directory if one was created and not cleaned up.
- **Interfaces / contracts:** None new — this task validates the existing contracts from `task-01` hold end-to-end, not just per-function in isolation.
- **Risk:** Medium — integration seams (redirect target, id propagation, version refetch timing) are exactly the class of bug that per-task unit tests don't catch; this is the only task in the story that exercises the real flow start to finish.
- **Design notes:** Per `02-ux-wireframes-y-maquetas.md` §2 step 9 ("agregar pruebas"), this is also the point to confirm the story-01 Done Criteria this scope covers are actually testable against real behavior, not just plausible on paper.

---

## Implementation Steps

1. `grep -r` for leftover fake-data markers (`setTimeout`, hardcoded fixture objects, a stray `mocks/` directory) under `src/features/assessment-creation/`; remove or justify each hit.
2. With `api/` running locally, manually walk the full flow: submit a real brief on `/assessments/new` → confirm redirect to `/assessments/{realId}/draft` → confirm the generated draft renders → edit a field and save → confirm the edit persists after a page refresh → regenerate with adjustment notes → confirm a new version appears and the version history shows the prior one.
3. Confirm story-01's Done Criteria that fall within this story's scope hold against the real flow just walked (not the mockups): required-field validation blocks submission; brief persists before the agent call; draft is fully editable and edits persist via the API; regeneration works with adjustment notes; previous versions remain accessible; draft and versions survive a page refresh.
4. Run the full test suite and lint once more across everything this story touched.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | No fake-data code remains under `src/features/assessment-creation/` | `grep -r "setTimeout\|mocks/" src/features/assessment-creation/` returns nothing unexplained |
| 2 | Full flow (submit brief → generated draft → edit → save → regenerate → version history) works against real `api/` | Manual walkthrough with `api/` running locally |
| 3 | Draft and version history survive a page refresh (re-fetched from the API, not held only in client state) | Manual refresh mid-walkthrough |
| 4 | Story-01's in-scope Done Criteria hold against the real flow | Manual cross-check against `../story-01-assessment-creation-ui.md` § Done Criteria |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | `api/` running locally via `./mvnw spring-boot:run` (with its own local Postgres per `api/`'s own setup) |
| 2 | App compiles and starts | `npm run build` then `npm run dev` |
| 3 | Connectivity check succeeds | `web/` successfully reaches `api/` at the configured `NEXT_PUBLIC_API_BASE_URL` (no CORS/network errors in the browser console) |
| 4 | Changed surface responds correctly | The full manual walkthrough in Implementation Step 2 completes without unhandled errors |
| 5 | No regressions are visible | Existing `/assessments` list page and unrelated routes still render without new console errors |

### Database / ORM Consistency Check

N/A — this task touches no database or ORM artifacts directly; it exercises `api/`'s existing, already-`DONE` persistence layer as a black box.

### Logging / Observability

- **Logging mechanism:** Confirm whatever mechanism was decided in `task-05`/`task-10`/`task-11` is actually producing log output during the manual walkthrough — this task is the first point where the full chain of correlation ids (submit → generate → load → save → regenerate → refetch) can be observed together.
- **Correlation / trace context:** Confirm each logical operation (brief submission, draft load, edit save, regeneration) has its own correlation id and none leak across unrelated operations.
- **Levels by event criticality:** N/A beyond what earlier tasks defined — this task verifies, doesn't add new log points.
- **Execution trace points:** N/A — verification only.
- **Sensitive data guardrails:** Confirm no full draft text, adjustment notes, or brief content appears in captured log output during the walkthrough.
- **Verification evidence:** A captured log excerpt from the manual walkthrough showing distinct correlation ids per logical operation with no sensitive payload content.

### Generated Test Suite

- **Task suite file:** `test-suites/task-13-end-to-end-connection-test-suite.md`
- **Required gates:** integration/acceptance (manual, since no e2e browser-automation harness exists yet in this project), smoke, security (confirm no secrets/tokens logged), architecture/design guide review (confirm story-01 Done Criteria hold).
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2 (full design-to-connection flow), `06-estado-datos-y-api.md` §13 (sync with backend).
- **Acceptance environment:** Real local `api/` instance (`./mvnw spring-boot:run` + its local Postgres), real local `web/` (`npm run dev`). No Docker Compose/Testcontainers needed — both services already support standalone local startup per their own existing setup.
- **Acceptance dependency inventory:** `api/` local instance and its database; `NEXT_PUBLIC_API_BASE_URL` pointed at it; a Firebase-authenticated teacher session (existing auth flow, out of this story's scope to build).
- **Missing acceptance profile:** N/A — not a Maven/Cucumber service; this is a manual walkthrough by design since no acceptance harness exists yet for `web/`.

---

## Done Criteria

- [ ] No fake-data residue remains under `src/features/assessment-creation/`.
- [ ] The full flow works end-to-end against real `api/` with real ids.
- [ ] Draft and version history survive a page refresh.
- [ ] Story-01's in-scope Done Criteria are confirmed against the real flow, not just the mockups.
- [ ] Full test suite and lint pass across everything this story touched.
- [ ] Software smoke test check above passes (services ready, build/startup/connectivity confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging follows `.planning/LOGGING.md`: correlation/trace context present across the full walkthrough, with INFO/DEBUG/WARN/ERROR levels chosen by criticality, per this task's Logging / Observability section.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply directly; `api/`'s existing persistence layer is exercised only as a black box.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

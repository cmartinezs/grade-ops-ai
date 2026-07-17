# ⚛️ TASK 12 — connect-real-api-draft-builder-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-09, task-10, task-11
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

The Draft Builder screen loads via `loadAssessmentDraftBuilderPage`, saves edits and regenerates via the real mutations, refetches versions after either succeeds, and surfaces 404/422/500 (plus 502/503 agent errors) — the fake local dataset from `task-09` is fully removed.

---

## Technical Design

- **Approach:** Swap `useAssessmentDraftBuilderPage`'s fake dataset and fake `onSave`/`onRegenerate` (from `task-09`) for a call to `loadAssessmentDraftBuilderPage` (from `task-10`) on mount and the real mutations (from `task-11`) — no other component or prop-shape changes, since `task-09` already built the real component tree with `onSave`/`onRegenerate` owned by the page hook and passed through to the Sections unchanged (`task-08`'s hierarchy).
- **Affected files / components:**
  - `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` (replace fake data with the real loader; replace fake save/regenerate handlers with the real ones)
  - `src/app/(protected)/assessments/[id]/draft/page.tsx` (pass `params.id` through)
  - Section test files (extend for real error states)
- **Interfaces / contracts:** The page hook's public shape (`RemoteData`-style page state, per `06-estado-datos-y-api.md` §8, including `onSave`/`onRegenerate` and their `isSaving`/`isRegenerating`/error fields per `task-08`) stays what `task-09` established — only what feeds it changes, from fake data/fake mutations to `task-10`'s loader and `task-11`'s real mutation functions.
- **Risk:** Medium — per `06-estado-datos-y-api.md` §13, after regenerate (always) or a save (which always edits the current version, since the editor is read-only while previewing history per `task-08`), the version list must be refetched, not assumed stale-safe; missing this would show the teacher an outdated version list right after they just created a new version.
- **Design notes:** Per `15-backend-frontend-contracts.md` §4 and `task-07`'s traced backend contract: 404 → assessment not found (shouldn't normally happen via the normal navigation flow from `task-06`, but handle it defensively — e.g. a stale bookmark); 422 → business validation error (field validation / empty notes / agent-rejected / no-prior-draft), translate the message; 502/503 → agent-down, translate the message, current draft is never cleared. **No 409 handling** — `task-07`/`task-08` both confirmed no draft endpoint returns one (`GlobalExceptionHandler` maps 409 only for the unrelated `DuplicateEmailException`); the last-write-wins concurrency risk this replaces is a documented backend limitation, not a UI-detectable conflict.

---

## Implementation Steps

1. Replace `useAssessmentDraftBuilderPage`'s fake dataset with a `loadAssessmentDraftBuilderPage(assessmentId)` call on mount, using the `RemoteData` states from `task-09`.
2. Replace the page hook's fake `onSave` with a call to `updateAssessmentDraft` (from `task-11`), and on success, refetch the version list (or the full page data) via `loadAssessmentDraftBuilderPage` — `DraftEditorSection`'s own props/behavior are unchanged from `task-09`, only what the page hook's `onSave` does internally changes.
3. Replace the page hook's fake `onRegenerate` with a call to `regenerateAssessmentDraft` (from `task-11`), and on success, refetch the version list and update the displayed draft to the new version — same "Section props unchanged" note as step 2.
4. Map 404/422/500 (plus 502/503 agent errors) to translated messages per `15-backend-frontend-contracts.md` §4, surfaced via the existing error-state UI from `task-09`. No 409 mapping exists — `task-07`/`task-08` confirmed no draft endpoint returns one.
5. Extend Section tests to cover: successful save/regenerate refreshes the version list; an agent-rejected (422) or agent-down (502/503) error during regenerate shows a clear message without clearing the current draft (`task-07`); a 404 on initial load shows a "not found" state.
6. Remove the fake dataset and fake `onSave`/`onRegenerate` code paths entirely from `task-09` — no leftover dead code or feature flag.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Initial load calls the real `loadAssessmentDraftBuilderPage` and renders the returned draft/versions | `npm run test` |
| 2 | Successful save refetches and reflects the updated draft | `npm run test` |
| 3 | Successful regenerate refetches the version list and shows the new version as current | `npm run test` |
| 4 | 404/422/500 (plus 502/503 agent errors) each render a distinct, translated message — no 409 case exists (`task-07`/`task-08`) | `npm run test` |
| 5 | No fake/mocked dataset remains in the hook | Manual code review |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | Changed surface responds correctly | With `api/` running locally and a real assessment id from `task-06`'s flow, load the draft screen, edit and save, then regenerate, and confirm both reflect in the UI with an updated version list |
| 3 | No startup regressions are visible | Inspect `npm run dev` output for new errors |

### Database / ORM Consistency Check

N/A — no database or ORM involved in `web/`.

### Logging / Observability

- **Logging mechanism:** Reuse whatever was decided in `task-05`/`task-10`/`task-11` — do not re-decide.
- **Correlation / trace context:** Initial load uses the loader's correlation id (`task-10`); save/regenerate each use their own mutation-scoped id (`task-11`), plus the follow-up refetch after either should carry a new load-scoped id since it's a fresh page-data read, not part of the mutation itself.
- **Levels by event criticality:** INFO on successful load/save/regenerate; WARN on 404/422/502/503 (recoverable via navigation/correction/retry); ERROR on 500. No 409 level applies (`task-07`/`task-08`: no draft endpoint returns one).
- **Execution trace points:** Page mount → loader call; save/regenerate handler entry → mutation call → refetch call → completion/failure.
- **Sensitive data guardrails:** Same as `task-10`/`task-11` — no full draft text or adjustment notes in logs.
- **Verification evidence:** A test or manual log sample showing the full load → edit → save → refetch sequence with correlation ids present at each step.

### Generated Test Suite

- **Task suite file:** `test-suites/task-12-connect-real-api-draft-builder-screen-test-suite.md`
- **Required gates:** unit, coverage, integration, static analysis (`npm run lint`), code style, smoke, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md`, `15-backend-frontend-contracts.md`.
- **Acceptance environment:** Mock `apiClient`/`fetch` for unit tests; a manual smoke pass against a real local `api/` instance (`./mvnw spring-boot:run`) covers the integration gate — no Docker/Testcontainers needed.
- **Acceptance dependency inventory:** `api/` running locally at the configured `NEXT_PUBLIC_API_BASE_URL`; a real assessment id with an existing draft (produced via `task-06`'s flow) to test against.
- **Missing acceptance profile:** N/A — not a Maven/Cucumber service.

---

## Done Criteria

- [ ] Draft Builder screen loads, edits/saves, and regenerates against the real API.
- [ ] Version list refetches after both save and regenerate.
- [ ] 404/422/500 (plus 502/503 agent errors) each show a distinct, translated message — no 409 case, since none exists for these endpoints (`task-07`/`task-08`).
- [ ] No fake/mocked dataset remains.
- [ ] All tests pass; `npm run lint` passes.
- [ ] Software smoke test check above passes (build/startup/connectivity confirmed against a real local `api/`); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

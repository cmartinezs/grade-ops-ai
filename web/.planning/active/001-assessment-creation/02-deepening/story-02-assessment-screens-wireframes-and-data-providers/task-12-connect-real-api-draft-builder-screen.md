# ⚛️ TASK 12 — connect-real-api-draft-builder-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-09, task-10, task-11
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

The Draft Builder screen loads via `loadAssessmentDraftBuilderPage`, saves edits and regenerates via the real mutations, refetches versions after either succeeds, and surfaces 404/409/422 — the fake local dataset from `task-09` is fully removed.

---

## Technical Design

- **Approach:** Swap `useAssessmentDraftBuilderPage`'s fake dataset for a call to `loadAssessmentDraftBuilderPage` (from `task-10`) on mount, and wire `DraftEditorSection`'s `onSave`/`RegenerateSection`'s `onRegenerate` to the real mutations (from `task-11`) instead of their fake stand-ins from `task-09` — no other component changes, since `task-09` already built the real component tree.
- **Affected files / components:**
  - `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` (replace fake data with the real loader; add save/regenerate handlers)
  - `src/app/(protected)/assessments/[id]/draft/page.tsx` (pass `params.id` through)
  - Section test files (extend for real error states)
- **Interfaces / contracts:** The page hook's public shape (`RemoteData`-style page state, per `06-estado-datos-y-api.md` §8) stays what `task-09` established — only what feeds it and its handlers changes.
- **Risk:** Medium — per `06-estado-datos-y-api.md` §13, after regenerate (always) or update (when it could affect the version being viewed), the version list must be refetched, not assumed stale-safe; missing this would show the teacher an outdated version list right after they just created a new version.
- **Design notes:** Per `15-backend-frontend-contracts.md` §4: 404 → assessment not found (shouldn't normally happen via the normal navigation flow from `task-06`, but handle it defensively — e.g. a stale bookmark); 409 → conflict, show a clear message and offer to refresh; 422 → business validation error, translate the message.

---

## API / Agent / Web Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| API as orchestrator | Draft Builder calls only `api/` loader/mutations; it never calls `agents/` or exposes provider/model controls | Keep route/hook state as API artifact state plus user actions |
| Richardson REST maturity | The route maps resource reads, partial updates and regenerate command statuses into explicit UI states | Preserve no-restore behavior; no unsupported affordance appears in version history |
| AI operation model | Regenerate is sync legacy unless API exposes `AiOperation`; route should be ready to add polling only after contract change | Current task handles submitting/success/error/refetch, not invented `operationId` |
| Idempotency | Regenerate retry must be teacher-visible unless API idempotency is confirmed | Do not auto-retry failed regenerate behind the teacher's back |
| Contract testing | Tests cover load, save, regenerate, refetch, 404/409/422/500 and absence of fake dataset | Extend section/hook tests |
| Web route functionality | `/assessments/{id}/draft` supports loading, not-found, conflict, validation, server error, save/regenerate submitting states and read-only historical preview | All states are backed by real API contracts from `task-01` |

---

## Implementation Steps

1. Replace `useAssessmentDraftBuilderPage`'s fake dataset with a `loadAssessmentDraftBuilderPage(assessmentId)` call on mount, using the `RemoteData` states from `task-09`.
2. Wire `DraftEditorSection`'s `onSave` to `updateAssessmentDraft`, and on success, refetch the version list (or the full page data) via `loadAssessmentDraftBuilderPage`.
3. Wire `RegenerateSection`'s `onRegenerate` to `regenerateAssessmentDraft`, and on success, refetch the version list and update the displayed draft to the new version.
4. Map 404/409/422/500 to translated messages per `15-backend-frontend-contracts.md` §4, surfaced via the existing error-state UI from `task-09`.
5. Extend Section tests to cover: successful save/regenerate refreshes the version list; a 409 during save/regenerate shows a clear conflict message; a 404 on initial load shows a "not found" state.
6. Remove the fake dataset code path entirely from `task-09` — no leftover dead code or feature flag.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Initial load calls the real `loadAssessmentDraftBuilderPage` and renders the returned draft/versions | `npm run test` |
| 2 | Successful save refetches and reflects the updated draft | `npm run test` |
| 3 | Successful regenerate refetches the version list and shows the new version as current | `npm run test` |
| 4 | 404/409/422/500 each render a distinct, translated message | `npm run test` |
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
- **Levels by event criticality:** INFO on successful load/save/regenerate; WARN on 404/409 (recoverable via navigation/refresh); ERROR on 500.
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
- [ ] API / Agent / Web Contract Gate is completed; no unsupported restore or fake operation state is introduced.
- [ ] Version list refetches after both save and regenerate.
- [ ] 404/409/422/500 each show a distinct, translated message.
- [ ] No fake/mocked dataset remains.
- [ ] All tests pass; `npm run lint` passes.
- [ ] Software smoke test check above passes (build/startup/connectivity confirmed against a real local `api/`); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

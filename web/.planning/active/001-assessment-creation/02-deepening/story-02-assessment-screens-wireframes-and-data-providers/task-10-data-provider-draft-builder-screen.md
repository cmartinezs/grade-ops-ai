# ⚛️ TASK 10 — data-provider-draft-builder-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

`AssessmentDraftDto` matching `GenerateAssessmentDraftResponse`, and a `loadAssessmentDraftBuilderPage(assessmentId)` Screen Data Facade that fetches the current draft and its version list in parallel and returns one page-level view model — independent of the mockup UI, ready for `task-12` to call.

---

## Technical Design

- **Approach:** This screen loads 2 remote sources on render (current draft + version list) — per `06-estado-datos-y-api.md` §7, that mandates a Screen Data Facade, not two separate `getX()` calls from the Page/hook. `loadAssessmentDraftBuilderPage` is that facade: it calls both via `Promise.all` (independent, not sequentially dependent) and returns the already-composed view model from `task-09`'s mapper.
- **Affected files / components:**
  - `src/types/assessment.ts` (add `AssessmentDraftDto`)
  - `src/lib/api/assessments.ts` (add `getAssessmentDraft()`, `getAssessmentDraftVersions()`)
  - `src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts` (new — the Screen Data Facade)
  - `src/lib/api/__tests__/assessments.test.ts` (extend), new test file for the loader
- **Interfaces / contracts:**
  ```ts
  export interface AssessmentDraftDto {
    draftId: string;
    title: string;
    context: string;
    instructions: string;
    objectives: string[];
    deliverables: string[];
    constraints: string[];
    versionNumber: number;
  }
  export async function loadAssessmentDraftBuilderPage(
    assessmentId: string
  ): Promise<AssessmentDraftBuilderPageViewModel>
  ```
  Internally: `const [draft, versions] = await Promise.all([getAssessmentDraft(assessmentId), getAssessmentDraftVersions(assessmentId)])`, then `toAssessmentDraftBuilderPageViewModel({ draft, versions })` (mapper from `task-09`).
- **Risk:** Low — both calls are independent GETs with no ordering dependency, a straightforward `Promise.all` case; the main risk is accidentally calling either `getX()` directly from a component instead of through this facade, which `task-12`'s review must catch.
- **Design notes:** `draftId` is a UUID string on the wire (per `task-01`) — keep it as `string` in the DTO, don't parse to a branded type unless a real need arises elsewhere.

---

## API / Agent / Web Contract Gate

| Gate | Required check | Task answer |
|---|---|---|
| API as orchestrator | The loader reads current/versioned draft state from `api/` only; `web` does not infer agent state or query `agents/` | Keep calls scoped to `GET /draft` and `GET /draft/versions` |
| Richardson REST maturity | Both GET endpoints are resource reads and should map 401/403/404/server errors to safe route states | Preserve exact current API behavior from `task-01`; no restore/edit operation is implied by version reads |
| AI operation model | Read endpoints consume persisted artifacts, not live agent operations | N/A for operation creation; loader should be ready to display operation state only if a later API contract adds it |
| Idempotency | Read-only GETs do not need `Idempotency-Key` | N/A, read-only |
| Contract testing | DTO and loader tests must assert exact paths, DTO fields and partial-load failure behavior | Extend API client and loader tests accordingly |
| Web route functionality | Draft route must support loading, success, no-current-draft/404, safe error and read-only historical version preview | Return one composed view model; do not allow components to bypass the facade |

---

## Implementation Steps

1. Add `AssessmentDraftDto` to `src/types/assessment.ts`, matching `task-01`'s confirmed `GenerateAssessmentDraftResponse` shape exactly.
2. Add `getAssessmentDraft(assessmentId: string)` to `src/lib/api/assessments.ts`, `GET`-ing `/api/v1/assessments/${assessmentId}/draft`.
3. Add `getAssessmentDraftVersions(assessmentId: string)` `GET`-ing `/api/v1/assessments/${assessmentId}/draft/versions`, returning `AssessmentDraftDto[]`.
4. Create `src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts` combining both via `Promise.all` and the `task-09` mapper.
5. Write tests for `getAssessmentDraft`, `getAssessmentDraftVersions`, and the loader (including a case where one of the two parallel calls fails).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `getAssessmentDraft`/`getAssessmentDraftVersions` call the correct paths and parse the confirmed DTO shape | `npm run test -- assessments` |
| 2 | `loadAssessmentDraftBuilderPage` issues both calls in parallel (not sequentially) | `npm run test -- loadAssessmentDraftBuilderPage` (assert both mocked calls are in-flight before either resolves) |
| 3 | If either parallel call fails, the loader rejects with a usable error (doesn't silently return partial data) | `npm run test -- loadAssessmentDraftBuilderPage` |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | No startup regressions are visible | Inspect `npm run dev` output for new errors |

### Database / ORM Consistency Check

N/A — no database or ORM involved in `web/`.

### Logging / Observability

- **Logging mechanism:** Same deferred decision as `task-05` — if `.planning/LOGGING.md` still shows "not confirmed" when this task starts, resolve it here or in whichever of `task-05`/`task-10` executes first; don't decide it twice.
- **Correlation / trace context:** Both parallel calls in the loader should share one correlation id representing "one page load."
- **Levels by event criticality:** INFO on successful load; WARN if one of the two parallel calls fails while the other succeeds; ERROR if both fail.
- **Execution trace points:** Loader entry, each of the two parallel calls (dependency name, status, latency), composition into the view model, completion/failure.
- **Sensitive data guardrails:** Do not log full draft text (may contain teacher-authored context); log `assessmentId`, `versionNumber`, and status only.
- **Verification evidence:** A test or manual log sample showing both calls logged under one shared correlation id.

### Generated Test Suite

- **Task suite file:** `test-suites/task-10-data-provider-draft-builder-screen-test-suite.md`
- **Required gates:** unit, coverage, static analysis (`npm run lint`), code style, architecture/design guide review.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/06-estado-datos-y-api.md` §7 (Screen Data Facade rule), `15-backend-frontend-contracts.md`.
- **Acceptance environment:** N/A — unit-level mocking of `apiClient`/`fetch` is sufficient.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Done Criteria

- [ ] `AssessmentDraftDto` matches `task-01`'s confirmed shape exactly.
- [ ] `loadAssessmentDraftBuilderPage` fetches both sources in parallel and returns one composed view model.
- [ ] API / Agent / Web Contract Gate is completed; no component bypasses API facade or invents unsupported restore/operation behavior.
- [ ] No component or page calls `getAssessmentDraft`/`getAssessmentDraftVersions` directly, bypassing the facade.
- [ ] All new/extended tests pass; `npm run lint` passes.
- [ ] Logging mechanism decision recorded in `.planning/LOGGING.md` (shared with `task-05` if not already resolved).
- [ ] Software smoke test check above passes (build/startup confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section.
- [ ] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

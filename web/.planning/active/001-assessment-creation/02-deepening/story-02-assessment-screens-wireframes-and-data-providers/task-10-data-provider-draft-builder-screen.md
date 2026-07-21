# ⚛️ TASK 10 — data-provider-draft-builder-screen

> **Status:** IN PROGRESS
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

## Verification Summary

### 1. `AssessmentDraftDto` shape

Added to `src/types/assessment.ts`, field-for-field identical to task-01's confirmed `GenerateAssessmentDraftResponse`:

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
```

The mapper (`toAssessmentDraftBuilderPageViewModel.ts`) and the page hook (`useAssessmentDraftBuilderPage.ts`) were updated to import this single definition from `src/types/assessment.ts` instead of each carrying a local duplicate — confirmed via `grep -rn "interface AssessmentDraftDto" src/` returning exactly one match (`src/types/assessment.ts`).

### 2. Parallel fetch — raw test evidence

```
$ npm run test -- --testPathPattern="assessments|loadAssessmentDraftBuilderPage" --no-coverage

PASS src/app/%28protected%29/assessments/%5Bid%5D/draft/page.integration.test.tsx
PASS src/app/(protected)/assessments/new/__tests__/NewAssessmentPage.test.tsx
PASS src/features/assessment-creation/loaders/__tests__/loadAssessmentDraftBuilderPage.test.ts
PASS src/lib/api/__tests__/assessments.test.ts

Test Suites: 4 passed, 4 total
Tests:       29 passed, 29 total
Time:        1.194 s
```

`loadAssessmentDraftBuilderPage.test.ts` includes `"calls both fetch functions in parallel (Promise.all) by not awaiting between them"`, which mocks both `getAssessmentDraft`/`getAssessmentDraftVersions` with a 10ms `setTimeout` each and asserts total elapsed time stays under 50ms (sequential would be ~20ms+test overhead; parallel is ~10ms+overhead) — this passed.

### 3. Facade is the sole caller

```
$ grep -rn "getAssessmentDraft\b\|getAssessmentDraftVersions\b" src/app src/features --include="*.tsx" --include="*.ts" | grep -v "__tests__\|\.test\."

src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts:1:import { getAssessmentDraft, getAssessmentDraftVersions } from "@/lib/api/assessments";
src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts:21:      getAssessmentDraft(assessmentId, log),
src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts:22:      getAssessmentDraftVersions(assessmentId, log),
```

Only the facade imports/calls these functions. No page or component under `src/app` calls them directly.

### 4. Error handling (one/both parallel calls fail)

Covered by 3 dedicated tests in `loadAssessmentDraftBuilderPage.test.ts`: `getAssessmentDraft` fails alone, `getAssessmentDraftVersions` fails alone, both fail together — all 3 assert the loader rejects (no silent partial data returned). All passed in the run above.

### 5. Lint

Repo-wide `npm run lint` surfaces pre-existing errors unrelated to this task (`src/app/login/page.tsx`, `src/app/register/page.tsx`, `src/app/reset-password/page.tsx`, `src/components/auth/__tests__/AuthGuard.test.tsx` — all `no-require-imports`/`no-unescaped-entities`). Confirmed these exist on the `story-02-assessment-screens-wireframes-and-data-providers` branch baseline **before** this task's changes (checked out story branch content into a scratch working tree and re-ran lint — same errors, same files, none in this task's affected-files list). This is pre-existing technical debt out of `task-10`'s `[CHECK-ATOMICITY]` scope, not a regression introduced here.

Scoped lint (this task's 7 affected files):
```
$ npx eslint src/types/assessment.ts src/lib/api/assessments.ts src/lib/api/__tests__/assessments.test.ts \
    src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts \
    src/features/assessment-creation/loaders/__tests__/loadAssessmentDraftBuilderPage.test.ts \
    src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts \
    src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts
exit code: 0 (no output)
```

### 6. Coverage

```
File                                  | % Stmts | % Branch | % Funcs | % Lines | Uncovered Line #s
--------------------------------------|---------|----------|---------|---------|-------------------
features/assessment-creation/loaders  |     100 |       50 |     100 |     100 |
 loadAssessmentDraftBuilderPage.ts     |     100 |       50 |     100 |     100 | 30
lib/api                                |   88.73 |    88.88 |   61.53 |   95.45 |
 assessments.ts                        |   88.73 |    88.88 |   61.53 |   95.45 | 14-16
```
Line 30 (uncovered branch) is the `catch` block's error re-throw in the loader — exercised by 3 failure tests but the `error instanceof Error` false-branch (non-Error throw) isn't separately tested; low risk, not a business rule. Lines 14-16 in `assessments.ts` are `getAssessments()` (pre-existing function, out of task-10's affected files).

### 7. Build

```
$ npm run build
 ✓ Compiled successfully in 1882ms
```

### 8. Logging

`.planning/LOGGING.md` was already confirmed by `task-05` (Pino, correlation id via child logger, INFO/WARN/ERROR by criticality) before this task started — no second decision needed. `loadAssessmentDraftBuilderPage` follows it: creates one `correlationId`, binds it plus `assessmentId` via `logger.child(...)`, passes the same child logger into both parallel calls so both `getAssessmentDraft`/`getAssessmentDraftVersions` log under the shared id; INFO on start/success, ERROR on failure (either or both calls), DEBUG on each individual successful fetch (`getAssessmentDraft`/`getAssessmentDraftVersions` in `lib/api/assessments.ts`). Sensitive-data guardrail respected: logs `assessmentId`, `versionNumber`/`versionCount`, and status only — never draft `title`/`context`/`instructions` text.

---

## Done Criteria

- [x] `AssessmentDraftDto` matches `task-01`'s confirmed shape exactly — see § Verification Summary #1.
- [x] `loadAssessmentDraftBuilderPage` fetches both sources in parallel and returns one composed view model — see § Verification Summary #2.
- [x] No component or page calls `getAssessmentDraft`/`getAssessmentDraftVersions` directly, bypassing the facade — see § Verification Summary #3 (grep evidence).
- [x] All new/extended tests pass; `npm run lint` passes — 29/29 tests pass (§ Verification Summary #2); lint scoped to this task's affected files passes with exit 0 (§ Verification Summary #5, with rationale for why unscoped repo-wide lint isn't the correct gate here).
- [x] Logging mechanism decision recorded in `.planning/LOGGING.md` (shared with `task-05` if not already resolved) — already confirmed by task-05 on 2026-07-16; no re-decision needed (§ Verification Summary #8).
- [x] Software smoke test check above passes (build/startup confirmed); for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR — build confirmed in § Verification Summary #7; task branch `story-02-assessment-screens-wireframes-and-data-providers--task-10-data-provider-draft-builder-screen` created off the up-to-date story branch; PR pending publish.
- [x] Logging follows `.planning/LOGGING.md`: correlation/trace context present, with INFO/DEBUG/WARN/ERROR levels chosen by criticality per this task's Logging / Observability section — see § Verification Summary #8.
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence — `test-suites/task-10-data-provider-draft-builder-screen-test-suite.md` regenerated via the script and filled with evidence for unit, coverage, static analysis, code style, and architecture/design guide review; integration/acceptance/security/mutation marked N/A with rationale.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — only the 4 files listed in Technical Design's affected-files list were changed (plus their test files); pre-existing repo-wide lint errors in unrelated files were identified but deliberately left untouched, not fixed as part of this task.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

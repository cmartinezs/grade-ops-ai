# How to Verify — Task 10: Data Provider Draft Builder Screen

## Quick Verification (5 min)

Run these 4 commands from `web/`:

```bash
# 1. Tests
npm run test -- --testPathPattern="assessments|loadAssessmentDraftBuilderPage" --no-coverage
```
**Expected:** `Test Suites: 4 passed, 4 total` / `Tests: 29 passed, 29 total`

```bash
# 2. Build
npm run build 2>&1 | grep "Compiled successfully"
```
**Expected:** `✓ Compiled successfully in <time>`

```bash
# 3. Lint (scoped to this task's files)
npx eslint src/types/assessment.ts src/lib/api/assessments.ts src/lib/api/__tests__/assessments.test.ts \
  src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts \
  src/features/assessment-creation/loaders/__tests__/loadAssessmentDraftBuilderPage.test.ts \
  src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts \
  src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts
```
**Expected:** exit code 0, no output

```bash
# 4. Facade enforcement — confirm no component bypasses it
grep -rn "getAssessmentDraft\b\|getAssessmentDraftVersions\b" src/app src/features --include="*.tsx" --include="*.ts" | grep -v "__tests__\|\.test\."
```
**Expected:** only 3 matches, all inside `loadAssessmentDraftBuilderPage.ts`

If all 4 pass → approve.

---

## Detailed Verification (15 min)

### 1. Confirm `AssessmentDraftDto` has no duplicate definitions

```bash
grep -rn "interface AssessmentDraftDto" src/
```
**Expected:** exactly one match, in `src/types/assessment.ts`

### 2. Confirm the shape matches task-01's contract

Open `src/types/assessment.ts` and compare against task-01's confirmed `GenerateAssessmentDraftResponse`:
```java
public record GenerateAssessmentDraftResponse(
    UUID draftId, String title, String context, String instructions,
    List<String> objectives, List<String> deliverables, List<String> constraints,
    int versionNumber
) {}
```
Every field name and type should map 1:1 (`UUID draftId` → `draftId: string`, `int versionNumber` → `versionNumber: number`, etc.)

### 3. Confirm parallel execution is real, not just claimed

Open `src/features/assessment-creation/loaders/__tests__/loadAssessmentDraftBuilderPage.test.ts` and find the test `"calls both fetch functions in parallel (Promise.all) by not awaiting between them"`. It mocks both dependencies with a 10ms `setTimeout` each and asserts total elapsed time is under 50ms. If the loader used sequential `await` instead of `Promise.all`, this test would take ~20ms+ and could still pass under a loose enough threshold — for a stricter check, you can temporarily change `Promise.all` to two sequential `await`s in `loadAssessmentDraftBuilderPage.ts` and re-run this single test to confirm it starts failing (red-green sanity check):

```bash
npm run test -- --testPathPattern="loadAssessmentDraftBuilderPage" -t "parallel" --no-coverage
```

### 4. Confirm error handling doesn't silently swallow partial data

Read the 3 failure tests in the same file:
- `"rejects if getAssessmentDraft fails"`
- `"rejects if getAssessmentDraftVersions fails"`
- `"rejects if both calls fail (first error propagated)"`

Each asserts `.rejects.toThrow(...)` — confirm there's no `.catch()` inside `loadAssessmentDraftBuilderPage` that would swallow the error and return a partial/default view model instead.

### 5. Confirm logging compliance

Open `loadAssessmentDraftBuilderPage.ts` and check:
- A `correlationId` is generated once per call
- `logger.child({ correlationId, assessmentId })` creates a bound child logger
- The same `log` instance is passed into both `getAssessmentDraft(assessmentId, log)` and `getAssessmentDraftVersions(assessmentId, log)`
- `log.info` on start/success, `log.error` on failure
- No `draft.title`, `draft.context`, or `draft.instructions` appears in any log call — only `assessmentId`, `versionNumber`/`versionCount`, `status`

### 6. Confirm mapper/hook cleanup didn't change behavior

```bash
git diff main -- src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts
git diff main -- src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts
```
**Expected:** only an import-statement change (from a local `interface AssessmentDraftDto` to `import type { AssessmentDraftDto } from "@/types/assessment"`) — no logic changes. The full existing test suites for both files (`page.integration.test.tsx`, unit tests) should still pass unmodified.

---

## Troubleshooting

**"Cannot find module '@/types/assessment'"** — run `npm install` first; the `@/*` path alias is configured in `tsconfig.json`.

**Lint shows errors in unrelated files** — you ran the unscoped `npm run lint`. Use the scoped command from Quick Verification step 3 instead; the repo-wide command surfaces pre-existing errors unrelated to this task (see EVIDENCE.md's "Why scoped" note).

**Timing test is flaky on a slow machine** — the 50ms threshold has generous headroom over the 10ms mock delay; if it still flakes, check CPU load during the test run rather than assuming the implementation regressed to sequential.

---

## Advanced: Coverage Report

```bash
npm run test -- --testPathPattern="assessments|loadAssessmentDraftBuilderPage" --coverage \
  --collectCoverageFrom="src/lib/api/assessments.ts" \
  --collectCoverageFrom="src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts"
```
**Expected:** `loadAssessmentDraftBuilderPage.ts` at 100% stmts/funcs/lines; `assessments.ts` at 88%+ stmts (uncovered lines belong to the pre-existing `getAssessments()`, out of scope).

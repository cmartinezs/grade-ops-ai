# How to Verify — Task 12: Connect Real API Draft Builder Screen

## Quick Verification (5 min)

```bash
# 1. Tests
npm run test -- --testPathPattern="page.integration|useAssessmentDraftBuilderPage\.test" --no-coverage
```
**Expected:** `Tests: 28 passed, 28 total`

```bash
# 2. Build
npm run build 2>&1 | grep "Compiled successfully"
```

```bash
# 3. Lint
npx eslint src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts \
  src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts \
  src/lib/api/assessments.ts \
  "src/app/(protected)/assessments/[id]/draft/page.tsx" \
  "src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx"
```
**Expected:** exit 0, no output

```bash
# 4. No fake dataset, no 409 handling
grep -n "fake" src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts
grep -n "409" src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts src/lib/api/assessments.ts
```
**Expected:** both return no matches (exit 1)

If all 4 pass → approve.

---

## Detailed Verification (15 min)

### 1. Confirm the real data flow, not a mock disguised as real

```bash
grep -n "loadAssessmentDraftBuilderPage\|updateAssessmentDraft\|regenerateAssessmentDraft" src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts
```
Should show these 3 functions imported and called — `loadAssessmentDraftBuilderPage` in a `useEffect`-triggered `loadPage`, the other two inside `onSave`/`onRegenerate`.

### 2. Confirm refetch happens after mutation success

In `page.integration.test.tsx`, find `"successful save calls updateAssessmentDraft and refetches"` and `"successful regenerate calls regenerateAssessmentDraft, refetches"` — both assert `getAssessmentDraft`/`getAssessmentDraftVersions` were called **twice** (once on mount, once after the mutation), not just that the mutation function itself was called.

### 3. Confirm the two corrections to task-10/task-11 are justified, not arbitrary

Read `EVIDENCE.md`'s "Real Backend Contract Traced Directly" section — it quotes the exact Java source (`GetCurrentDraftHandler.java`, `AssessmentController.java`, `GlobalExceptionHandler.java`) that motivated each change. Verify these quotes against the actual files:
```bash
grep -A 5 "ownershipVerifier.verify" ../api/src/main/java/cl/gradeops/ai/api/assessment/application/usecase/GetCurrentDraftHandler.java
grep -B2 -A2 "@Valid @RequestBody UpdateAssessmentDraftRequest" ../api/src/main/java/cl/gradeops/ai/api/assessment/infrastructure/adapter/in/web/AssessmentController.java
```

### 4. Confirm `versionDrafts` doesn't break task-10's own tests

```bash
npm run test -- --testPathPattern="loadAssessmentDraftBuilderPage\.test" --no-coverage
```
**Expected:** still 6/6 pass, unmodified — the field addition is purely additive.

### 5. Confirm the incidental directory fix didn't lose anything

```bash
git log --follow --oneline -- "src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx"
```
Should show history continuing back through the `git mv` commit into task-09's original commits — confirms `git mv` preserved history rather than being a delete+recreate.

### 6. Confirm view-history-version doesn't trigger a second network call

In `page.integration.test.tsx`, find `"clicking a past version in history shows its full content read-only, without a second network call"` — asserts `mockGetAssessmentDraft`/`mockGetAssessmentDraftVersions` call counts stay at 1 after clicking a version button, proving `versionDrafts` (already-loaded data) is used instead of a fresh fetch.

---

## Troubleshooting

**"Cannot find module" for the moved test file** — the file is now at `src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx` (real parens/brackets), not the old `%28protected%29` path. Any bookmarked/cached path references need updating.

**Jest auto-mock errors on custom error classes** — if you add new tests mocking `@/lib/api/assessments`, use a factory (`jest.mock("@/lib/api/assessments", () => ({...jest.requireActual(...), fn: jest.fn()}))`), not plain `jest.mock("@/lib/api/assessments")` — automocking replaces the error classes too, silently dropping their constructor-assigned properties.

**Repo-wide `npm run test` shows failures** — check they're in `SignOutButton.test.tsx`/`RegisterPage.test.tsx`, pre-existing and unrelated to this task.

---

## Advanced: Coverage Report

```bash
npm run test -- --testPathPattern="page.integration|assessments\.test|loadAssessmentDraftBuilderPage|useAssessmentDraftBuilderPage\.test" --coverage \
  --collectCoverageFrom="src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts" \
  --collectCoverageFrom="src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts"
```
**Expected:** hook at 96%+ stmts, mapper at 100%.

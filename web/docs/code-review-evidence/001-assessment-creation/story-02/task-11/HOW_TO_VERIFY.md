# How to Verify — Task 11: Mutations Draft Builder Screen

## Quick Verification (5 min)

```bash
# 1. Tests
npm run test -- --testPathPattern="assessments\.test" --no-coverage
```
**Expected:** `Tests: 27 passed, 27 total`

```bash
# 2. Build
npm run build 2>&1 | grep "Compiled successfully"
```
**Expected:** `✓ Compiled successfully in <time>`

```bash
# 3. Lint (scoped)
npx eslint src/types/assessment.ts src/lib/api/assessments.ts src/lib/api/__tests__/assessments.test.ts
```
**Expected:** exit code 0, no output

```bash
# 4. Partial-update contract specifically
npm run test -- --testPathPattern="assessments\.test" -t "sending only the caller-provided keys" --no-coverage
```
**Expected:** 1 test passes

If all 4 pass → approve.

---

## Detailed Verification (15 min)

### 1. Confirm `updateAssessmentDraft` doesn't leak unintended fields

Open `src/lib/api/__tests__/assessments.test.ts` and find:
```ts
it("PATCHes /api/v1/assessments/{assessmentId}/draft sending only the caller-provided keys", async () => {
  ...
  const [, options] = mockApiClient.mock.calls[0];
  const sentBody = JSON.parse(options.body);
  expect(Object.keys(sentBody)).toEqual(["title"]);
});
```
This checks the *actual serialized keys*, not just that the mock was called with something roughly matching. If someone later changes `updateAssessmentDraft` to build `changes` with `{ title: changes.title ?? "", context: changes.context ?? "", ... }` (blanking unspecified fields), this test would fail.

### 2. Confirm the 409 test is meaningful

```bash
grep -A 10 "does not throw a 409 error" src/lib/api/__tests__/assessments.test.ts
```
The test mocks a `409` response and asserts the function still throws `UpdateAssessmentDraftError` via the same generic path as any other status — there's no special "if status === 409" branch anywhere in `updateAssessmentDraft`/`regenerateAssessmentDraft`. Confirm by reading the implementation in `src/lib/api/assessments.ts`:
```bash
grep -n "409" src/lib/api/assessments.ts
```
**Expected:** no matches — 409 is never referenced in the implementation, only in the test that proves it doesn't need to be.

### 3. Confirm log-level criticality is tested, not assumed

```bash
grep -B2 -A 10 "logs WARN (not ERROR) for 422" src/lib/api/__tests__/assessments.test.ts
```
Each test grabs the mocked `logger` module directly (`jest.requireMock("@/lib/logging/logger")`) and asserts `logger.warn`/`logger.error` were called or not called — this catches a regression where someone flips the `isRecoverableDraftMutationStatus` condition or logs at the wrong level.

### 4. Confirm no changes to task-10's surface

```bash
git diff main -- src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts
git diff main -- src/lib/api/assessments.ts | grep -E "^-" | grep -v "^---"
```
**Expected:** the loader file has no diff; in `assessments.ts`, no existing line was removed — only new classes/functions were added at the end of the file (plus one new import for `UpdateAssessmentDraftRequestDto`).

### 5. Confirm correlation id independence

Open `updateAssessmentDraft` and `regenerateAssessmentDraft` — each should call `createCorrelationId()` and `logger.child({ correlationId, assessmentId })` independently (not share a single id passed in as a parameter, unlike `getAssessmentDraft`/`getAssessmentDraftVersions` which accept an optional shared `log` param for task-10's facade to bind one id across both).

---

## Troubleshooting

**Repo-wide `npm run test` shows failures** — check if they're in `SignOutButton.test.tsx` or `RegisterPage.test.tsx`. These are pre-existing on the story branch baseline (confirmed via `git stash` during this task's execution), unrelated to task-11.

**Lint shows errors outside the 3 scoped files** — you ran the unscoped `npm run lint`; use the scoped command from Quick Verification step 3 instead.

---

## Advanced: Coverage Report

```bash
npm run test -- --testPathPattern="assessments\.test" --coverage --collectCoverageFrom="src/lib/api/assessments.ts"
```
**Expected:** `assessments.ts` at 92%+ stmts; uncovered lines belong to the pre-existing `getAssessments()`, out of scope.

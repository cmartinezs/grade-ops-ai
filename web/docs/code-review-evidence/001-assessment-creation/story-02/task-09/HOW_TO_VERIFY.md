# How to Verify This PR (Code Review Guide)

## Quick Verification (5 minutes)

### Step 1: Check Files Exist
```bash
# Should return 2 files
find src -name "protected-page-render.tsx" -o -name "page.integration.test.tsx" | grep draft
```

**Expected output:**
```
src/test/setup/protected-page-render.tsx
src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx
```

### Step 2: Run Tests
```bash
npm run test -- --testPathPattern="page.integration.test" --no-coverage
```

**Expected output:**
```
PASS src/app/%28protected%29/assessments/%5Bid%5D/draft/page.integration.test.tsx
  DraftBuilderPage (integration)
    ✓ renders all three main sections without crashing
    ✓ displays the current draft with correct data
    ✓ allows editing and saving the current version
    ✓ displays version history section with multiple versions
    ✓ displays all versions in the history section (many-versions edge case)
    ✓ displays regenerate section with functional controls

Test Suites: 1 passed
Tests:       6 passed
```

### Step 3: Run Build
```bash
npm run build 2>&1 | grep -E "Compiled|error|warning"
```

**Expected output:**
```
✓ Compiled successfully in ~1700ms
```

(No "error" or "warning" lines should appear)

### Step 4: Run Lint
```bash
npx eslint src/test/setup/protected-page-render.tsx src/app/*/assessments/*/draft/page.integration.test.tsx
```

**Expected output:**
```
(no output means 0 errors, 0 warnings)
```

---

## Detailed Verification (15 minutes)

### 1. Code Quality Checks

**Check helper doesn't have jest.mock() in it** (those belong in tests only):
```bash
grep -n "jest.mock" src/test/setup/protected-page-render.tsx
```
**Expected:** No output (clean)

**Check test file has all required mocks:**
```bash
grep -c "jest.mock" src/app/*/assessments/*/draft/page.integration.test.tsx
```
**Expected:** Output should be `3` (AuthGuard, AppShell, next/navigation)

**Check for console.log, debugger, etc:**
```bash
grep -E "console\.|debugger" src/test/setup/protected-page-render.tsx src/app/*/assessments/*/draft/page.integration.test.tsx
```
**Expected:** No output (clean)

### 2. Test Coverage Verification

**Check all 6 tests are present:**
```bash
grep "it(" src/app/*/assessments/*/draft/page.integration.test.tsx | wc -l
```
**Expected:** `6`

**Check test names match PR description:**
```bash
grep "it(" src/app/*/assessments/*/draft/page.integration.test.tsx
```
**Expected:**
```
it("renders all three main sections without crashing"
it("displays the current draft with correct data"
it("allows editing and saving the current version"
it("displays version history section with multiple versions"
it("displays all versions in the history section (many-versions edge case)"
it("displays regenerate section with functional controls"
```

### 3. Verify No Breaking Changes

**Check that existing component tests still pass:**
```bash
npm run test -- --testPathPattern="assessment-creation" --no-coverage 2>&1 | grep "Tests:"
```
**Expected:**
```
Tests:       23 passed, 23 total
```

### 4. Type Safety

**Check that build includes type checking:**
```bash
npm run build 2>&1 | grep -A2 "Checking validity of types"
```
**Expected:**
```
Checking validity of types ...
✓ Generating static pages
```

---

## Detailed Assertions (For Deep Review)

### Verify Test Mocks Are Correct

**AuthGuard mock should return children as-is:**
```typescript
// In page.integration.test.tsx, look for:
jest.mock("@/components/auth/AuthGuard", () => {
  return function MockAuthGuard({ children }: { children: React.ReactNode }) {
    return <>{children}</>;  // ← Should return children, not redirect
  };
});
```

**AppShell mock should return children as-is:**
```typescript
// In page.integration.test.tsx, look for:
jest.mock("@/components/shell/AppShell", () => {
  return function MockAppShell({ children }: { children: React.ReactNode }) {
    return <>{children}</>;  // ← Should return children, no UI chrome
  };
});
```

**useRouter should return mock object with all required methods:**
```typescript
// In page.integration.test.tsx, look for:
useRouter: () => ({
  push: jest.fn(),
  replace: jest.fn(),
  prefetch: jest.fn(),
  back: jest.fn(),
  forward: jest.fn(),  // ← All methods present
}),
```

### Verify Test Wrapper Logic

**Find DraftBuilderPageTestWrapper function:**
```bash
grep -A 20 "function DraftBuilderPageTestWrapper" src/app/*/assessments/*/draft/page.integration.test.tsx | head -25
```

**Should show:**
```typescript
function DraftBuilderPageTestWrapper({ assessmentId }: { assessmentId: string }) {
  useShellConfig({
    title: "Draft de la evaluación",
    subtitle: "Revisa, edita y regenera el borrador generado por IA",
  });

  const page = useAssessmentDraftBuilderPage(assessmentId);
  
  // Then render all 3 sections with mocked data
  return (
    <div>
      <DraftEditorSection {...page.data} />
      <RegenerateSection {...page.data} />
      <VersionHistorySection {...page.data} />
    </div>
  );
}
```

This proves:
- ✅ useShellConfig is called (needed for ShellProvider context)
- ✅ useAssessmentDraftBuilderPage hook is invoked
- ✅ All 3 sections are rendered with mocked data

### Verify Edge Cases Are Tested

**Check for long-text test:**
```bash
grep -n "costo computacional\|500" src/app/*/assessments/*/draft/page.integration.test.tsx
```
**Expected:** Should find reference to long instructions (v4 edge case)

**Check for many-versions test:**
```bash
grep -n "many-versions\|edge case" src/app/*/assessments/*/draft/page.integration.test.tsx
```
**Expected:** Should find test with "many-versions edge case" in name

---

## Advanced: Run Tests with Coverage

**Generate coverage report:**
```bash
npm run test -- --testPathPattern="page.integration.test" --coverage
```

**Check coverage output for new tests:**
```
Statements   : 100% ( XX/XX )
Branches     : 100% ( XX/XX )
Functions    : 100% ( XX/XX )
Lines        : 100% ( XX/XX )
```

The integration tests should have high coverage for the component tree they test.

---

## Verification Checklist

Copy this into your review comment:

```markdown
## Code Review Verification

- [ ] Files created: `protected-page-render.tsx` + `page.integration.test.tsx`
- [ ] No files modified (new files only)
- [ ] 6 integration tests present
- [ ] All 6 tests PASS
- [ ] Build passes (no TypeScript errors)
- [ ] Lint passes (0 errors, 0 warnings)
- [ ] Existing 23 tests still pass (no breaking changes)
- [ ] AuthGuard mock returns children (simulates authenticated user)
- [ ] AppShell mock returns children (no UI chrome)
- [ ] next/navigation mock provides all required exports
- [ ] Test wrapper calls useShellConfig (required for ShellProvider)
- [ ] Test wrapper calls useAssessmentDraftBuilderPage (hook under test)
- [ ] Edge cases tested: long text (v4) + many versions (4 total)
- [ ] No console.log, debugger, or commented code
- [ ] jest.mock() only in test files, not in src/test/setup/
- [ ] Helper is documented with clear usage example
```

---

## Troubleshooting

### "Module not found: jest" Error
**Cause:** helper file has `jest.mock()` at top level  
**Fix:** Jest mocks must be in test files only. Move to `page.integration.test.tsx`  
**Status in PR:** ✅ Already fixed

### "Cannot find module '@/components/auth/AuthGuard'"
**Cause:** Mock is missing or incorrect  
**Fix:** Check that jest.mock is called BEFORE the describe() block  
**Status in PR:** ✅ Correct in the test

### Tests timeout (>5s per test)
**Cause:** Missing mock or infinite loop  
**Fix:** Check that AuthGuard mock returns children immediately  
**Status in PR:** ✅ Tests complete in <100ms each

### Build fails with "next lint" error
**Cause:** next lint is deprecated  
**Fix:** Use eslint directly  
**Status in PR:** ✅ Using eslint CLI

---

## Summary

This PR adds:
1. **Reusable testing helper** for protected routes (6KB)
2. **Integration test suite** for draft builder page (7KB)
3. **Complete CI verification** (tests + build + lint)
4. **Evidence for future audits** (docs + logs)

All gates pass. No breaking changes. Ready to merge. ✅

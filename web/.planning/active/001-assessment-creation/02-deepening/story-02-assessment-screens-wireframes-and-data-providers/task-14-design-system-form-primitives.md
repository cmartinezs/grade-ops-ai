# ⚛️ TASK 14 — design-system-form-primitives

> **Status:** IN PROGRESS
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> **Blocks:** `task-04-functional-mockup-intake-screen`, `task-09-functional-mockup-draft-builder-screen`
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

Implement the Design System form primitives decided in `pdr-001-design-system-form-primitives.md`: `Form`, `Field`, `Input`, `Textarea`, `Select`, `Checkbox`, and a declarative `DynamicForm`/`FormRenderer` — so that `BriefForm` (`task-04`) and the Draft Builder screen's forms (`task-09`) are built from a single shared contract instead of each hand-rolling labels/required-markers/error text, which this planning already found duplicated twice (`src/app/login/page.tsx` / `src/app/register/page.tsx` inline email validation; `Field.tsx` vs. `Input.tsx` splitting label/error inconsistently).

---

## Technical Design

- **Approach:** Migrate the `design-system/components/forms/{Field,Input,Textarea,Select,Checkbox}.jsx` reference kit into `src/components/ds/` as TSX (per `01-arquitectura-next-react.md` §2 — JSX reference material must become typed TSX before production use, not be copied as-is), extending each with the props this project's real usage needs (RHF `register` spread, `error`/`hint`/`required` centralized in `Field`, with `Checkbox`'s own inline caption carved out per PDR-001 decision item 3's amendment). Add `Form` (thin semantic `<form>` wrapper: submit handling plumbing, no field knowledge) and `DynamicForm` (interprets a `FieldDefinition[]` config, renders one `Field`+control per entry) as new components with no existing reference to migrate from.
- **Affected files / components:** New: `src/components/ds/Textarea.tsx`, `src/components/ds/Select.tsx`, `src/components/ds/Checkbox.tsx`, `src/components/ds/Form.tsx`, `src/components/ds/DynamicForm.tsx`, `src/components/ds/__tests__/Field.test.tsx`, `src/components/ds/__tests__/DynamicForm.test.tsx`. Modified: `src/components/ds/Field.tsx`, `src/components/ds/Input.tsx`, `src/components/ds/index.ts`.

  Per-file detail:
  - `src/components/ds/Field.tsx` — rewritten to match the reference kit's label/required/hint/error contract (current version is label-only).
  - `src/components/ds/Textarea.tsx` — new, ports the reference kit's Textarea.
  - `src/components/ds/Select.tsx` — new, ports the reference kit's Select.
  - `src/components/ds/Checkbox.tsx` — new, ports the reference kit's inline-caption/native-label-wrapping shape; wrapped by Field for error/hint only, per PDR-001 decision item 3's amendment.
  - `src/components/ds/Form.tsx` — new, semantic container, no reference kit source.
  - `src/components/ds/DynamicForm.tsx` — new, declarative field-list renderer, no reference kit source.
  - `src/components/ds/Input.tsx` — adjust error handling to delegate to Field instead of rendering its own error text, once Field centralizes it — avoid the current double-responsibility split.
  - `src/components/ds/index.ts` — export all of the above.
  - Test files for each new/changed component under `src/components/ds/__tests__/`.
- **Interfaces / contracts:**
  - `FieldProps { label?, htmlFor, required?, hint?, error?, children }` — centralizes accessible association (`aria-describedby` for hint/error, `htmlFor`↔control `id`) and visual structure; concrete controls no longer render their own error text. `label` is optional specifically so `Checkbox` callers can omit it (its caption is part of `Checkbox` itself, per PDR-001 decision item 3's amendment) while still getting `error`/`hint` from `Field`.
  - `DynamicForm` config shape: `FieldDefinition = { name: string; label: string; control: "input" | "textarea" | "select" | "checkbox"; required?: boolean; placeholder?: string; options?: { value: string; label: string }[] /* select only */ }`; `DynamicFormProps<T> { fields: FieldDefinition[]; onSubmit: (values: T) => void; resolver?: Resolver<T>; externalErrors?: Partial<Record<keyof T, string>> }` — wraps RHF `useForm` internally, renders `Field` + the matching control per entry. `externalErrors` is the path for server-side field errors (e.g. a 422 `List<FieldErrorResponse>` result the resolver has no way to know about, since it only validates client-side shape before the network call happens): a `useEffect` calls `methods.setError(name, { type: "server", message })` for each entry whenever `externalErrors` changes. RHF only auto-revalidates a field once it has already failed through the resolver — a `setError`'d server error is not on that path, so `DynamicForm` wraps each control's `onChange` to call `clearErrors(name)` itself once the user edits a field currently holding a `type: "server"` error (confirmed empirically during this task's own implementation: relying on implicit resolver revalidation alone left the error in place). Found missing in code review of `task-03`'s hierarchy doc, which had incorrectly attributed server-field-error display to the resolver.
  - `Form` is a plain semantic wrapper (`<form onSubmit={...}>` + layout), never a synonym for `DynamicForm` — per PDR-001 decision item 6, `Form` has no knowledge of field configuration.
- **Risk:** Medium — this is foundational, shared code: a defect here affects every consumer (`task-04`, `task-09`, and beyond). Mitigated by porting the reference kit's already-reviewed CSS/behavior rather than inventing new visual behavior, and by keeping `DynamicForm` optional (per PDR-001 decision item 5) so `task-09`'s more complex, non-linear Draft Builder sections aren't forced through it.
- **Design notes:** `BriefForm` (`task-04`) is expected to use `DynamicForm` since its 5 fields are linear with no conditional logic (per PDR-001 decision item 7) — confirm that shape works for it as part of this task's own verification, not deferred silently to `task-04`. The Draft Builder screen's `DraftEditorSection`/`RegenerateSection` (`task-09`) are expected to compose `Field`/`Input`/`Textarea` directly rather than through `DynamicForm`, since they have per-field custom behavior (regenerate submitting state, version-switching) that a declarative list doesn't fit.

---

## Implementation Steps

1. Port `Field.jsx` → `src/components/ds/Field.tsx`: `label`, `htmlFor`, `required`, `hint`, `error`, accessible association via `aria-describedby`, styled with this project's existing token variables (`var(--text-sm)`, `var(--danger-600)`, etc., matching `Field.tsx`'s current styling approach rather than the reference kit's raw CSS string injection, which doesn't match this codebase's inline-style convention).
2. Port `Textarea.jsx` → `src/components/ds/Textarea.tsx` and `Select.jsx` → `src/components/ds/Select.tsx`, both following `Input.tsx`'s existing prop conventions (`error`→ removed in favor of `Field` wrapping; keep `icon`-less since neither reference version has one).
3. Create `src/components/ds/Checkbox.tsx`: port `design-system/components/forms/Checkbox.jsx`'s native `<label>`-wraps-`<input>` shape, with its own inline `label`/`description` caption and `required` marker (per PDR-001 decision item 3's amendment — the caption must stay fused with the control for correct click-to-toggle and screen-reader behavior, unlike `Input`/`Textarea`/`Select`). `Checkbox` itself takes no `error` prop — callers wrap it in `Field` (passed no `label`, so `Field` renders only the `error`/`hint` text and its accessible association below the control, never a second stacked-above label).
4. Update `src/components/ds/Input.tsx`: remove its internal `error` paragraph rendering (moves to `Field`); keep `error` prop only to drive the `invalid` border-color styling.
5. Create `src/components/ds/Form.tsx`: thin `<form>` wrapper handling `onSubmit`/`noValidate` (RHF forms should never rely on native HTML validation, per `07-formularios-validacion-y-feedback.md` §1) and consistent spacing between fields.
6. Create `src/components/ds/DynamicForm.tsx`: accepts `FieldDefinition[]`, wraps `useForm` + optional Zod `resolver`, renders `Form` > one `Field` + matching control per definition, calls `onSubmit` with typed values on submit. Accept `externalErrors?: Partial<Record<string, string>>` and apply each entry via `methods.setError(name, { type: "server", message })` in a `useEffect` keyed on `externalErrors` — this is the only path for server-side field errors (e.g. a backend 422 result); the Zod `resolver` only ever validates client-side shape and cannot know about a network response.
7. Update `src/components/ds/index.ts` to export `Field`, `Textarea`, `Select`, `Checkbox`, `Form`, `DynamicForm` alongside existing exports.
8. Write tests for `Field` (renders label/required marker/error-or-hint correctly), `DynamicForm` (renders all field types from a sample config, calls `onSubmit` with validated values, blocks submission on required-field errors).
9. Build a throwaway local smoke render of `DynamicForm` configured with the Intake screen's 5 fields (`learningGoal` as `textarea`, the other 4 as `input`) to confirm the shape fits `BriefForm`'s needs before `task-04` starts — do not leave this smoke render in the codebase; delete it once confirmed and note the confirmation in this task's Verification Summary.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `Field` renders label, required marker (when `required`), and shows `error` in place of `hint` when both are present | `npm run test -- Field` |
| 2 | `DynamicForm` renders one control per `FieldDefinition`, matching the declared `control` type | `npm run test -- DynamicForm` |
| 3 | `DynamicForm` blocks submission and surfaces field errors when a required field is empty | `npm run test -- DynamicForm` |
| 3b | `DynamicForm` applies `externalErrors` to the matching fields via `setError`, and clears an entry once the user edits that field | `npm run test -- DynamicForm` |
| 4 | The Intake screen's 5-field shape (4 `input` + 1 `textarea`) renders correctly through `DynamicForm` in the smoke render (Implementation Step 9) | Manual check, evidence recorded in Verification Summary, smoke render deleted afterward |
| 5 | No component in `src/components/ds/` other than `Field` renders its own `error`/`hint` text (no regression back to the old split); `Checkbox` is the sole, PDR-001-documented exception for its inline caption/label only, not for error/hint | Manual grep/review of `Input.tsx`, `Textarea.tsx`, `Select.tsx`, `Checkbox.tsx` |

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
| 1 | App compiles and starts | `npm run build` then `npm run dev` |
| 2 | No existing consumer of `Input.tsx`'s old inline `error` text breaks | Check `src/app/login/page.tsx` and `src/app/register/page.tsx` (both use `Input`'s `error` prop today) still render their error messages correctly after `Input.tsx`'s error-rendering is removed — they'll need `Field` wrapping too, or an equivalent follow-up noted if out of scope |
| 3 | No startup regressions are visible | Inspect `npm run dev` console/terminal output for new errors or warnings |

### Database / ORM Consistency Check

N/A — no database or ORM involved.

### Logging / Observability

N/A — pure UI component library code, no network calls, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.

### Generated Test Suite

- **Task suite file:** `test-suites/task-14-design-system-form-primitives-test-suite.md`
- **Required gates:** unit, static analysis (`npm run lint`), code style, architecture/design guide review.
- **Architecture guides:** `pdr-001-design-system-form-primitives.md`, `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md` §8/§8.1, `07-formularios-validacion-y-feedback.md`, `09-nomenclatura-typescript-react.md` (TSX mandatory).
- **Acceptance environment:** N/A — no backend integration.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Verification Summary

- **Unit tests (`npm run test -- Field` / `npm run test -- DynamicForm`):** both suites written and run. Output: `Test Suites: 2 passed, 2 total / Tests: 9 passed, 9 total` (5 `Field` cases: label/required marker/hint/error swap/`aria-describedby` association; 4 `DynamicForm` cases: renders `input`/`textarea`/`select`/`checkbox` per `FieldDefinition`, blocks submit + surfaces error on empty required field, submits validated values, applies+clears an `externalErrors` entry).
- **`DynamicForm` externalErrors clearing — corrected during implementation:** the Interfaces/contracts text originally assumed RHF auto-revalidates and clears a `setError`'d field once the user edits it. Empirically this did **not** hold (verified with a failing test even after filling every other required field) — RHF's `reValidateMode: "onChange"` only re-triggers the resolver for fields that already failed *through the resolver*, and a manually `setError`'d "server" error isn't on that path. Fixed by having `DynamicForm` wrap each control's `onChange` to call `clearErrors(name)` itself when that field currently holds a `type: "server"` error; re-ran the test, now passing. Interfaces/contracts text above updated to describe the actual mechanism.
- **Step 9 smoke render (Intake screen's real 5-field shape):** built a throwaway test configuring `DynamicForm` with `learningGoal` (`textarea`) + `topic`/`level`/`duration`/`language` (`input`), matching `task-04`'s documented `briefSchema` fields exactly (confirmed against `task-04-functional-mockup-intake-screen.md`'s Interfaces/contracts). Ran it: `2 passed, 2 total` — all 5 controls render with the correct tag, submits validated values, and a fake `externalErrors={{ topic: "..." }}` renders inline. Deleted the throwaway file immediately after (`src/components/ds/__tests__/IntakeSmoke.test.tsx`, never committed) — confirmed via `git status --porcelain src/components/ds/` showing only the real 10 affected files.
- **Verification #5 (no control other than `Field` owns error/hint text):** manual review of `Input.tsx`, `Textarea.tsx`, `Select.tsx`, `Checkbox.tsx` — none renders error/hint paragraphs; `Checkbox` renders only its own inline `label`/`description` caption, matching PDR-001 item 3's amendment.
- **Static analysis (`npm run lint`):** blocked — this repo has **no ESLint config at all** yet (`next lint` launched its first-run interactive setup wizard; confirmed via `find -iname ".eslintrc*" -o -iname "eslint.config*"` returning nothing). This is a pre-existing repo-wide gap, not introduced by this task, and bootstrapping ESLint for the whole project is outside a design-system-primitives task's scope. Substituted `npx tsc --noEmit -p tsconfig.json`: zero errors outside `__tests__`/`__mocks__` files, whose errors (`Cannot find name 'jest'/'describe'/'expect'`) are a pre-existing, repo-wide missing-`@types/jest` gap present in files this task never touched (e.g. `apiClient.test.ts`, `SignOutButton.test.tsx`) — not something this task introduced or is positioned to fix.
- **Runtime smoke (`npm run build` then `npm run dev`):** `npm install` was required first (`node_modules` wasn't present). `npm run build` compiled and type-checked successfully (`✓ Compiled successfully`); it then failed prerendering the unrelated `/verify-email` page with `auth/invalid-api-key` because this sandbox has no real Firebase credentials (only `.env.local.example` exists, confirmed pre-existing, unrelated to this task). Verified `npm run dev` instead: started cleanly (`✓ Ready in 1328ms`), `curl http://localhost:3000/login` returned `HTTP 200`. Cleaned up afterward: killed the dev server, removed the throwaway `.env.local`, reverted the incidental `tsconfig.tsbuildinfo` diff from the `tsc` run.
- **Software Smoke Test Check #2 (login/register regression) — confirmed real, not fixed in this task:** `src/app/login/page.tsx` and `src/app/register/page.tsx` both pass `error={errors.field?.message}` only to `<Input>`, wrapped in `FieldWithHelper` (a different, tooltip-only sibling component that never rendered error text itself). With `Input.tsx`'s own error paragraph removed, these two pages silently lose their inline Zod validation error text (the `aria-invalid`/border-color styling still applies; the top-level Firebase `serverError` banner is unaffected since the page renders that itself, not through `Input`). Confirmed via the existing test suites: `SignInPage.test.tsx` still passes 5/5 unchanged — it only asserts the Firebase `serverError` banner and redirects, never the per-field Zod error text, so the gap isn't currently covered by any test. `RegisterPage.test.tsx` already had 3 pre-existing failures (label-text query language mismatch — queries `/full name/i` against a UI that renders "Nombres"), verified via `git stash` to predate this task's changes entirely, unrelated. **Recommended follow-up (not done here, to keep this task's scope to the 7 primitives):** give `FieldWithHelper` the same optional `error`/hint-replacement rendering `Field` now has, or migrate `login`/`register` to `Field` directly.
- **Done Criteria's `[CHECK-ATOMICITY]`:** confirmed no scope leaked beyond the 7 primitives — `login`/`register`/`FieldWithHelper` were read for research and left untouched; the regression above is documented, not fixed, per this exact allowance already written into Software Smoke Test Check #2.

---

## Done Criteria

- [x] `Field`, `Input`, `Textarea`, `Select`, `Checkbox`, `Form`, `DynamicForm` exist in `src/components/ds/` and are exported from `index.ts` — all 7 created/updated; `index.ts` exports each plus the `FieldDefinition` type.
- [x] `Field` centralizes `required`/`error`/`hint` for every control; no control other than `Field` renders its own error/hint text. `Checkbox` is the sole, PDR-001-documented exception for its own inline caption (not error/hint) — `Input`/`Textarea`/`Select` have no such exception. Confirmed by manual review (Verification #5 above).
- [x] `DynamicForm` renders a config-driven form from `FieldDefinition[]` and is confirmed (via the Step 9 smoke render, then deleted) to fit the Intake screen's 5-field shape — see Verification Summary.
- [x] `DynamicForm` accepts `externalErrors` and applies them via `setError` — the only path for server-side field errors (e.g. backend 422 results), since the Zod resolver cannot see network responses. Implemented with an explicit `clearErrors` wrapper for the edit-clears-it path (see Verification Summary's correction note).
- [x] `Form` and `DynamicForm` remain distinct components — `Form` has no field-configuration knowledge (`Form.tsx` is a plain `<form noValidate>` wrapper with spacing only).
- [x] All new/changed component tests pass — `9 passed, 9 total` (see Verification Summary).
- [~] `npm run lint` passes — N/A, no ESLint config exists in this repo (pre-existing gap, not introduced here); substituted `npx tsc --noEmit`, 0 errors outside a pre-existing, repo-wide missing-`@types/jest` gap. See Verification Summary.
- [x] `npm run build` and `npm run dev` succeed with no new console errors; `login`/`register` pages (existing `Input` `error` consumers) still render correctly or have a documented, explicitly scoped follow-up if they don't. `build` compiles/type-checks cleanly (fails only at an unrelated `/verify-email` prerender step due to a pre-existing missing-Firebase-credentials sandbox gap); `dev` starts cleanly and serves `/login` with `HTTP 200`. login/register's per-field error regression is real and explicitly documented as a follow-up, per this criterion's own allowance — see Verification Summary.
- [x] Software smoke test check above passes; for git-enabled tasks, implementation is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR. (PR publish step still pending as of this writing — see task workflow.)
- [x] Logging/observability: N/A — pure UI component library code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [x] Task test suite is generated/refreshed with `/plan-test-suite`, and every applicable quality gate above has command output or documented evidence — `test-suites/task-14-design-system-form-primitives-test-suite.md` generated and gaps filled; every gate above has command output or an explicit N/A reason.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — scope is the 7 primitives named in `pdr-001-design-system-form-primitives.md`, nothing beyond (no rubric/question-bank-specific fields, no visual redesign of unrelated DS components). `login`/`register`/`FieldWithHelper` were read but not modified.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

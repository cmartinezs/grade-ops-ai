# Code Review: task-14-design-system-form-primitives

Date: 2026-07-16  
Scope: `task-14-design-system-form-primitives.md`, `pdr-001-design-system-form-primitives.md`, generated task test suite, and the implementation diff on `tasks/story-02-assessment-screens-wireframes-and-data-providers/task-14-design-system-form-primitives` against `origin/story-02-assessment-screens-wireframes-and-data-providers`.

## Findings

### P1 - Removing `Input`'s inline error text breaks existing auth/reset field validation messages

- Files:
  - `src/components/ds/Input.tsx:80`
  - `src/components/ds/FieldWithHelper.tsx:68`
  - `src/app/forgot-password/page.tsx:82`
  - `src/app/reset-password/page.tsx:200`
  - `src/app/reset-password/page.tsx:216`
  - `src/app/reset-password/page.tsx:233`
  - `src/app/login/page.tsx:119`
  - `src/app/login/page.tsx:135`
  - `src/app/register/page.tsx:106`
  - `src/app/register/page.tsx:120`
  - `src/app/register/page.tsx:137`
  - `src/app/register/page.tsx:153`

`Input` used to render the `error` message after the native input. This task removes that paragraph and keeps `error` only for invalid styling / `aria-invalid`. That is correct for the new `Field` contract, but the current auth/reset screens still wrap `Input` with `FieldWithHelper`, not `Field`; `FieldWithHelper` renders the label and tooltip only, then returns `{children}` without rendering any error text or adding `aria-describedby`.

The result is a production-visible regression across every current `FieldWithHelper` + `Input error={errors...}` consumer: validation can block submit while showing only a red border, with no inline error message and no described-by relationship for assistive tech. This is not just hypothetical: the existing forgot/reset password tests now fail because their expected field errors disappear.

Evidence:

- `rg -n "error=\\{errors\\.[^}]+\\}" src/app src/components -g '*.tsx'` finds 10 current call sites in forgot/reset/login/register.
- `npm run test -- ForgotPasswordPage ResetPasswordPage` fails 4 tests:
  - `ForgotPasswordPage › shows field error for empty submit without calling API`
  - `ForgotPasswordPage › shows field error for invalid email format without calling API`
  - `ResetPasswordPage › shows passwords-mismatch Zod error without calling API`
  - `ResetPasswordPage › shows email field error for RESET_CODE_EMAIL_MISMATCH (422)`

Recommendation: make the responsibility migration atomic for all existing consumers. Either migrate these screens to the new `Field` primitive, or extend `FieldWithHelper` with the same `error` / hint-replacement rendering and update the call sites to pass the error to the wrapper. Add or keep page-level tests that assert the field error text remains visible.

### P2 - `FieldDefinition.required` is only visual unless every caller duplicates validation in a resolver

- Files:
  - `src/components/ds/DynamicForm.tsx:16`
  - `src/components/ds/DynamicForm.tsx:40`
  - `src/components/ds/DynamicForm.tsx:78`
  - `src/components/ds/DynamicForm.tsx:110`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-14-design-system-form-primitives.md:79`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-14-design-system-form-primitives.md:85`

`FieldDefinition.required` is part of the public `DynamicForm` contract, and the task verification says `DynamicForm` blocks submission and surfaces errors when a required field is empty. In the implementation, though, `required` only reaches the visual marker (`Field.required` / `Checkbox.required`). `useForm` receives an optional resolver, `Form` always sets `noValidate`, and `registerField()` does not derive any validation rule from `field.required`.

The current test passes because the test schema separately encodes `.min(1, "...")`. A caller can set `required: true` with no resolver, or forget to mirror one required field in the resolver, and `DynamicForm` will still submit empty values while marking the UI as required. That makes the new shared form contract easy to misuse in the exact downstream screens task-14 is meant to protect.

Recommendation: choose one contract and encode it. Either make `resolver` required for `DynamicForm` validation and document `required` as display-only, or have `DynamicForm` pass register validation rules derived from `field.required` and add a test that omits the resolver but expects required fields to block submit.

## Validation Notes

- Reviewed task-14, PDR-001, and the generated test-suite document.
- Reviewed changed primitives: `Field`, `Input`, `Textarea`, `Select`, `Checkbox`, `Form`, `DynamicForm`, exports, and new tests.
- Reviewed existing `FieldWithHelper` consumers in auth/reset pages after the shared `Input` behavior changed.
- Ran `npm run test -- Field DynamicForm`: PASS, 2 suites / 9 tests.
- Ran `npm run test -- SignInPage`: PASS, 1 suite / 5 tests. This suite does not cover client-side field error text.
- Ran `npm run test -- ForgotPasswordPage ResetPasswordPage`: FAIL, 2 suites / 4 failed tests, matching the missing field error regression above.


# Code Review: task-03-component-hierarchy-intake-screen

Date: 2026-07-15  
Scope: `task-03-component-hierarchy-intake-screen.md` and the artifacts it introduces or wires (`wireframes/intake-screen-hierarchy.md`, `pdr-001-design-system-form-primitives.md`, `task-14-design-system-form-primitives.md`, task-04/task-09 dependency updates, frontend hierarchy guide, traceability).

## Re-review - 2026-07-15

### Findings

### P2 - Task-level and downstream mockup contracts still describe the old `BriefForm` shape

- Files:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-03-component-hierarchy-intake-screen.md:30`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-04-functional-mockup-intake-screen.md:18`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-04-functional-mockup-intake-screen.md:35`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-04-functional-mockup-intake-screen.md:36`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-04-functional-mockup-intake-screen.md:37`

The main hierarchy artifact now correctly adds `fieldErrors` and the `view: IntakeAssessmentPageViewModel` flow, but the task-level contract still says `BriefForm` receives only `{ onSubmit, isSubmitting, serverError }`. Task-04 is also still written as if it can build the mockup without that contract: `BriefForm` only gets `DynamicForm` + resolver + button, `BriefFormSection` is only a wrapper, and the hook only owns a coarse `RemoteData` submit state. That leaves the actual implementation task free to recreate the exact stale component shape the re-review just fixed.

This matters because task-06 explicitly promises "no other component changes" when swapping the fake submit for `submitAssessmentBrief`. If task-04 implements the old contract, task-06 must either retrofit `fieldErrors`/`externalErrors` through `BriefFormSection`/`BriefForm`, or fail to show backend `List<FieldErrorResponse>` errors inline.

Recommendation: update task-03's Technical Design contract and task-04's Technical Design / Implementation Steps / tests so task-04 creates the final component API now: Page passes `view` to `BriefFormSection`; `BriefFormSection` forwards `isSubmitting`, `serverError`, `fieldErrors`, and `handleSubmit`; `BriefForm` passes `fieldErrors` to `DynamicForm.externalErrors`; and task-04 tests the `externalErrors` rendering path even with fake/local data.

## Re-review - 2026-07-16

### Findings

No findings. The remaining P2 from the 2026-07-15 re-review is fixed.

### Validation Notes

- `task-03-component-hierarchy-intake-screen.md` now states the task-level contract explicitly: Page calls `useIntakeAssessmentPage()`, passes the returned `view` into `BriefFormSection`, and `BriefForm` receives `{ onSubmit, isSubmitting, serverError, fieldErrors }` before passing `fieldErrors` to `DynamicForm.externalErrors`.
- `task-04-functional-mockup-intake-screen.md` now requires the mockup to build that final contract immediately, including a fake `topic: "trigger-field-error"` path and test coverage for inline rendering via `DynamicForm.externalErrors`.
- `task-06-connect-real-api-intake-screen.md` now preserves the "no component changes" claim by reusing the same `fieldErrors` path for real `List<FieldErrorResponse>` 422 responses.
- Previous P1/P2/P2/P3 findings remain resolved: backend field errors have an explicit path, `BriefFormSection` no longer depends on implicit data flow, the Checkbox/Field responsibility carve-out is documented consistently, and the PDR link resolves to the existing file.
- Runtime tests not run: this remains a design/planning artifact review with no executable implementation in task-03.

## Previous Findings Status

- Resolved: P1 server-side field errors now have an explicit `fieldErrors` -> `DynamicForm.externalErrors` path in `wireframes/intake-screen-hierarchy.md` and task-14.
- Resolved: P2 `BriefFormSection` no longer claims implicit data flow in the hierarchy artifact; Page passes a `view` prop.
- Resolved: P2 Checkbox / `Field` responsibility conflict is reconciled by a narrow PDR-001 checkbox-caption carve-out, while `Field` keeps error/hint ownership.
- Resolved: P3 PDR link now resolves to `../../../pdr-001-design-system-form-primitives.md`; verified with `fs.existsSync`.

## Original Findings

### P1 - Server-side field errors have no path back into `DynamicForm`

- Files:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/intake-screen-hierarchy.md:18`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/intake-screen-hierarchy.md:63`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-14-design-system-form-primitives.md:30`

The hierarchy says `useIntakeAssessmentPage` owns field-error mapping, but `BriefFormProps` only exposes `onSubmit`, `isSubmitting`, and `serverError`; line 63 then says field-level 422 errors are surfaced by `DynamicForm` "via its resolver." A resolver only validates local Zod/client state. It cannot display backend `List<FieldErrorResponse>` results from the submit mutation unless the form contract has an explicit server-field-error path, such as `fieldErrors`, `initialErrors`, a `setServerErrors` callback, or a `DynamicForm` API for applying backend validation errors.

This directly affects task-06 because task-02 already documented that `POST /assessments` can return field-level 422 validation errors with a distinct body shape. As written, the next implementer can satisfy the documented `BriefForm`/`DynamicForm` contract and still have no way to show those backend field errors inline.

Recommendation: add an explicit contract between `useIntakeAssessmentPage`, `BriefForm`, and `DynamicForm` for backend field errors. Then update task-14's `DynamicFormProps` and task-03's hierarchy so the server-side 422 flow is not attributed to the Zod resolver.

### P2 - `BriefFormSection` is specified as prop-less while the Page owns the hook result

- Files:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/intake-screen-hierarchy.md:15`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/intake-screen-hierarchy.md:78`

The hierarchy says the Page invokes `useIntakeAssessmentPage` and renders `BriefFormSection`, but later says `BriefFormSection` takes no props and "receives the hook's view model implicitly by rendering `BriefForm` with the values `useIntakeAssessmentPage` returns." There is no implicit data flow in React for that. Either the Page must pass the hook's view model into `BriefFormSection`, or `BriefFormSection` must invoke the hook itself. The current wording conflicts with the "Page invokes hook" responsibility and leaves task-04 with an impossible component contract.

Recommendation: choose one ownership model. The cleaner fit with the hierarchy guide is Page calls `useIntakeAssessmentPage()` and passes `{ isSubmitting, serverError, fieldErrors, onSubmit }` or a named view model prop into `BriefFormSection`.

### P2 - PDR says Checkbox labels/errors go through `Field`, but task-14 tells Checkbox to render them inline

- Files:
  - `.planning/active/001-assessment-creation/pdr-001-design-system-form-primitives.md:18`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-14-design-system-form-primitives.md:43`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-14-design-system-form-primitives.md:61`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-14-design-system-form-primitives.md:93`

PDR-001 decision item 3 says concrete controls including `Checkbox` reuse `Field` instead of each reimplementing labels, errors, and hints. Task-14 step 3 contradicts that by directing `Checkbox` to own an inline label and error. Its verification and done criteria then say no component except `Field` should render label/error text. This makes task-14 internally inconsistent before implementation starts.

Recommendation: either update the PDR to explicitly carve out checkbox layout while preserving `Field` as the owner of error/hint association, or update task-14 so `Checkbox` composes through `Field` consistently with the accepted decision and its own verification rows.

### P3 - The hierarchy doc links to the PDR with the wrong relative path

- File:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/intake-screen-hierarchy.md:5`

The hierarchy file lives under `.../story-02-assessment-screens-wireframes-and-data-providers/wireframes/`, while the PDR lives at `.planning/active/001-assessment-creation/pdr-001-design-system-form-primitives.md`. The link text uses `../pdr-001-design-system-form-primitives.md`, which resolves to the story task directory, not the planning root. Readers following the link will land on a missing file.

Recommendation: change the relative link to `../../../pdr-001-design-system-form-primitives.md`.

## Verification

- Reviewed the task-03 task file and generated hierarchy artifact.
- Reviewed the new PDR, task-14, task-04/task-09 dependency changes, traceability update, and component hierarchy guide update.
- Cross-checked local DS files (`Input.tsx`, `Field.tsx`, `Button.tsx`, `index.ts`) and the existing assessments route directory.
- Runtime tests not run: this task is a design/planning document review with no executable code changes in scope.

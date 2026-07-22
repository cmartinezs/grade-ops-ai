# ⚛️ TASK 03 — component-hierarchy-intake-screen

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-02
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A written Page → Section → Component breakdown for the Intake screen, following `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md`, naming every file `task-04` (mockup) and `task-06` (real API wiring) will create.

---

## Technical Design

- **Approach:** Apply the project's standard hierarchy (`Layout → Page → Section → Component`) rather than a flat single-file component — the screen has one non-trivial form with its own submit lifecycle, which per §9 of the guide requires its own hook, and a single-purpose screen like this doesn't need `SubSection`/`MiniComponent` layers beyond the form itself.
- **Affected files / components:** New: `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/intake-screen-hierarchy.md`, `.planning/active/001-assessment-creation/pdr-001-design-system-form-primitives.md`, `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-14-design-system-form-primitives.md`. Modified: `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md`, `.planning/active/001-assessment-creation/TRACEABILITY.md`, `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers.md`, `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-04-functional-mockup-intake-screen.md`, `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-06-connect-real-api-intake-screen.md`, `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-09-functional-mockup-draft-builder-screen.md`.

  The new PDR/task/traceability files exist because human review escalated a cross-cutting form-architecture concern out of this task (see Design notes below). The guide edit adds new section 8 point 1 plus a pointer to the PDR. The story file's task table gains a row for the new task. The two sibling task files each gain the new task as a dependency, and had their old "reuse existing components ad hoc" guidance replaced with a reference to the new primitives — otherwise they'd repeat the exact problem the PDR closes.

  This bullet's list above is exhaustive for staging purposes; everything below names files `task-04` (mockup) and `task-06` (real API wiring) will create, not files this task touches:
  - `src/app/(protected)/assessments/new/page.tsx` — Page
  - `src/features/assessment-creation/components/BriefFormSection.tsx` — Section
  - `src/features/assessment-creation/components/BriefForm.tsx` — Component (thin DynamicForm configuration, not hand-composed fields — see PDR-001)
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` — Page hook
  - `src/features/assessment-creation/schemas/briefSchema.ts` — Zod schema
  - DS form primitives (Field/Input/Textarea/Select/Checkbox/Form/DynamicForm) — built by the new task-14, not this task or task-04
- **Interfaces / contracts:** the Page calls `useIntakeAssessmentPage()` and passes the result into `BriefFormSection` as a single `view: IntakeAssessmentPageViewModel` prop (no implicit data flow — `BriefFormSection` does not call the hook itself); `BriefFormSection` forwards `view.isSubmitting`, `view.serverError`, `view.fieldErrors`, and `view.handleSubmit` (as `onSubmit`) to `BriefForm`. `BriefForm` receives `{ onSubmit, isSubmitting, serverError, fieldErrors }` and passes `fieldErrors` straight to `DynamicForm`'s `externalErrors` prop (`task-14`) — the only path for backend 422 field errors, since the Zod resolver only validates client-side shape. Naming follows the guide's prop-naming rules (`onApprove`/`onRetry`-style callback naming, §11 of `03-jerarquia-de-componentes.md`).
- **Risk:** Low — this is a routine hierarchy decision using an existing project pattern (`features/<feature>/` per `01-arquitectura-next-react.md` §5), not a novel structure.
- **Design notes:** `features/assessment-creation/` is a new feature folder — this is the first story to populate it (no existing `features/` directory exists yet in `web/src/`; the project currently keeps everything under flat `components/`). Both screens in this story (Intake, Draft Builder) share this same feature folder.
- **MicroComponent/MiniComponent criterion (first round of human review):** an initial draft of this task's doc dismissed MicroComponents entirely ("no new Badge/IconButton/Spinner-style atom needed"), which was wrong — the screen does use existing MicroComponents (`Input`, `Button`) and needs one new one (`Textarea`, no multi-line field exists in the DS yet). The reviewer's correction: a base `Input`/`Textarea` stays a `MicroComponent` as long as it's parameterizable via native attrs (`required`/`minLength`/`maxLength`/`pattern`); a *specialized* `MiniComponent` (e.g. `EmailInput`) is only worth extracting once a specific validation shape repeats across screens — none of this screen's 5 fields need one (all are `@NotBlank String`, no enum/pattern on the backend). This distinction didn't previously exist in `03-jerarquia-de-componentes.md`, so it was added as new §8.1 for future screens to reference, citing the real duplicate email-validation pattern already present in `src/app/login/page.tsx`/`src/app/register/page.tsx` as the motivating (out-of-scope) example.
- **Form architecture, second round of human review — escalated to a PDR, not just a doc fix:** the first round's correction (above) was still per-field/MicroComponent-scoped. The reviewer then raised a broader point: `Input`, `Button`, `Textarea` etc. existing as reusable atoms doesn't automatically mean *forms* built from them are reused — the original hierarchy still had `BriefForm` hand-compose each field individually, which just relocates the "repeat the reuse decision per screen" problem one level up (from fields to whole forms). Since this affects every future form in `web/`, not just the Intake screen, it was escalated to a Project Decision Record (`pdr-001-design-system-form-primitives.md`, status Accepted) rather than decided inline in this task's doc. The PDR mandates DS-wide `Form`/`Field`/`Input`/`Textarea`/`Select`/`Checkbox`/`DynamicForm` primitives, with `Field` centralizing label/required/error/hint and `DynamicForm` as an optional declarative renderer (not mandatory for complex/non-linear forms). Implementing those primitives is out of scope for this task — split into `task-14-design-system-form-primitives.md`, added as a new dependency to `task-04` (this screen's functional mockup) and `task-09` (Draft Builder's functional mockup, which also builds forms). This task's own hierarchy doc was revised to configure `DynamicForm` (via `BriefForm`) instead of hand-composing fields, matching the PDR.

---

## Implementation Steps

1. Write `wireframes/intake-screen-hierarchy.md` listing the Page/Section/Component/hook/schema files above, each with a one-line responsibility.
2. For each file, state whether it's a Server or Client Component per `01-arquitectura-next-react.md` §7 — the Page and its children are all Client Components here (`"use client"`) since the form has interactive state and Firebase-authenticated fetch calls, which cannot run as Server Components in this project's client-driven auth model.
3. Confirm the route path `src/app/(protected)/assessments/new/` doesn't collide with the existing `src/app/(protected)/assessments/page.tsx` placeholder (it doesn't — Next.js route groups treat `new/` as a distinct segment).

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Every file named in the hierarchy doc maps to exactly one responsibility (Page composes, Section holds business meaning, Component renders, hook holds logic) | Manual review against `03-jerarquia-de-componentes.md` §2-6 |
| 2 | No component in the hierarchy exceeds one hook's worth of non-trivial logic (no "component gets fetching + form + modal" anti-pattern per §10) | Manual review against `03-jerarquia-de-componentes.md` §10 |

### Software Smoke Test Check

N/A — design document only, no runtime surface.

### Database / ORM Consistency Check

N/A.

### Logging / Observability

N/A — this task produces no executable code.

### Generated Test Suite

- **Task suite file:** `test-suites/task-03-component-hierarchy-intake-screen-test-suite.md`
- **Required gates:** architecture/design guide review only.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md`, `01-arquitectura-next-react.md`.
- **Acceptance environment:** N/A.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Verification Summary

`wireframes/intake-screen-hierarchy.md` written (see file). Evidence per Done Criteria item:

- **Verification row 1 (every file → exactly one responsibility):** the hierarchy table (§1 of the doc) lists all 9 files — Layout (existing, reused), Page, Section, Component, Page hook, Schema, plus 3 MicroComponent rows (`Input`/`Button` existing-reused, `Textarea` new) — each with a single one-line responsibility and Server/Client designation, matching `03-jerarquia-de-componentes.md` §2 (Layout), §3 (Page: "componer la pantalla", not implement forms), §4 (Section: business block, no logic of its own), §6 (Component: renders + delegates to hook), §8/§8.1 (MicroComponent: parameterizable base vs. specialized derived), §9 (non-trivial component → own hook). Cross-checked against `04-hooks-y-logica-de-ui.md` §1: the hook (`useIntakeAssessmentPage`) owns state/effects/handlers/error-normalization; `BriefForm` owns structure/composition/DS usage/prop-binding only — no overlap.
- **Verification row 2 (no anti-pattern, §10):** §6 of the doc states the check explicitly — `BriefForm` renders only (no fetching inline), all orchestration lives in the hook, estimated size is 5 fields + 1 button + 1 conditional banner (well under the 250-line/god-component threshold), and the screen has exactly 1 handler (submit) and 0 local effects against `01-arquitectura-next-react.md` §11's growth thresholds (>150 líneas TSX, >5 estados, >3 efectos, >4 handlers, >2 responsabilidades, formulario con validación y submit — this screen sits at 1 form with validation+submit, the threshold that triggers *watching* for growth, not a violation, since there is nothing left to divide further at this hierarchy level).
- **Corrected during human review — MicroComponent/MiniComponent criterion:** the first draft claimed no MicroComponent was needed; reviewer flagged that `Input`, `Button`, and loaders are exactly what MicroComponents are for, and the doc needed to apply the actual criterion instead of dismissing the whole level. Re-derived per-field:
  - `topic`, `level`, `duration`, `language` → existing `Input` (`src/components/ds/Input.tsx`), already parameterizable (`required`/`minLength`/`maxLength`/`pattern` via `...props extends React.InputHTMLAttributes`) — reused as-is, no new component.
  - `learningGoal` → no `Textarea` exists in the DS (`src/components/ds/index.ts` checked directly — only `Input` is exported) — flagged as a new base MicroComponent required, not optional, before `task-04`/`task-05` can build the real form.
  - Submit button → existing `Button` (`src/components/ds/Button.tsx`), `loading` prop already renders `Spinner` internally (`Button.tsx:18,108`) — reused as-is.
  - No specialized *derived* MiniComponent (e.g. `EmailInput`) is needed for any of the 5 fields — all are `@NotBlank String` with no enum/pattern on the backend, so none has a repeatable fixed-format validation shape to justify one.
- **Guide gap fixed, not just worked around:** `03-jerarquia-de-componentes.md` had no rule distinguishing "parameterizable base input" from "specialized derived input," which is what caused the first draft's dismissal. Added §8.1 to the guide itself (not just this task's doc) so future screens have this criterion up front — cited the real, pre-existing duplicate email-validation pattern in `src/app/login/page.tsx` and `src/app/register/page.tsx` (`type="email"` + `z.string().email(...)`, hand-rolled in both, never extracted) as the concrete motivating example, without refactoring those two files (out of scope for this task).
- **Other claims verified directly against the codebase, not assumed:**
  - Route collision check (§5 of the doc): `ls src/app/(protected)/assessments/` → only `page.tsx` exists, no `new/` subfolder — confirmed no collision, matching Implementation Step 3.
  - `level` field type (§3 of the doc, resolving `task-02`'s "TBD" note): `CreateAssessmentBriefRequest.java` declares `@NotBlank String level` — plain string, no enum — resolved as free text input, not a select.
  - Server-error banner precedent (`src/app/login/page.tsx`): existing pattern renders `<div role="alert">` inline rather than as an extracted component — followed the same precedent for `BriefForm`'s banner instead of inventing a new MiniComponent.
- **Corrected during second round of human review — form-level reuse, escalated to PDR-001:** the doc's §1 hierarchy table and §4 interfaces originally had `BriefForm` call `useForm` directly and render `Field`+`Input`/`Textarea` per field itself. Reviewer pointed out this repeats the same-shaped mistake as the first correction, one level up: reusable atoms (`Input`, `Button`) don't prevent duplicated *form-building* logic across screens. Rewrote §1/§3/§4/§6 of `wireframes/intake-screen-hierarchy.md` so `BriefForm` only supplies a `FieldDefinition[]` + `briefSchema` to the shared `DynamicForm` (owns `useForm` internally) — zero field-level markup in `BriefForm` itself. Created `pdr-001-design-system-form-primitives.md` (Accepted) recording the DS-wide rule, and `task-14-design-system-form-primitives.md` to implement it, wired as a new `Depends On` entry into `task-04` and `task-09` (both build forms) — confirmed by reading both files directly and finding `task-04`'s prior "reuse existing DS components ... rather than inventing new form primitives" guidance, which would have re-caused the exact problem the PDR closes; corrected that line too.
- **Formal code review (`.code-reviews/story-02-assessment-screens-wireframes-and-data-providers/task-03-component-hierarchy-intake-screen.md`), 4 findings, all fixed:**
  - **P1 — server-side field errors had no path into `DynamicForm`:** doc line then read "field-level 422 errors are surfaced by `DynamicForm` via its resolver," which is wrong — a Zod resolver only validates client-side shape before the network call happens, it cannot know about a backend `List<FieldErrorResponse>` result. Fixed by adding an explicit `fieldErrors`/`externalErrors` path: `useIntakeAssessmentPage` maps the 422 body → `fieldErrors` in its view model → `BriefForm`'s new `fieldErrors` prop → `DynamicForm`'s new `externalErrors` prop (`task-14`, applies via RHF `setError(name, { type: "server", message })`). Updated `wireframes/intake-screen-hierarchy.md` §1/§4 and `task-14`'s Interfaces/Implementation Step 6/Verification/Done Criteria to match.
  - **P2 — `BriefFormSection` specified prop-less while the Page owned the hook result:** impossible data flow (no implicit prop passing in React). Fixed: Page calls `useIntakeAssessmentPage()` and passes the view model into `BriefFormSection` as a `view` prop; `BriefFormSection` forwards it to `BriefForm`. Updated the Page/Section rows in §1 and the `BriefFormSection` bullet in §4 (removed the "no props, implicit" claim entirely).
  - **P2 — PDR said `Checkbox` goes through `Field` for labels, but task-14 told `Checkbox` to render its own label/error inline:** internally inconsistent before implementation. Fixed by reading the actual reference source, `design-system/components/forms/Checkbox.jsx` — its label is a native `<label>` wrapping the `<input>` (required for click-to-toggle/screen-reader semantics), a genuine, narrow exception unlike `Input`/`Textarea`/`Select`. Amended `pdr-001-design-system-form-primitives.md` decision item 3 with an explicit `Checkbox` carve-out (caption only, not error/hint — `Field` still owns error/hint for `Checkbox` too), and updated `task-14`'s step 3, `FieldProps` (`label` now optional), Verification row 5, and Done Criteria to match the amended PDR consistently.
  - **P3 — broken relative link:** `wireframes/intake-screen-hierarchy.md`'s link to the PDR used `../pdr-001-...md` (resolves to the story task directory) instead of `../../../pdr-001-...md` (the actual planning root). Verified the fix programmatically: `path.normalize(path.join(path.dirname(<hierarchy-doc-path>), "../../../pdr-001-design-system-form-primitives.md"))` resolves to `.planning/active/001-assessment-creation/pdr-001-design-system-form-primitives.md`, and `fs.existsSync` on that resolved path returns `true`.
- **Re-review (same file, "Re-review — 2026-07-15" section), 1 remaining P2, now fixed:** all 4 original findings were confirmed resolved in the hierarchy artifact itself, but the re-review caught that I had fixed `wireframes/intake-screen-hierarchy.md` without propagating the same fix to this task's own "Interfaces / contracts" bullet (still read `BriefForm` receives `{ onSubmit, isSubmitting, serverError }`, missing `fieldErrors`) and to `task-04`'s Technical Design/Implementation Steps/Verification/Done Criteria, which were still written to build the *old* shape. Reviewer's concrete concern: `task-06` promises "no other component changes" when swapping the fake submit for the real API call — if `task-04` builds the stale contract, `task-06` would have to retrofit `fieldErrors`/`externalErrors` wiring despite that promise. Fixed by:
  - Rewriting this task's own "Interfaces / contracts" bullet (Technical Design, above) to match the corrected `view`/`fieldErrors` flow exactly.
  - Rewriting `task-04`'s Approach, Interfaces/contracts, Implementation Steps 2-6, Verification (added rows 4-5), and Done Criteria so the mockup builds the *final* `view` → `BriefFormSection` → `BriefForm` → `DynamicForm.externalErrors` contract now, including a fake `topic: "trigger-field-error"` case in the hook and a test asserting it renders inline — so the `fieldErrors` path is exercised with fake data, not left implicit until `task-06`.
  - Tightening `task-06`'s Interfaces/contracts bullet and Implementation Step 3 to state explicitly that a real 422's `fieldErrors` reuses the exact mechanism `task-04` builds, with no new prop/component — making its "no other component changes" claim actually true instead of just asserted.
  - Added `task-06-connect-real-api-intake-screen.md` to this task's Affected files list (Technical Design, above) since it was touched during this fix.

## Master Plan Addendum — Intake Hierarchy Gates

Added after this task was already `DONE` in `develop`. Any R01 revalidation or future hierarchy change must identify ownership for effective locale, localized copy/error/catalog labels, `outputLocale`/`contentLocale`, API I/O boundaries and async completion.

Components must not own final hardcoded user-facing strings or fake completion timers. The page/hook boundary resolves locale and API state; presentational components receive localized strings/view models and code identifiers remain in English.

---

## Done Criteria

- [x] `wireframes/intake-screen-hierarchy.md` names every file, its responsibility, and Server/Client designation. — §1 hierarchy table, all 6 files.
- [x] The hierarchy has no anti-pattern from `03-jerarquia-de-componentes.md` §10. — §6 of the doc; see Verification Summary row 2 above.
- [x] Software smoke/build/startup/connectivity checks: N/A, no runtime surface. PR #69 (`tasks/story-02-assessment-screens-wireframes-and-data-providers/task-03-component-hierarchy-intake-screen` → `story-02-assessment-screens-wireframes-and-data-providers`) opened, reviewed across two rounds of corrections (4 findings + 1 re-review finding, all fixed and re-verified in `.code-reviews/story-02-assessment-screens-wireframes-and-data-providers/task-03-component-hierarchy-intake-screen.md`, both pushed to the same PR), approved, and merged 2026-07-16 (merge commit `ae9e4e8`).
- [x] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [x] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — every file beyond `wireframes/intake-screen-hierarchy.md` was touched at explicit, traceable reviewer direction, not self-initiated scope creep: `03-jerarquia-de-componentes.md` §8.1 (round 1 correction); `pdr-001-design-system-form-primitives.md` + `TRACEABILITY.md` + `task-14-design-system-form-primitives.md` + the story file's task table + `task-04`/`task-09`'s `Depends On`/design-notes (round 2 correction, explicitly directed to become "un PDR/ADR y una tarea independiente"); and `task-04`/`task-06`'s Technical Design/Steps/Verification/Done Criteria (formal code review + re-review, propagating the corrected contract so `task-06`'s "no other component changes" claim holds). No code was written; the actual DS primitives (`Field`, `Textarea`, `Select`, `Checkbox`, `Form`, `DynamicForm`) are `task-14`'s implementation work, not this task's.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

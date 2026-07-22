# ⚛️ TASK 03 — component-hierarchy-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-02
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A written Page → Section → Component breakdown for the Intake screen, following `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md`, naming every file `task-04` (mockup) and `task-06` (real API wiring) will create, including the DS component/control selected for each intake field, the API/sync-async state boundary and the i18n ownership boundary.

---

## Technical Design

- **Approach:** Apply the project's standard hierarchy (`Layout → Page → Section → Component`) rather than a flat single-file component — the screen has one non-trivial form with its own submit lifecycle, which per §9 of the guide requires its own hook, and a single-purpose screen like this doesn't need `SubSection`/`MiniComponent` layers beyond the form itself.
- **Affected files / components:** New file `wireframes/intake-screen-hierarchy.md` (design artifact). Names the following real files to be created in later tasks:
  - `src/app/(protected)/assessments/new/page.tsx` — Page
  - existing dashboard page/action component — visible "Nueva evaluacion" action that navigates to `/assessments/new`
  - `src/features/assessment-creation/components/BriefFormSection.tsx` — Section
  - `src/features/assessment-creation/components/BriefForm.tsx` — Component
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` — Page hook
  - `src/features/assessment-creation/schemas/briefSchema.ts` — Zod schema
- **Interfaces / contracts:** `BriefFormSection` receives no props beyond what the Page passes (the hook's state/handlers); `BriefForm` receives `{ onSubmit, isSubmitting, serverError }` per the guide's prop-naming rules (`onApprove`/`onRetry`-style callback naming, §11 of `03-jerarquia-de-componentes.md`). The hierarchy must name field-level controls and any catalog provider boundary instead of hiding all controls inside generic `Input`. User-facing strings come from the i18n layer/page view model, not hardcoded component literals; code identifiers and prop names stay in English.
- **Risk:** Low — this is a routine hierarchy decision using an existing project pattern (`features/<feature>/` per `01-arquitectura-next-react.md` §5), not a novel structure.
- **Design notes:** `features/assessment-creation/` is a new feature folder — this is the first story to populate it (no existing `features/` directory exists yet in `web/src/`; the project currently keeps everything under flat `components/`). Both screens in this story (Intake, Draft Builder) share this same feature folder.

---

## Implementation Steps

1. Write `wireframes/intake-screen-hierarchy.md` listing the Page/Section/Component/hook/schema files above, each with a one-line responsibility.
2. For each file, state whether it's a Server or Client Component per `01-arquitectura-next-react.md` §7 — the Page and its children are all Client Components here (`"use client"`) since the form has interactive state and Firebase-authenticated fetch calls, which cannot run as Server Components in this project's client-driven auth model.
3. Confirm the route path `src/app/(protected)/assessments/new/` doesn't collide with the existing `src/app/(protected)/assessments/page.tsx` placeholder (it doesn't — Next.js route groups treat `new/` as a distinct segment).
4. Identify the dashboard file/component that owns the existing "Nueva evaluacion" action and document whether it is a `Link`, router push or button callback; this action is part of the hierarchy for US-010 reachability even if the form itself lives under `/assessments/new`.
5. Add the field-control hierarchy: `BriefForm` uses `Textarea`/long-text control for `learningGoal`, `Select`/`Radio` for `level`, numeric/preset control for `duration`, selector/catalog control for `language`, and topic picker/tag/custom-controlled component according to `task-01`/`task-02` residuals.
6. Add API boundary responsibilities: hook owns submit state and calls `submitAssessmentBrief`; `lib/api` owns DTO mapping; missing catalog/default/capability providers are named as API residuals, not hidden local state.
7. Add i18n responsibilities: page/hook resolves effective locale, components receive translated labels/help/error text or translation keys, catalog labels are locale-aware, `AssessmentBriefFormValue` keeps programming `language` separate from `outputLocale`/`contentLocale`, and observability remains English.
8. If generation is async, name the status/progress component or hook responsibility that consumes the agreed completion mechanism; do not leave completion as a timer in the component tree.
9. If a catalog/master-data provider is needed but not supported by API yet, name the interface as a residual or future provider; do not collapse it into unrestricted text in the hierarchy.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Every file named in the hierarchy doc maps to exactly one responsibility (Page composes, Section holds business meaning, Component renders, hook holds logic) | Manual review against `03-jerarquia-de-componentes.md` §2-6 |
| 2 | No component in the hierarchy exceeds one hook's worth of non-trivial logic (no "component gets fetching + form + modal" anti-pattern per §10) | Manual review against `03-jerarquia-de-componentes.md` §10 |
| 3 | Hierarchy includes the dashboard action that reaches `/assessments/new` | Manual review against UI Action Reachability rule |
| 4 | Hierarchy names the DS control for every intake field and any catalog provider/residual | Manual review against `ui-design-data-strategy.md` and task-02 field matrix |
| 5 | Hierarchy identifies API boundary and async status/progress owner when applicable | Manual review against task-02 API I/O + sync/async matrix |
| 6 | Hierarchy identifies i18n ownership for copy, errors, catalog labels and generated-content locale without translating code identifiers | Manual review against task-02 i18n matrix |

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

## Done Criteria

- [ ] `wireframes/intake-screen-hierarchy.md` names every file, its responsibility, and Server/Client designation.
- [ ] The hierarchy identifies the existing dashboard action owner and its navigation responsibility to `/assessments/new`.
- [ ] The hierarchy maps every intake field to the correct DS/control type and does not hide all fields inside generic text input components.
- [ ] Missing catalog/master-data support is named as API/provider residual rather than implemented as unrestricted `Input`.
- [ ] API boundary and sync/async status ownership are explicit; no component owns fake completion timers as final behavior.
- [ ] i18n ownership is explicit: no hardcoded user-facing component literals as final behavior, catalog labels are locale-aware, and `outputLocale`/`contentLocale` is separate from programming `language`.
- [ ] The hierarchy has no anti-pattern from `03-jerarquia-de-componentes.md` §10.
- [ ] Software smoke/build/startup/connectivity checks: N/A, no runtime surface; for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [ ] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

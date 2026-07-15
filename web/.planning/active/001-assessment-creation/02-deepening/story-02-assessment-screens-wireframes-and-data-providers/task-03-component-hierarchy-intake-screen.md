# ⚛️ TASK 03 — component-hierarchy-intake-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-02
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A written Page → Section → Component breakdown for the Intake screen, following `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md`, naming every file `task-04` (mockup) and `task-06` (real API wiring) will create.

---

## Technical Design

- **Approach:** Apply the project's standard hierarchy (`Layout → Page → Section → Component`) rather than a flat single-file component — the screen has one non-trivial form with its own submit lifecycle, which per §9 of the guide requires its own hook, and a single-purpose screen like this doesn't need `SubSection`/`MiniComponent` layers beyond the form itself.
- **Affected files / components:** New file `wireframes/intake-screen-hierarchy.md` (design artifact). Names the following real files to be created in later tasks:
  - `src/app/(protected)/assessments/new/page.tsx` — Page
  - `src/features/assessment-creation/components/BriefFormSection.tsx` — Section
  - `src/features/assessment-creation/components/BriefForm.tsx` — Component
  - `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` — Page hook
  - `src/features/assessment-creation/schemas/briefSchema.ts` — Zod schema
- **Interfaces / contracts:** `BriefFormSection` receives no props beyond what the Page passes (the hook's state/handlers); `BriefForm` receives `{ onSubmit, isSubmitting, serverError }` per the guide's prop-naming rules (`onApprove`/`onRetry`-style callback naming, §11 of `03-jerarquia-de-componentes.md`).
- **Risk:** Low — this is a routine hierarchy decision using an existing project pattern (`features/<feature>/` per `01-arquitectura-next-react.md` §5), not a novel structure.
- **Design notes:** `features/assessment-creation/` is a new feature folder — this is the first story to populate it (no existing `features/` directory exists yet in `web/src/`; the project currently keeps everything under flat `components/`). Both screens in this story (Intake, Draft Builder) share this same feature folder.

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

## Done Criteria

- [ ] `wireframes/intake-screen-hierarchy.md` names every file, its responsibility, and Server/Client designation.
- [ ] The hierarchy has no anti-pattern from `03-jerarquia-de-componentes.md` §10.
- [ ] Software smoke/build/startup/connectivity checks: N/A, no runtime surface; for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [ ] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

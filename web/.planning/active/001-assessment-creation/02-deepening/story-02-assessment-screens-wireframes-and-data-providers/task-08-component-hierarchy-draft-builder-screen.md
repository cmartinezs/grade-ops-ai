# ⚛️ TASK 08 — component-hierarchy-draft-builder-screen

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-07
> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

---

## Objective

A written Page → Sections → Components breakdown for the Draft Builder screen, following `03-jerarquia-de-componentes.md`, naming every file `task-09` (mockup) and `task-12` (real API wiring) will create.

---

## Technical Design

- **Approach:** Three `Section`s under one `Page` — `DraftEditorSection`, `RegenerateSection`, `VersionHistorySection` — each independently non-trivial (own state/handlers) and therefore each gets its own hook per §9, composed by one page-level hook (`useAssessmentDraftBuilderPage`) that owns the Screen Data Facade call from `task-10`.
- **Affected files / components:** New file `wireframes/draft-builder-screen-hierarchy.md` (design artifact). Names the following real files to be created in later tasks:
  - `src/app/(protected)/assessments/[id]/draft/page.tsx` — Page
  - `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` — Page hook (owns the Screen Data Facade)
  - `src/features/assessment-creation/components/DraftEditorSection.tsx` + `hooks/useDraftEditorSection.ts`
  - `src/features/assessment-creation/components/RegenerateSection.tsx` + `hooks/useRegenerateSection.ts`
  - `src/features/assessment-creation/components/VersionHistorySection.tsx` + `hooks/useVersionHistorySection.ts`
  - `src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts`
- **Interfaces / contracts:** `DraftEditorSection` receives the current draft view model + an `onSave` callback; `RegenerateSection` receives an `onRegenerate(adjustmentNotes)` callback + its own submitting/error state; `VersionHistorySection` receives the versions list (read-only) + a `onViewVersion(versionNumber)` callback for local (non-mutating) selection — no `onRestore` callback exists, since no such endpoint exists (`task-01`).
- **Risk:** Low — this is a routine hierarchy decision for a screen with 3 independent concerns, using the same `features/<feature>/` pattern already established by `task-03` for the Intake screen.
- **Design notes:** Route is `src/app/(protected)/assessments/[id]/draft/` — a dynamic segment, matching the redirect target from `task-06`.

---

## Implementation Steps

1. Write `wireframes/draft-builder-screen-hierarchy.md` listing every file above with its responsibility and Server/Client designation (all Client Components — interactive state + Firebase-authenticated fetch, same reasoning as `task-03`).
2. Name the page-level view model shape returned by `useAssessmentDraftBuilderPage`: `{ draft: AssessmentDraftViewModel, versions: AssessmentDraftVersionViewModel[], selectedVersion: number }`.
3. Confirm each Section's callback names follow the `onX` action-naming convention (`onSave`, `onRegenerate`, `onViewVersion`) per `03-jerarquia-de-componentes.md` §11 — no bare boolean props for variant control.

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Every file named maps to exactly one responsibility, and each of the 3 Sections has its own hook per §9 | Manual review against `03-jerarquia-de-componentes.md` §4-6, §9 |
| 2 | No callback name is a bare boolean or unnamed function; all use `onX` action naming | Manual review against `03-jerarquia-de-componentes.md` §11 |
| 3 | No `onRestore`/rollback callback appears anywhere in the hierarchy | Cross-check against `task-01`'s confirmed contract |

### Software Smoke Test Check

N/A — design document only, no runtime surface.

### Database / ORM Consistency Check

N/A.

### Logging / Observability

N/A — this task produces no executable code.

### Generated Test Suite

- **Task suite file:** `test-suites/task-08-component-hierarchy-draft-builder-screen-test-suite.md`
- **Required gates:** architecture/design guide review only.
- **Architecture guides:** `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md`, `04-hooks-y-logica-de-ui.md`.
- **Acceptance environment:** N/A.
- **Acceptance dependency inventory:** N/A.
- **Missing acceptance profile:** N/A.

---

## Done Criteria

- [ ] `wireframes/draft-builder-screen-hierarchy.md` names every file, its responsibility, and Server/Client designation.
- [ ] Each of the 3 Sections has its own named hook.
- [ ] No restore/rollback affordance appears anywhere.
- [ ] Software smoke/build/startup/connectivity checks: N/A, no runtime surface; for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR.
- [ ] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [ ] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [ ] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

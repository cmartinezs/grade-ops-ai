# ⚛️ TASK 08 — component-hierarchy-draft-builder-screen

> **Status:** DONE
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

## Verification Summary

`wireframes/draft-builder-screen-hierarchy.md` written (see file). Evidence per Done Criteria item:

- **Every file named, one responsibility each, Server/Client designation:** § 1 Hierarchy table lists Layout (existing, reused) → Page → 3 Sections + their 3 hooks → Page hook → Mapper, 10 rows total, matching the `inspect` stage's affected-files list exactly. All are Client Components, with the same §7 rationale (hooks, user events, Firebase-authenticated fetch) already established for the Intake screen in `task-03`/`wireframes/intake-screen-hierarchy.md` — see § 2.
- **Each of the 3 Sections has its own named hook (§9):** `useDraftEditorSection` (local form/validation state, calls the page-hook-supplied `onSave`), `useRegenerateSection` (local textarea/validation state, calls the page-hook-supplied `onRegenerate`), `useVersionHistorySection` (label/preview derivation, satisfying §9's "deriva datos para mostrar" trigger even with no local `useState`) — see § 1 table and § 3's justification for why these stay 3 separate hooks rather than 1 shared one.
- **No restore/rollback affordance:** `VersionHistorySectionProps` (§ 4) has only `onViewVersion(versionNumber)`, no `onRestore` — cross-checked against `task-01`'s exhaustive 7-endpoint `AssessmentController.java` listing (no 8th mapping).
- **Additional finding beyond the task's original scope (historical-preview vs. edit contract):** tracing this task's own Technical Design ("`onViewVersion` ... for local (non-mutating) selection") against `task-01`'s confirmed fact that `GET .../draft/versions` returns full per-version content (not just previews) surfaced a real gap: if `DraftEditorSection` stayed editable while displaying a past version's content, clicking "Guardar cambios" would silently overwrite the current draft with that old version — a de facto restore through a path `task-01`/`task-07` both went out of their way to confirm doesn't exist as an explicit endpoint. Resolved by making `DraftEditorSection` read-only whenever `isViewingHistoricalVersion` is true (§ 4, "Finding — historical preview must not become a silent restore"), keeping `onViewVersion`'s "non-mutating" description true in practice, not just by omission.
- **AI-disclosure state (`task-07` finding carried forward):** `aiDisclosureLabel` is modeled as page-hook-owned, session-only, in-memory state (§ 4) — never read from `AssessmentDraftDto`, since no `editedByTeacher`/timestamp field is persisted (`task-07`).
- **Callback naming (§11):** `onSave`, `onRegenerate`, `onViewVersion` — all action-named, no bare booleans for variant control; `isReadOnly`/`isSaving`/`isRegenerating` are plain status flags passed as props, not variant-control booleans that contradict each other (§ 3 explains why Section 1/2's submitting states are kept as separate `isX`/error pairs on the page hook instead of one shared boolean pair).
- **Route collision check:** § 5 confirms `src/app/(protected)/assessments/[id]/draft/` is a genuinely new segment — verified directly against the current `src/app/(protected)/assessments/` directory listing (only `page.tsx` and `new/` exist).
- **Anti-pattern check (§10):** § 6 — no component combines fetching + form + modal + table; both Sections' estimated sizes stay well under the 250-line/god-component threshold.
- **Correction round (code review, 2026-07-17) — 4 findings, all closed:**
  - **P1, mutation/refetch ownership split:** an earlier version had `useDraftEditorSection`/`useRegenerateSection` "wrap" `task-11`'s mutations directly, exposing only `onSaved`/`onRegenerated` callbacks on the page hook for the Sections to call back into after doing the network call themselves — conflicting with `task-11`/`task-12`'s own design, which makes the post-mutation refetch a page-level concern. Fixed: `onSave`/`onRegenerate` (calling `task-11` + reloading via `task-10`'s facade) now live directly on the page hook's own view model; Section hooks only own form/validation state and invoke whichever callback the page hook hands them — see § 1 table and § 4's "Finding — mutation/refetch ownership" note. Propagated into `task-09` (fake `onSave`/`onRegenerate` now explicitly page-hook-owned) and `task-11`/`task-12` (design notes clarify PATCH is only reachable on the current version, never a historical one, since the editor is read-only otherwise).
  - **P2, historical-read-only fix not propagated into `task-09`:** `task-09`'s steps/verification/Done Criteria now explicitly require the `isReadOnly` prop, that `onSave` cannot be called while previewing history, and that returning to the current version re-enables both.
  - **P2, zero-prior-versions fixture conflicts with the always-including-current versions contract:** `task-09`'s fixture wording changed from "empty version list" to "one-item version list containing only the current draft" throughout (Implementation Steps, Risk, Verification, Done Criteria); the hierarchy's `versions` field now explicitly documents it is never an empty array.
  - **P2, `task-11`/`task-12` still required 409 handling after `task-07`/`task-08` ruled it out:** removed all 409 assumptions from `task-11` (Technical Design, Implementation Steps, Verification, Logging, Done Criteria) and `task-12` (Objective, Technical Design, Implementation Steps, Verification, Logging, Done Criteria), replacing them with the real traced error surface (404/422/500 plus 502/503 agent errors) and an explicit note that the last-write-wins concurrency risk is a documented backend limitation, not a UI-detectable conflict.

## Done Criteria

- [x] `wireframes/draft-builder-screen-hierarchy.md` names every file, its responsibility, and Server/Client designation — see § 1 Hierarchy table.
- [x] Each of the 3 Sections has its own named hook — see § 1 table and § 3's rationale for keeping them separate.
- [x] No restore/rollback affordance appears anywhere — confirmed against `task-01`'s exhaustive endpoint listing; also surfaced and closed the more subtle "editable historical preview = silent restore" gap (see § Verification Summary above).
- [x] Post-review correction: mutation/refetch ownership centralized in the page hook, historical-read-only enforcement propagated into `task-09`, `task-09`'s version-list fixture wording made consistent with the always-current-included contract, and stale 409 handling removed from `task-11`/`task-12` — see § Verification Summary's "Correction round" bullet.
- [x] Software smoke/build/startup/connectivity checks: N/A, no runtime surface; for git-enabled tasks, this task is committed, pushed, and published in a task PR before human developer PR review, with corrections pushed to the same PR. PR #80 (`story-02-...--task-08-component-hierarchy-draft-builder-screen` → `story-02-assessment-screens-wireframes-and-data-providers`) opened, reviewed (P1 mutation/refetch ownership split + 3 P2 findings, all fixed in one correction round), re-reviewed (one residual P2 — stale Server/Client rationale sentence — fixed), re-reviewed again with no further findings, approved, and merged 2026-07-17 (merge commit `5c6df55`).
- [x] Logging/observability: N/A — no executable code, no correlation/trace/INFO/DEBUG/WARN/ERROR log levels apply.
- [x] Task test suite: N/A — the generated test-suite quality gates in this task's Generated Test Suite section are architecture-review only.
- [x] Database/ORM: N/A — static DB/ORM consistency and runtime persistence smoke checks do not apply; no database, ORM, or persistence artifact is touched.
- [x] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]` — single deliverable (the hierarchy doc); the historical-preview finding is a contract clarification for that same deliverable's own `onViewVersion` callback, not a second deliverable, matching `task-02`/`task-07`'s precedent.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

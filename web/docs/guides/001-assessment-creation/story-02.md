# 🔍 DEEPENING: Story 02 — assessment-screens-wireframes-and-data-providers

**Date:** 2026-07-21 | **Area:** unknown

## Overview
Design and build, per screen, the full pipeline defined by `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2 (objetivo de usuario → flujo → estados → wireframe → jerarquía de componentes → maqueta funcional con datos fake → validar copy → conectar API real) for the two screens that make up Story 01's scope:

1. **Intake screen** (US-010) — brief form that kicks off draft generation.
2. **Draft Builder screen** (US-011 + US-012) — a single screen with the draft editor, the regenerate action, and the version history, since the Done Criteria in `story-01` require regeneration and version history to happen "from the draft view," not as separate routes.

This story delivers the wireframes, the navigable fake-data mockups, the DTOs/view models/Screen Data Facade that talk to `api/`'s real endpoints, and the final wiring between both screens. Story 01 then builds on top of this: its component/hook implementation and test tasks consume the wireframes, mockups, and data providers delivered here instead of starting from an unspecified screen shape.

**Source stories:** `docs/02-product/user-stories/epic-02-assessment-creation/01-assessment-brief-intake.md` (US-010), `02-assessment-draft-generation.md` (US-011), `03-assessment-draft-regeneration.md` (US-012).

---

## How to use
### Component Hierarchy Intake Screen
- Write `wireframes/intake-screen-hierarchy.md` listing the Page/Section/Component/hook/schema files above, each with a one-line responsibility.
- For each file, state whether it's a Server or Client Component per `01-arquitectura-next-react.md` §7 — the Page and its children are all Client Components here (`"use client"`) since the form has interactive state and Firebase-authenticated fetch calls, which cannot run as Server Components in this project's client-driven auth model.
- Confirm the route path `src/app/(protected)/assessments/new/` doesn't collide with the existing `src/app/(protected)/assessments/page.tsx` placeholder (it doesn't — Next.js route groups treat `new/` as a distinct segment).
- --

### Functional Mockup Intake Screen
- Create `src/features/assessment-creation/schemas/briefSchema.ts` with the Zod schema for `learningGoal`, `topic`, `level`, `duration`, `language` (all required strings), matching `CreateAssessmentBriefRequest`.
- Create `src/features/assessment-creation/components/BriefForm.tsx` accepting `{ onSubmit, isSubmitting, serverError, fieldErrors }`, using `DynamicForm` (from `src/components/ds`, built in `task-14`) configured with the 5 fields (`learningGoal` as `textarea`, the other 4 as `input`), `briefSchema` as its Zod resolver, and `fieldErrors` passed straight to `DynamicForm`'s `externalErrors` prop, plus a submit `Button` disabled while `isSubmitting` and the server-error banner rendered from `serverError`.
- Create `src/features/assessment-creation/components/BriefFormSection.tsx` accepting a single `view: IntakeAssessmentPageViewModel` prop and forwarding `view.isSubmitting`, `view.serverError`, `view.fieldErrors`, and `view.handleSubmit` (as `onSubmit`) to `BriefForm`, wrapped in a semantic `<section aria-labelledby="brief-form-title">` per `03-jerarquia-de-componentes.md` §4. Do not have `BriefFormSection` call `useIntakeAssessmentPage()` itself — the Page owns that call.
- Create `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` returning `{ isSubmitting, serverError, fieldErrors, handleSubmit }` (`fieldErrors: Partial<Record<keyof BriefFormValues, string>> | null`), holding a `RemoteData`-shaped submit state (`idle | submitting | success | error`) with a **fake** submit function (`setTimeout(() => ..., 800)`) standing in for the real API call. The fake submit must simulate a server-side field-error response for one recognizable input (e.g. `topic === "trigger-field-error"`) that resolves to a fake `{ topic: "Ya existe una evaluación con este tema" }`-shaped `fieldErrors` result instead of success, so the `fieldErrors` → `DynamicForm.externalErrors` path is exercised now with fake data, not left untested until `task-06`'s real 422 response.
- Create `src/app/(protected)/assessments/new/page.tsx` calling `useShellConfig({ title: "Nueva evaluación", subtitle: "Describe el objetivo de aprendizaje" })`, calling `useIntakeAssessmentPage()`, and passing its result into `BriefFormSection` as the `view` prop.
- Write `BriefForm.test.tsx` covering: required-field validation blocks submission (per story-01's own Done Criteria), the submit button disables while submitting, and — submitting `topic: "trigger-field-error"` renders the fake field error inline via `DynamicForm`'s `externalErrors` path.

### Data Provider Intake Screen
- Add `CreateAssessmentBriefRequestDto`/`CreateAssessmentBriefResponseDto` to `src/types/assessment.ts`, matching `task-01`'s confirmed shapes exactly.
- Add `createAssessmentBrief(brief: CreateAssessmentBriefRequestDto)` to `src/lib/api/assessments.ts`, `POST`-ing to `/api/v1/assessments` via `apiClient`, throwing on non-2xx per the existing `getAssessments()` pattern in the same file.
- Add `generateAssessmentDraft(assessmentId: string)` `POST`-ing to `/api/v1/assessments/${assessmentId}/draft` via `apiClient`.
- Add `submitAssessmentBrief(brief)` calling both in sequence, distinguishing which step failed in the thrown error.
- Extend `src/lib/api/__tests__/assessments.test.ts` with tests for all three functions, including the case where step 2 fails after step 1 succeeds.
- --

### Connect Real Api Intake Screen
- Replace `useIntakeAssessmentPage`'s fake submit with a call to `submitAssessmentBrief(brief)`.
- On success, use `useRouter().push()` to navigate to `/assessments/${assessmentId}/draft` (the Draft Builder screen route from `task-08`).
- On failure, branch on the response shape/status per `task-02`'s traced evidence, not a flat 422/500 switch: `List<FieldErrorResponse>` (422 from `POST /assessments`) → map to `fieldErrors` (`Partial<Record<keyof BriefFormValues, string>>`) and return it from the hook exactly as `task-04`'s fake `trigger-field-error` case did — it flows through the already-built `BriefForm.fieldErrors` → `DynamicForm.externalErrors` path, no new UI mechanism; `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_REJECTED"}` → business-rejection message via `serverError` (banner); `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_ERROR"|"UNREACHABLE"}` (502/503) → service-unavailable message via `serverError`, distinct wording from the rejection case; anything else (400/404/500) → generic retry message via `serverError`. All translated to Spanish per `15-backend-frontend-contracts.md` §4, surfaced via the existing error-state UI from `task-04`.
- Extend `BriefForm.test.tsx`/add a hook test covering: successful submit navigates with the real `assessmentId`; the `List<FieldErrorResponse>` 422 shows per-field messages; the `AGENT_REJECTED` 422 shows the business message; a 502/503 shows the service-unavailable message; a 500 shows the generic retry message.
- Remove the `setTimeout` fake-submit code path entirely — no leftover dead code or feature flag.
- --

### Wireframe Draft Builder Screen
- Write `wireframes/draft-builder-screen.md` using the guide's §3 format, covering:
- Usuario: docente autenticado, dueño de la evaluación.
- Objetivo: revisar el draft generado por IA, editarlo, regenerarlo con notas de ajuste si no es correcto, y consultar versiones previas.
- Layout: `AppShell` protegido → Header (título de la evaluación) → Section "Editor de draft" (campos editables: title, context, instructions, objectives, deliverables, constraints) → Section "Regenerar" (input de notas de ajuste + acción) → Section "Historial de versiones" (lista de versiones previas, solo lectura).
- Acción primaria: guardar cambios editados / regenerar (son dos acciones distintas, ambas con su propio loading/error).
- Estados: loading inicial (skeleton), empty (aún no existe draft — no debería ocurrir si se llega desde `task-06`'s redirect, pero documentarlo como estado defensivo), listo (draft renderizado), guardando edición, regenerando, error de conflicto 409 (alguien más modificó el draft), error 404 (assessment no existe), error 500.

### Component Hierarchy Draft Builder Screen
- Write `wireframes/draft-builder-screen-hierarchy.md` listing every file above with its responsibility and Server/Client designation (all Client Components — interactive state + Firebase-authenticated fetch, same reasoning as `task-03`).
- Name the page-level view model shape returned by `useAssessmentDraftBuilderPage`: `{ draft: AssessmentDraftViewModel, versions: AssessmentDraftVersionViewModel[], selectedVersion: number }`.
- Confirm each Section's callback names follow the `onX` action-naming convention (`onSave`, `onRegenerate`, `onViewVersion`) per `03-jerarquia-de-componentes.md` §11 — no bare boolean props for variant control.
- --

### Functional Mockup Draft Builder Screen
- Create `toAssessmentDraftBuilderPageViewModel.ts` mapping a `GenerateAssessmentDraftResponse`-shaped draft + `GenerateAssessmentDraftResponse[]`-shaped versions into `{ draft: AssessmentDraftViewModel, versions: AssessmentDraftVersionViewModel[] }`.
- Create `DraftEditorSection.tsx` + `useDraftEditorSection.ts`: editable fields for title/context/instructions/objectives/deliverables/constraints, using RHF + Zod for local form state/validation only; `isSaving`/`fieldErrors`/`serverError`/`onSave` are props passed through from `useAssessmentDraftBuilderPage` (per `task-08`'s hierarchy — the Section hook does not own submitting/error state or call the mutation itself, even in this fake-data phase). Must accept and honor an `isReadOnly` prop: when `true` (previewing a non-current version), every field is disabled and the "Guardar cambios" button is hidden/disabled — `onSave` must not be callable in this state. This is not optional polish; without it, previewing a past version and saving becomes a silent restore, which `task-01`/`task-07`/`task-08` all confirm has no real endpoint.
- Create `RegenerateSection.tsx` + `useRegenerateSection.ts`: adjustment-notes textarea + regenerate button, owning only local textarea state/required-field validation; `isRegenerating`/`fieldError`/`agentError`/`onRegenerate` are props passed through from `useAssessmentDraftBuilderPage`, same ownership split as Section 1.
- Create `VersionHistorySection.tsx` + `useVersionHistorySection.ts`: read-only list of past versions, `onViewVersion` callback that swaps which version's fields are displayed in `DraftEditorSection` locally (no API call) **and sets `isReadOnly=true` on `DraftEditorSection` whenever the selected version is not the current one** (`task-08`'s finding) — selecting the current version again restores normal editing.
- Create `useAssessmentDraftBuilderPage.ts` with a fake dataset: at least one draft with long objectives/instructions text (edge case), a version list with 4+ entries (many-versions edge case), and separately test the single-current-version case (a one-item version list containing only the current draft — never an empty array, since `GET .../draft/versions` always includes the current version per `task-01`/`task-08`). Its `onSave`/`onRegenerate` are fake mutations for now (update the in-memory fake dataset directly, no network call) — `task-12` swaps these for the real `task-11` mutations plus a real `task-10` refetch without touching the Sections' props.
- Create `src/app/(protected)/assessments/[id]/draft/page.tsx` reading `params.id`, calling `useShellConfig`, composing the 3 Sections.

### Data Provider Draft Builder Screen
- Add `AssessmentDraftDto` to `src/types/assessment.ts`, matching `task-01`'s confirmed `GenerateAssessmentDraftResponse` shape exactly.
- Add `getAssessmentDraft(assessmentId: string)` to `src/lib/api/assessments.ts`, `GET`-ing `/api/v1/assessments/${assessmentId}/draft`.
- Add `getAssessmentDraftVersions(assessmentId: string)` `GET`-ing `/api/v1/assessments/${assessmentId}/draft/versions`, returning `AssessmentDraftDto[]`.
- Create `src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts` combining both via `Promise.all` and the `task-09` mapper.
- Write tests for `getAssessmentDraft`, `getAssessmentDraftVersions`, and the loader (including a case where one of the two parallel calls fails).
- --

### Mutations Draft Builder Screen
- Add `UpdateAssessmentDraftRequestDto` to `src/types/assessment.ts` — all fields optional, matching `task-01`'s confirmed `UpdateAssessmentDraftRequest` partial-update semantics exactly (only include keys actually being changed).
- Add `updateAssessmentDraft(assessmentId, changes)` to `src/lib/api/assessments.ts`, `PATCH`-ing `/api/v1/assessments/${assessmentId}/draft` with only the provided keys.
- Add `regenerateAssessmentDraft(assessmentId, adjustmentNotes)` `POST`-ing `/api/v1/assessments/${assessmentId}/draft/regenerate` with `{ adjustmentNotes }`.
- Write tests: `updateAssessmentDraft` sends only the changed keys (not a full object with empty-string defaults); `regenerateAssessmentDraft` sends the notes and parses the new draft; both surface 422 field/notes/agent-rejected errors and 502/503 agent-down distinctly from a generic 500, per `06-estado-datos-y-api.md` §9 and `task-07`'s traced error surface — neither surfaces a 409, since none exists for these endpoints.
- --

### Connect Real Api Draft Builder Screen
- Replace `useAssessmentDraftBuilderPage`'s fake dataset with a `loadAssessmentDraftBuilderPage(assessmentId)` call on mount, using the `RemoteData` states from `task-09`.
- Replace the page hook's fake `onSave` with a call to `updateAssessmentDraft` (from `task-11`), and on success, refetch the version list (or the full page data) via `loadAssessmentDraftBuilderPage` — `DraftEditorSection`'s own props/behavior are unchanged from `task-09`, only what the page hook's `onSave` does internally changes.
- Replace the page hook's fake `onRegenerate` with a call to `regenerateAssessmentDraft` (from `task-11`), and on success, refetch the version list and update the displayed draft to the new version — same "Section props unchanged" note as step 2.
- Map 404/422/500 (plus 502/503 agent errors) to translated messages per `15-backend-frontend-contracts.md` §4, surfaced via the existing error-state UI from `task-09`. No 409 mapping exists — `task-07`/`task-08` confirmed no draft endpoint returns one.
- Extend Section tests to cover: successful save/regenerate refreshes the version list; an agent-rejected (422) or agent-down (502/503) error during regenerate shows a clear message without clearing the current draft (`task-07`); a 404 on initial load shows a "not found" state.
- Remove the fake dataset and fake `onSave`/`onRegenerate` code paths entirely from `task-09` — no leftover dead code or feature flag.

### End To End Connection
- `grep -r` for leftover fake-data markers (`setTimeout`, hardcoded fixture objects, a stray `mocks/` directory) under `src/features/assessment-creation/`; remove or justify each hit.
- With `api/` running locally, manually walk the full flow: submit a real brief on `/assessments/new` → confirm redirect to `/assessments/{realId}/draft` → confirm the generated draft renders → edit a field and save → confirm the edit persists after a page refresh → regenerate with adjustment notes → confirm a new version appears and the version history shows the prior one.
- Confirm story-01's Done Criteria that fall within this story's scope hold against the real flow just walked (not the mockups): required-field validation blocks submission; brief persists before the agent call; draft is fully editable and edits persist via the API; regeneration works with adjustment notes; previous versions remain accessible; draft and versions survive a page refresh.
- Run the full test suite and lint once more across everything this story touched.
- --

### Design System Form Primitives
- Port `Field.jsx` → `src/components/ds/Field.tsx`: `label`, `htmlFor`, `required`, `hint`, `error`, accessible association via `aria-describedby`, styled with this project's existing token variables (`var(--text-sm)`, `var(--danger-600)`, etc., matching `Field.tsx`'s current styling approach rather than the reference kit's raw CSS string injection, which doesn't match this codebase's inline-style convention).
- Port `Textarea.jsx` → `src/components/ds/Textarea.tsx` and `Select.jsx` → `src/components/ds/Select.tsx`, both following `Input.tsx`'s existing prop conventions (`error`→ removed in favor of `Field` wrapping; keep `icon`-less since neither reference version has one).
- Create `src/components/ds/Checkbox.tsx`: port `design-system/components/forms/Checkbox.jsx`'s native `<label>`-wraps-`<input>` shape, with its own inline `label`/`description` caption and `required` marker (per PDR-001 decision item 3's amendment — the caption must stay fused with the control for correct click-to-toggle and screen-reader behavior, unlike `Input`/`Textarea`/`Select`). `Checkbox` itself takes no `error` prop — callers wrap it in `Field` (passed no `label`, so `Field` renders only the `error`/`hint` text and its accessible association below the control, never a second stacked-above label).
- Update `src/components/ds/Input.tsx`: remove its internal `error` paragraph rendering (moves to `Field`); keep `error` prop only to drive the `invalid` border-color styling.
- Create `src/components/ds/Form.tsx`: thin `<form>` wrapper handling `onSubmit`/`noValidate` (RHF forms should never rely on native HTML validation, per `07-formularios-validacion-y-feedback.md` §1) and consistent spacing between fields.
- Create `src/components/ds/DynamicForm.tsx`: accepts `FieldDefinition[]`, wraps `useForm` + optional Zod `resolver`, renders `Form` > one `Field` + matching control per definition, calls `onSubmit` with typed values on submit. Accept `externalErrors?: Partial<Record<string, string>>` and apply each entry via `methods.setError(name, { type: "server", message })` in a `useEffect` keyed on `externalErrors` — this is the only path for server-side field errors (e.g. a backend 422 result); the Zod `resolver` only ever validates client-side shape and cannot know about a network response.

### Deterministic E2e Suite
- Add `@playwright/test` as a devDependency; document the Chrome/Chromium prerequisite (this environment required the real `google-chrome-stable` channel, not bundled Chromium — see task-13's notes) as a comment in `playwright.config.ts` or the e2e script.
- Author `web/e2e/fixtures/auth.ts`: a Playwright fixture that programmatically creates and signs in a teacher via the Firebase Auth Emulator's REST API (`accounts:signUp` → admin-bypass `accounts:update` to force `emailVerified:true` → `accounts:signInWithPassword`), then registers the teacher against the real `api/` backend and seeds the browser's auth state — exposing an `authenticatedPage` fixture other specs (and future authenticated-screen tests) can import directly, without re-deriving this flow.
- Author spec files covering task-13's walkthrough legs:
- Registration UI (real form submit, not the fixture's programmatic path) — one spec exercising the actual `/register` page end to end, to keep UI regression coverage independent of the fixture shortcut.
- Login UI end to end.
- Intake submission: real brief persisted before any agent call; confirm the expected agent-down error banner when `agents/` is unreachable (matches task-06's designed error path).

## Related components
- [task-03](story-02/task-03.md)
- [task-04](story-02/task-04.md)
- [task-05](story-02/task-05.md)
- [task-06](story-02/task-06.md)
- [task-07](story-02/task-07.md)
- [task-08](story-02/task-08.md)
- [task-09](story-02/task-09.md)
- [task-10](story-02/task-10.md)
- [task-11](story-02/task-11.md)
- [task-12](story-02/task-12.md)
- [task-13](story-02/task-13.md)
- [task-14](story-02/task-14.md)
- [task-15](story-02/task-15.md)

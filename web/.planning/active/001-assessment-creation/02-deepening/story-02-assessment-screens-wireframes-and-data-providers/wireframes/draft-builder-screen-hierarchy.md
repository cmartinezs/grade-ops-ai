# Component hierarchy: Draft Builder screen

> Follows `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md` (jerarquía estándar, §9 regla de hook propio, §10 anti-pattern, §11 props) and `04-hooks-y-logica-de-ui.md` §6 (Screen Data Facade rule).
> Builds on `wireframes/draft-builder-screen.md` (task-07) — same route, 3 sections, and 12 real UI states.
> **Reuses the DS form primitives from `pdr-001-design-system-form-primitives.md`/`task-14`** (`Field`, `Input`, `Textarea`) directly — **not** `DynamicForm`. Unlike the Intake screen's linear `BriefForm`, each Section here has per-field behavior that a declarative field list doesn't fit (multi-field save, independent regenerate submitting state, version-preview toggling) — this composition choice is already fixed in `task-09`'s Technical Design and is carried into this hierarchy unchanged, not re-decided here.
> Names every file `task-09` (functional mockup) and `task-12` (real API wiring) will create. Does not implement code.

---

## 1. Hierarchy table

| Level | File | Responsibility (one line) | Server/Client |
|-------|------|---------------------------|----------------|
| Layout | `src/app/(protected)/layout.tsx` (existing, reused — not created by this story) | App shell, auth guard, shell provider — already covers this route via the `(protected)` route group. | Client (existing) |
| Page | `src/app/(protected)/assessments/[id]/draft/page.tsx` | Route entry: reads `params.id`, configures `useShellConfig`, invokes `useAssessmentDraftBuilderPage(assessmentId)`, branches on the returned `RemoteData` status (skeleton / full-screen 404-or-500 / ready), and on `ready` passes the composed view model into the 3 Sections. No fetching or markup beyond that branching. | Client — invokes hooks, no static content. |
| Section 1 | `src/features/assessment-creation/components/DraftEditorSection.tsx` | Renders the editable fields (`title`, `context`, `instructions`, `objectives[]`, `deliverables[]`, `constraints[]`) via `Field`+`Input`/`Textarea` (task-14), the AI-disclosure label, and the "Guardar cambios" button. Read-only (fields + button disabled) while a non-current version is being previewed — see §4 "Historical preview vs. edit" below. Receives the Page hook's view model slice as props; calls no API itself. | Client — user events (typing, submit), disabled-state coordination. |
| Section 1 hook | `src/features/assessment-creation/hooks/useDraftEditorSection.ts` | RHF+Zod form state seeded from the currently displayed version's content; owns `guardando`/error state (422 field errors + generic banner); `onSave` callback wraps `task-11`'s update mutation. | N/A (hook). |
| Section 2 | `src/features/assessment-creation/components/RegenerateSection.tsx` | Renders the `adjustmentNotes` `Field`+`Textarea` and "Regenerar con IA" button; shows its own error banner (agent-rejected / agent-down) independent of Section 1. | Client — user events, its own disabled/loading state. |
| Section 2 hook | `src/features/assessment-creation/hooks/useRegenerateSection.ts` | Owns `regenerando`/error state (422 empty-notes, 422 agent-rejected, 502/503 agent-down); `onRegenerate(adjustmentNotes)` callback wraps `task-11`'s regenerate mutation. Never touches Section 1's or Section 3's state directly — a successful regenerate is surfaced back to the page hook (see §4) so it can refresh `draft`/`versions`/`aiDisclosureLabel`. | N/A (hook). |
| Section 3 | `src/features/assessment-creation/components/VersionHistorySection.tsx` | Read-only list of versions (`versionNumber` + content preview), highlights the row matching `selectedVersion`, marks the latest as "(actual)". No `onRestore`. | Client — click handler per row. |
| Section 3 hook | `src/features/assessment-creation/hooks/useVersionHistorySection.ts` | Derives each row's label (`"v{n} (actual)"` vs `"v{n}"`) and truncated title/context preview from the raw versions array — satisfies §9's "deriva datos para mostrar" trigger even though the section has no local `useState`. | N/A (hook). |
| Page hook | `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` | Owns the Screen Data Facade call (`loadAssessmentDraftBuilderPage`, `task-10`) wrapped in `RemoteData<T>` (`06-estado-datos-y-api.md` §8); once `ready`, also owns `selectedVersion` + `onViewVersion` (cross-section: affects both Section 1's displayed content and Section 3's highlighted row, so it cannot live in either Section alone) and the session-only `aiDisclosureLabel` transient state (see §4). Composes the final page-level view model named in this task's Implementation Steps. | N/A (hook). |
| Mapper | `src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts` | Pure function combining the current-draft DTO + versions DTO array (both `GenerateAssessmentDraftResponse`-shaped, per `task-01`) into `{ draft: AssessmentDraftViewModel, versions: AssessmentDraftVersionViewModel[] }` — the facade's (`task-10`) output, before the page hook layers `selectedVersion`/`onViewVersion`/`aiDisclosureLabel` on top. | N/A (pure TS, no React). |

No `SubSection` is introduced — see §3. No DS primitive is created by this task; `Field`/`Input`/`Textarea`/`Button` are consumed as built by `task-14`.

---

## 2. Server/Client rationale (per `01-arquitectura-next-react.md` §7)

Every file above is a Client Component (`"use client"`), same reasoning as `wireframes/intake-screen-hierarchy.md` §2:

- The Page invokes `useShellConfig` and the page-level feature hook — both are hooks, disqualifying it from Server Component status per §7.
- All 3 Sections are rendered inside that Client Component subtree, handle user events (typing, clicking Guardar/Regenerar/a version row), and (via their hooks) trigger Firebase-authenticated fetch calls — explicit Client Component triggers per §7.
- None of the 3 Sections has a server-renderable static path independent from the interactive editor/regenerate/history behavior; the whole screen is one interactive unit beyond the already-existing `(protected)/layout.tsx`.

---

## 3. Why no SubSection, and why each Section keeps its own hook

- **No SubSection**: `03-jerarquia-de-componentes.md` §5 reserves SubSection for grouping *within* one Section when it improves reading/testability (filters, toolbar, table). Each of the 3 Sections here is already a single, indivisible concern (one form, one form, one read-only list) — nothing inside any of them needs a second internal grouping.
- **3 independent hooks, not 1 shared hook**: per this task's own Technical Design, Section 1 (save) and Section 2 (regenerate) are two distinct primary actions with independent loading/error state (confirmed by `task-07`'s wireframe: "son dos acciones distintas, ambas con su propio loading/error"). Collapsing them into one hook would force unrelated submitting/error flags to coexist in one object, exactly the "booleans that contradict" anti-pattern `03-jerarquia-de-componentes.md` §11 warns against (e.g. `isSavingDraft` and `isRegenerating` both true at once would be meaningless but representable). Section 3 gets its own hook too, even though it holds no local state, because it derives display labels from raw data (§9 "deriva datos para mostrar" trigger) — kept separate from Section 1/2's hooks since it has no submitting/error concern at all.
- **No `DynamicForm` reuse here (cross-reference, not re-decided)**: `task-09`'s Technical Design already fixed this — `DynamicForm` fits the Intake screen's single linear field list, but Section 1 needs conditional read-only-while-previewing behavior per field and Section 2 is a single field with its own submit action; forcing either through a declarative `FieldDefinition[]` config would fight the framework instead of using it. Both Sections still use the shared `Field`/`Input`/`Textarea` MicroComponents from `task-14` — only the declarative `DynamicForm` orchestration layer is skipped, not the primitives themselves.

---

## 4. Interfaces / contracts

```ts
// useAssessmentDraftBuilderPage.ts — page hook return shape
type AssessmentDraftBuilderPageState = RemoteData<AssessmentDraftBuilderPageViewModel>;

interface AssessmentDraftBuilderPageViewModel {
  draft: AssessmentDraftViewModel;               // content currently displayed — defaults to the current version, swapped by onViewVersion
  versions: AssessmentDraftVersionViewModel[];   // full list, current version included (per task-01's confirmed GET .../draft/versions shape)
  selectedVersion: number;                       // versionNumber currently displayed in `draft`
  isViewingHistoricalVersion: boolean;           // selectedVersion !== the latest entry in `versions` — derived, not stored twice
  aiDisclosureLabel: "generado-por-ia" | "version-actual"; // session-only, in-memory (see finding below); never read from the API
  onViewVersion: (versionNumber: number) => void; // local, non-mutating selection — no network call, no onRestore
  onSaved: () => void;                            // called by Section 1 after a successful save; resets aiDisclosureLabel to "version-actual" (task-07's disclosure finding) and refreshes selectedVersion bookkeeping
  onRegenerated: (updated: AssessmentDraftViewModel) => void; // called by Section 2 after a successful regenerate; sets aiDisclosureLabel back to "generado-por-ia" and appends the new version
}

// DraftEditorSection.tsx
interface DraftEditorSectionProps {
  draft: AssessmentDraftViewModel;
  aiDisclosureLabel: "generado-por-ia" | "version-actual";
  isReadOnly: boolean;        // = isViewingHistoricalVersion — disables all fields + hides "Guardar cambios" (see finding below)
  isSaving: boolean;
  fieldErrors: Partial<Record<DraftEditableField, string>> | null; // 422 List<FieldErrorResponse>
  serverError: UiError | null;                                     // 500 / no-prior-draft 422
  onSave: (values: DraftEditableFields) => void;
}

// RegenerateSection.tsx
interface RegenerateSectionProps {
  isRegenerating: boolean;
  fieldError: string | null;   // 422 adjustmentNotes empty
  agentError: UiError | null;  // 422 agent-rejected / 502-503 agent-down — draft is never cleared on this path (task-07)
  onRegenerate: (adjustmentNotes: string) => void;
}

// VersionHistorySection.tsx
interface VersionHistorySectionProps {
  versions: AssessmentDraftVersionViewModel[];
  selectedVersion: number;
  onViewVersion: (versionNumber: number) => void; // no onRestore — task-01's confirmed contract has no 8th endpoint
}

interface AssessmentDraftVersionViewModel {
  versionNumber: number;
  isCurrent: boolean;      // true for the latest entry
  previewLabel: string;    // "v{n}" or "v{n} (actual)" — derived by useVersionHistorySection
  titlePreview: string;    // truncated title, no createdAt available (task-01/task-07 finding — GenerateAssessmentDraftResponse has no timestamp)
}
```

**Finding — historical preview must not become a silent restore:** this task's own Technical Design states `onViewVersion` is for "local (non-mutating) selection," and `task-07`'s wireframe explicitly rules out any restore action. But `GET .../draft/versions` returns full content per version (not just a preview, per `task-01`) — so if `onViewVersion` simply swapped `DraftEditorSection`'s editable field values to a past version's content *while leaving "Guardar cambios" enabled*, saving would silently overwrite the current draft with the old version's content: a restore action in every practical sense, achieved through a UI path this project has twice (`task-01`, `task-07`) gone out of its way to confirm does not exist. Resolved here by making `DraftEditorSection` read-only whenever `isViewingHistoricalVersion` is true — viewing history is a pure preview; editing/saving is only possible once selection returns to the current version. This keeps `onViewVersion`'s own "non-mutating" description accurate instead of only true by omission.

**AI-disclosure state ownership (carries `task-07`'s finding forward):** `aiDisclosureLabel` is UI-only, in-memory state living in the page hook — never derived from an API field, since `AssessmentDraft.applyEdit()` persists no `editedByTeacher`/timestamp marker (`task-07`). It starts as `"generado-por-ia"` immediately after a successful generate/regenerate, and flips to `"version-actual"` after any successful save or on a fresh page load (the facade's `loadAssessmentDraftBuilderPage` has no way to know either way, so a reload always starts neutral).

---

## 5. Route collision check

`src/app/(protected)/assessments/[id]/draft/` does not collide with existing routes — confirmed by listing `src/app/(protected)/assessments/`: only `page.tsx` (list) and `new/page.tsx` (task-03/04) exist today, no `[id]/` segment yet. `[id]` is a fresh dynamic segment this task's Page introduces.

---

## 6. Anti-pattern check (§10)

No component here combines fetching + form + modal + table + styles + mappings:

- `DraftEditorSection`/`RegenerateSection` each own exactly one form concern and delegate fetching to their own hook (which calls `task-11`'s mutation functions, not `lib/api`/Firebase directly).
- `VersionHistorySection` has no form, no fetching, no mutation — a plain derived list.
- The Page has 0 local effects and 0 handlers of its own (all delegated to the page hook); it only branches on `RemoteData` status and composes 3 Sections — well under the "página > 150 líneas / >5 estados / >3 efectos / >4 handlers" thresholds (`01-arquitectura-next-react.md` §11).
- Estimated sizes: `DraftEditorSection.tsx` (6 fields via `Field`/`Input`/`Textarea` + 1 button + read-only branch) and `RegenerateSection.tsx` (1 field + 1 button + 1 banner) both stay well under the 250-line god-component threshold; `VersionHistorySection.tsx` is a single mapped list with no branching beyond the highlighted row.

---

> [← story file](../story-02-assessment-screens-wireframes-and-data-providers.md)

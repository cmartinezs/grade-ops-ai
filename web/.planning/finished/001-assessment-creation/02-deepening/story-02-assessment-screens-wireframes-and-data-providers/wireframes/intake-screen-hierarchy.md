# Component hierarchy: Intake screen

> Follows `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md` (jerarquía estándar, §9 regla de hook propio, §10 anti-pattern, §11 props) and `01-arquitectura-next-react.md` §5/§7 (features/ folder, Server/Client Component rule).
> Builds on `wireframes/intake-screen.md` (task-02) — same route, fields, and 8 UI states.
> **Form architecture governed by `../../../pdr-001-design-system-form-primitives.md`** (accepted during human review of this task, 2026-07-15): `BriefForm` is built from shared DS primitives (`Field`, `Input`, `Textarea`, `DynamicForm`), not screen-specific field markup. Building those primitives is **out of scope for this task** — split into `task-14-design-system-form-primitives.md`, which must run before `task-04` (functional mockup) can implement `BriefForm` for real.
> Names every file `task-04` (functional mockup) and `task-06` (real API wiring) will create. Does not implement code.

---

## 1. Hierarchy table

| Level | File | Responsibility (one line) | Server/Client |
|-------|------|---------------------------|----------------|
| Layout | `src/app/(protected)/layout.tsx` (existing, reused — not created by this story) | App shell, auth guard, shell provider — already covers this route via the `(protected)` route group. | Client (existing) |
| Page | `src/app/(protected)/assessments/new/page.tsx` | Route entry: configures shell title/subtitle via `useShellConfig`, invokes `useIntakeAssessmentPage`, passes the returned view model into `BriefFormSection` as a `view` prop, no other markup. | Client — invokes hooks (`useIntakeAssessmentPage`, `useShellConfig`), no static content. |
| Section | `src/features/assessment-creation/components/BriefFormSection.tsx` | Semantic wrapper (`<section aria-labelledby>`) around the intake form — the screen's single business block; holds no logic of its own. Receives the Page hook's view model as a prop (`view: IntakeAssessmentPageViewModel`) and forwards its fields to `BriefForm` — it does not call the hook itself. | Client — renders a Client Component child (`BriefForm`) and needs no state itself, but stays under the same `"use client"` boundary as its parent Page since it takes no server-renderable static path. |
| Component | `src/features/assessment-creation/components/BriefForm.tsx` | **Thin wrapper, not a hand-built form.** Configures the shared DS `DynamicForm` with this screen's `FieldDefinition[]` (5 entries: `learningGoal` as `textarea`, the other 4 as `input`) and `briefSchema`; adds the submit `Button` and server-error banner around it. No field, label, or error markup is written here — that lives in `Field`/`DynamicForm` (`task-14`). | Client — uses `DynamicForm` (which owns `useForm`), user events, and disabled/loading visual states. |
| Page hook | `src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` | Owns the two-step sequential mutation (`createAssessmentBrief` → `generateAssessmentDraft`), submitting/error state, field-error mapping, success navigation; returns the view model `BriefFormSection`/`BriefForm` consume. | N/A (hook, not a component) — runs only in Client Components per Next.js hook rules. |
| Schema | `src/features/assessment-creation/schemas/briefSchema.ts` | Zod schema (`briefSchema`) for the 5 required fields, passed to `DynamicForm` as its resolver. | N/A (pure TS, no React). |
| DS primitive (**not built by this task** — see `task-14`) | `src/components/ds/DynamicForm.tsx` | Config-driven form renderer: takes `FieldDefinition[]` + a resolver + `externalErrors` (server-side field errors, applied via RHF's `setError`), renders `Field` + the matching control per entry, wraps `useForm` internally. `BriefForm` is its first real consumer. | Client (per `task-14`). |
| DS primitive (**not built by this task** — see `task-14`) | `src/components/ds/Field.tsx` | Centralizes `label`/`required`/`error`/`hint` and their accessible association — the *only* place any of the 5 fields' labels/errors render. Rewritten by `task-14` from its current label-only shape. | Client (per `task-14`). |
| DS primitive (existing, adjusted by `task-14`) | `src/components/ds/Input.tsx` | Renders `topic`, `level`, `duration`, `language` (4 of the 5 fields) via `DynamicForm`. `task-14` removes its current internal error-text rendering (moves to `Field`); `BriefForm` never references `Input` directly. | Client (existing). |
| DS primitive (**not built by this task** — see `task-14`) | `src/components/ds/Textarea.tsx` | Renders `learningGoal` (multi-line) via `DynamicForm`. No `Textarea` exists in the DS yet (checked against `src/components/ds/index.ts`) — built once, by `task-14`, for every future multi-line field, not per screen. | Client (per `task-14`). |
| MicroComponent (existing, reused) | `src/components/ds/Button.tsx` | Submit button; its `loading` prop internally renders `Spinner` (`Button.tsx:18,108`) — `BriefForm` never touches `Spinner` directly. | Client (existing). |

No `SubSection` or specialized/derived `MiniComponent` (e.g. a per-screen `EmailInput`-style wrapper) is introduced — see §3. No per-field `Input`/`Textarea` composition happens inside `BriefForm` either — that's exactly the duplication `pdr-001-design-system-form-primitives.md` rules out; `BriefForm` only supplies configuration to the shared `DynamicForm`.

---

## 2. Server/Client rationale (per `01-arquitectura-next-react.md` §7)

All React files above are TSX per §2's mandatory rule (Page, Section, Component are all `.tsx`; this is independent of Client/Server status — Server Components are TSX too). By §7's actual Client/Server criteria (hooks, browser APIs, user events — not "renders JSX"), every one of them is a Client Component (`"use client"`):

- The Page invokes `useShellConfig` (a hook) and a feature hook — both disqualify it from being a Server Component per §7's "usa hooks" rule.
- `BriefFormSection` and `BriefForm` are rendered inside that Client Component subtree; `BriefForm` additionally renders `DynamicForm` (which itself uses `useForm`), handles user events (typing, submit) through it, and triggers Firebase-authenticated fetch calls from its submit handler — all explicit Client Component triggers per §7.
- None of these files render static content, read no browser API directly (that's confined to `lib/api`/`lib/firebase`), and none can be deferred to a Server Component boundary, since the whole screen is one interactive form with no server-renderable static shell beyond the already-existing `(protected)/layout.tsx`.

## 3. Why no SubSection, no per-field duplication, and no *specialized derived* MiniComponent input

Two layers govern this, at different scopes:

- **`03-jerarquia-de-componentes.md` §8.1** (narrow, per-field question): a base `Input`/`Textarea` is a `MicroComponent` as long as it stays parameterizable via native attrs (`required`, `minLength`, `maxLength`, `pattern`); a *specialized* `MiniComponent` (e.g. `EmailInput`) is only worth extracting when a specific validation shape (regex, fixed `type`) repeats across more than one screen.
- **`pdr-001-design-system-form-primitives.md`** (broad, cross-cutting): governs the *whole form*, not just individual fields — no screen may compose `Field`/`Input`/`Textarea` ad hoc when the form is linear; it must go through `DynamicForm` instead. An earlier version of this section had `BriefForm` hand-compose each field directly, correct at §8.1's narrower scope but wrong at the PDR's broader one — corrected during human review, see below.

- **SubSection**: the guide reserves this for grouping *within* a section (filters, toolbar, table) when it "improves reading or testability" (§5). `BriefFormSection` has exactly one internal grouping — the form itself — so a SubSection would wrap `BriefForm` with nothing left to differentiate; skipped.
- **Specialized/derived MiniComponent input** (§8.1 scope): all 5 fields (`learningGoal`, `topic`, `level`, `duration`, `language`) are plain required strings with no fixed format — `CreateAssessmentBriefRequest.java` declares every field `@NotBlank String`, no `@Size`/pattern, no enum. None needs a regex, a fixed `type` (email/tel/url), or any validation shape likely to repeat elsewhere. Building an `EmailInput`-style wrapper here would be a premature abstraction with no second caller — skipped. This also resolves the "texto corto (o select, TBD)" note left open in `task-02`'s wireframe: `level` is plain text (backend has no enum for it), not a select.
- **Server-error banner**: the only MiniComponent candidate is the banner for agent-rejected/agent-down/unexpected errors (see `wireframes/intake-screen.md` § Estados). The existing precedent for the same shape (`src/app/login/page.tsx`) renders it inline as `<div role="alert">`, not as an extracted component, and it meets none of §7's extraction triggers (not reused elsewhere yet, no own tests planned, doesn't distract from `BriefForm`'s markup at this size) — kept inline in `BriefForm`, outside `DynamicForm`'s own rendering.
- **No per-field composition in `BriefForm` itself (PDR-001 scope, corrected during review):** an earlier version of this section had `BriefForm` render `Input`/`Textarea` directly per field, with `Field` wrapping each one's label/error individually — functionally fine for this one screen, but exactly the "repeat the reuse decision per form" pattern `pdr-001-design-system-form-primitives.md` rules out project-wide, given this repo already shows that pattern drifting twice (duplicated email validation in `login`/`register`; `Field.tsx`/`Input.tsx` splitting label/error inconsistently). `BriefForm` now only supplies `FieldDefinition[]` + `briefSchema` to the shared `DynamicForm` (§1 table) — it contains zero field-level markup.

**Guide update (§8.1, still valid, narrower scope):** `03-jerarquia-de-componentes.md` did not previously distinguish "parameterizable base input" from "specialized derived input" — added §8.1 with the real, pre-existing duplicate email-validation pattern in `login`/`register` as the motivating example (out of scope to refactor here).

**Cross-cutting decision (PDR-001, broader scope, spun into its own task):** the DS-wide `Form`/`Field`/`DynamicForm` architecture is decided in `pdr-001-design-system-form-primitives.md` and implemented in `task-14-design-system-form-primitives.md`, not in this task or this doc. This task only documents the target shape `BriefForm` must be built against once `task-14` is DONE.

---

## 4. Interfaces / contracts

```ts
// BriefForm.tsx
interface BriefFormProps {
  onSubmit: (values: BriefFormValues) => void;
  isSubmitting: boolean;
  serverError: UiError | null; // banner-level error only (agent-rejected/agent-down/unexpected) — see wireframes/intake-screen.md § Estados
  fieldErrors: Partial<Record<keyof BriefFormValues, string>> | null; // backend 422 List<FieldErrorResponse> results, mapped by useIntakeAssessmentPage — passed straight to DynamicForm's externalErrors prop (task-14)
}

// BriefForm.tsx internals — the FieldDefinition[] passed to DynamicForm (src/components/ds, task-14)
const briefFormFields: FieldDefinition[] = [
  { name: "learningGoal", label: "Objetivo de aprendizaje", control: "textarea", required: true },
  { name: "topic", label: "Tema", control: "input", required: true },
  { name: "level", label: "Nivel", control: "input", required: true },
  { name: "duration", label: "Duración", control: "input", required: true },
  { name: "language", label: "Lenguaje", control: "input", required: true },
];
```

- `BriefForm` itself never calls `useForm` — that lives inside `DynamicForm` (`task-14`). `BriefForm`'s only job is to supply `briefFormFields` + `briefSchema` + `fieldErrors` to `DynamicForm` (via its `externalErrors` prop) and wrap it with the submit `Button` and server-error banner.
- **Server-side field errors have an explicit path (fixes a gap found in code review):** `briefSchema`'s Zod resolver only validates client-side shape (required/non-blank) — it has no way to know about a backend 422 `List<FieldErrorResponse>` result, since that only exists after the network call returns. The actual path is: `useIntakeAssessmentPage.handleSubmit` catches the 422, maps `List<FieldErrorResponse>` to `Partial<Record<keyof BriefFormValues, string>>` (this is what the hook's already-stated "field-error mapping" responsibility, §1 table, produces) → returned as `fieldErrors` in `IntakeAssessmentPageViewModel` → threaded through `BriefFormSection` → `BriefForm`'s `fieldErrors` prop → `DynamicForm`'s `externalErrors` prop (`task-14`, which applies them via RHF's `setError(name, { type: "server", message })`). This is why `task-04`'s section of `task-01`'s and `task-02`'s documented 422 field-validation shape must not be silently dropped — it now has a concrete destination.
- Naming follows §11 of `03-jerarquia-de-componentes.md`: no `onApprove`/`onRetry`-style callback needed here (single action), but `onSubmit` matches the "named by action" rule and avoids the generic `onChange`/`onClick`.
- **`BriefFormSection` is not prop-less (fixes a contradiction found in code review):** the Page calls `useIntakeAssessmentPage()` and passes the returned view model into `BriefFormSection` as a single `view: IntakeAssessmentPageViewModel` prop; `BriefFormSection` forwards `view.isSubmitting`, `view.serverError`, `view.fieldErrors`, and `view.handleSubmit` (as `onSubmit`) to `BriefForm`. There is no implicit data flow in React — the earlier "no props, implicit" wording was wrong and is corrected here.
- `useIntakeAssessmentPage()` returns a view model shaped like:
  ```ts
  interface IntakeAssessmentPageViewModel {
    isSubmitting: boolean;
    serverError: UiError | null;
    fieldErrors: Partial<Record<keyof BriefFormValues, string>> | null;
    handleSubmit: (values: BriefFormValues) => void; // wraps createAssessmentBrief → generateAssessmentDraft, then navigates
  }
  ```
  This is a single-source hook (§7 of `04-hooks-y-logica-de-ui.md`: "una pantalla simple puede llamar un servicio API desde su hook") — the Intake screen has no GET-on-load, so no Screen Data Facade/loader is needed here (that pattern is reserved for the Draft Builder screen in `task-08`/`task-10`, which does have 2+ remote sources). The two sequential POSTs are a **mutation**, orchestrated per `06-estado-datos-y-api.md` §10, not a page-load facade.
- `briefSchema` exports `type BriefFormValues = z.infer<typeof briefSchema>` — the single source of truth for the 5 field names/types, consumed by both `useForm`'s generic parameter (inside `DynamicForm`) and `IntakeAssessmentPageViewModel.handleSubmit`/`fieldErrors`.

---

## 5. Route collision check

`src/app/(protected)/assessments/new/` does not collide with the existing `src/app/(protected)/assessments/page.tsx` placeholder — Next.js App Router treats `new/` as a distinct path segment (`/assessments/new`) from the parent segment's own `page.tsx` (`/assessments`). Confirmed by inspecting `src/app/(protected)/assessments/` directly: only `page.tsx` exists there today, no `new/` subfolder, so no naming conflict at creation time either.

---

## 6. Anti-pattern check (§10)

No component in this hierarchy combines fetching + form + modal + table + styles + mappings. `BriefForm` configures `DynamicForm` and renders the submit `Button` + banner only — it contains no field markup and no fetching of its own; all fetching/orchestration lives in `useIntakeAssessmentPage`, and all per-field rendering/RHF state lives in `DynamicForm`/`Field` (`task-14`). Estimated `BriefForm.tsx` size: one field-definition array + 1 button + 1 conditional banner — well under the 250-line/god-component threshold, and under the "página > 150 líneas / >5 estados / >3 efectos / >4 handlers" growth-rule thresholds in `01-arquitectura-next-react.md` §11 (this screen has exactly 1 handler — submit — and 0 local effects, since `DynamicForm` owns form state and the mutation is a plain async call, not an effect).

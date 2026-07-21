# ADR: Implement the Design System form primitives decided in `pdr-001-design-system-form-primitives.md`: `Form`, `Field`, `Input`, `Textarea`, `Select`, `Checkbox`, and a declarative `DynamicForm`/`FormRenderer` — so that `BriefForm` (`task-04`) and the Draft Builder screen's forms (`task-09`) are built from a single shared contract instead of each hand-rolling labels/required-markers/error text, which this planning already found duplicated twice (`src/app/login/page.tsx` / `src/app/register/page.tsx` inline email validation; `Field.tsx` vs.

**Date:** 2026-07-16
**Status:** Accepted
**Planning:** 001-assessment-creation / story-02 / task-14

## Context
- **Approach:** Migrate the `design-system/components/forms/{Field,Input,Textarea,Select,Checkbox}.jsx` reference kit into `src/components/ds/` as TSX (per `01-arquitectura-next-react.md` §2 — JSX reference material must become typed TSX before production use, not be copied as-is), extending each with the props this project's real usage needs (RHF `register` spread, `error`/`hint`/`required` centralized in `Field`, with `Checkbox`'s own inline caption carved out per PDR-001 decision item 3's amendment). Add `Form` (thin semantic `<form>` wrapper: submit handling plumbing, no field knowledge) and `DynamicForm` (interprets a `FieldDefinition[]` config, renders one `Field`+control per entry) as new components with no existing reference to migrate from.
- **Affected files / components:** New: `src/components/ds/Textarea.tsx`, `src/components/ds/Select.tsx`, `src/components/ds/Checkbox.tsx`, `src/components/ds/Form.tsx`, `src/components/ds/DynamicForm.tsx`, `src/components/ds/__tests__/Field.test.tsx`, `src/components/ds/__tests__/DynamicForm.test.tsx`. Modified: `src/components/ds/Field.tsx`, `src/components/ds/Input.tsx`, `src/components/ds/index.ts`.

  Per-file detail:
  - `src/components/ds/Field.tsx` — rewritten to match the reference kit's label/required/hint/error contract (current version is label-only).
  - `src/components/ds/Textarea.tsx` — new, ports the reference kit's Textarea.
  - `src/components/ds/Select.tsx` — new, ports the reference kit's Select.
  - `src/components/ds/Checkbox.tsx` — new, ports the reference kit's inline-caption/native-label-wrapping shape; wrapped by Field for error/hint only, per PDR-001 decision item 3's amendment.
  - `src/components/ds/Form.tsx` — new, semantic container, no reference kit source.
  - `src/components/ds/DynamicForm.tsx` — new, declarative field-list renderer, no reference kit source.
  - `src/components/ds/Input.tsx` — adjust error handling to delegate to Field instead of rendering its own error text, once Field centralizes it — avoid the current double-responsibility split.
  - `src/components/ds/index.ts` — export all of the above.
  - Test files for each new/changed component under `src/components/ds/__tests__/`.
- **Interfaces / contracts:**
  - `FieldProps { label?, htmlFor, required?, hint?, error?, children }` — centralizes accessible association (`aria-describedby` for hint/error, `htmlFor`↔control `id`) and visual structure; concrete controls no longer render their own error text. `label` is optional specifically so `Checkbox` callers can omit it (its caption is part of `Checkbox` itself, per PDR-001 decision item 3's amendment) while still getting `error`/`hint` from `Field`.
  - `DynamicForm` config shape: `FieldDefinition = { name: string; label: string; control: "input" | "textarea" | "select" | "checkbox"; required?: boolean; placeholder?: string; options?: { value: string; label: string }[] /* select only */ }`; `DynamicFormProps<T> { fields: FieldDefinition[]; onSubmit: (values: T) => void; resolver?: Resolver<T>; externalErrors?: Partial<Record<keyof T, string>> }` — wraps RHF `useForm` internally, renders `Field` + the matching control per entry. `externalErrors` is the path for server-side field errors (e.g. a 422 `List<FieldErrorResponse>` result the resolver has no way to know about, since it only validates client-side shape before the network call happens): a `useEffect` calls `methods.setError(name, { type: "server", message })` for each entry whenever `externalErrors` changes. RHF only auto-revalidates a field once it has already failed through the resolver — a `setError`'d server error is not on that path, so `DynamicForm` wraps each control's `onChange` to call `clearErrors(name)` itself once the user edits a field currently holding a `type: "server"` error (confirmed empirically during this task's own implementation: relying on implicit resolver revalidation alone left the error in place). Found missing in code review of `task-03`'s hierarchy doc, which had incorrectly attributed server-field-error display to the resolver.
  - `Form` is a plain semantic wrapper (`<form onSubmit={...}>` + layout), never a synonym for `DynamicForm` — per PDR-001 decision item 6, `Form` has no knowledge of field configuration.
- **Risk:** Medium — this is foundational, shared code: a defect here affects every consumer (`task-04`, `task-09`, and beyond). Mitigated by porting the reference kit's already-reviewed CSS/behavior rather than inventing new visual behavior, and by keeping `DynamicForm` optional (per PDR-001 decision item 5) so `task-09`'s more complex, non-linear Draft Builder sections aren't forced through it.
- **Design notes:** `BriefForm` (`task-04`) is expected to use `DynamicForm` since its 5 fields are linear with no conditional logic (per PDR-001 decision item 7) — confirm that shape works for it as part of this task's own verification, not deferred silently to `task-04`. The Draft Builder screen's `DraftEditorSection`/`RegenerateSection` (`task-09`) are expected to compose `Field`/`Input`/`Textarea` directly rather than through `DynamicForm`, since they have per-field custom behavior (regenerate submitting state, version-switching) that a declarative list doesn't fit.

---

## Decision
- `src/components/ds/Input.tsx` — adjust error handling to delegate to Field instead of rendering its own error text, once Field centralizes it — avoid the current double-responsibility split.

## Consequences
** `BriefForm` (`task-04`) is expected to use `DynamicForm` since its 5 fields are linear with no conditional logic (per PDR-001 decision item 7) — confirm that shape works for it as part of this task's own verification, not deferred silently to `task-04`. The Draft Builder screen's `DraftEditorSection`/`RegenerateSection` (`task-09`) are expected to compose `Field`/`Input`/`Textarea` directly rather than through `DynamicForm`, since they have per-field custom behavior (regenerate submitting state, version-switching) that a declarative list doesn't fit.

## Alternatives Considered
- `src/components/ds/Input.tsx` — adjust error handling to delegate to Field instead of rendering its own error text, once Field centralizes it — avoid the current double-responsibility split.

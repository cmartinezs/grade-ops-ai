# Design System Form Primitives

**Source:** task-14 | **Area:** unknown | **Date:** 2026-07-16

## What it does
Implement the Design System form primitives decided in `pdr-001-design-system-form-primitives.md`: `Form`, `Field`, `Input`, `Textarea`, `Select`, `Checkbox`, and a declarative `DynamicForm`/`FormRenderer` — so that `BriefForm` (`task-04`) and the Draft Builder screen's forms (`task-09`) are built from a single shared contract instead of each hand-rolling labels/required-markers/error text, which this planning already found duplicated twice (`src/app/login/page.tsx` / `src/app/register/page.tsx` inline email validation; `Field.tsx` vs. `Input.tsx` splitting label/error inconsistently).

---

## How to use it
- Port `Field.jsx` → `src/components/ds/Field.tsx`: `label`, `htmlFor`, `required`, `hint`, `error`, accessible association via `aria-describedby`, styled with this project's existing token variables (`var(--text-sm)`, `var(--danger-600)`, etc., matching `Field.tsx`'s current styling approach rather than the reference kit's raw CSS string injection, which doesn't match this codebase's inline-style convention).
- Port `Textarea.jsx` → `src/components/ds/Textarea.tsx` and `Select.jsx` → `src/components/ds/Select.tsx`, both following `Input.tsx`'s existing prop conventions (`error`→ removed in favor of `Field` wrapping; keep `icon`-less since neither reference version has one).
- Create `src/components/ds/Checkbox.tsx`: port `design-system/components/forms/Checkbox.jsx`'s native `<label>`-wraps-`<input>` shape, with its own inline `label`/`description` caption and `required` marker (per PDR-001 decision item 3's amendment — the caption must stay fused with the control for correct click-to-toggle and screen-reader behavior, unlike `Input`/`Textarea`/`Select`). `Checkbox` itself takes no `error` prop — callers wrap it in `Field` (passed no `label`, so `Field` renders only the `error`/`hint` text and its accessible association below the control, never a second stacked-above label).
- Update `src/components/ds/Input.tsx`: remove its internal `error` paragraph rendering (moves to `Field`); keep `error` prop only to drive the `invalid` border-color styling.
- Create `src/components/ds/Form.tsx`: thin `<form>` wrapper handling `onSubmit`/`noValidate` (RHF forms should never rely on native HTML validation, per `07-formularios-validacion-y-feedback.md` §1) and consistent spacing between fields.
- Create `src/components/ds/DynamicForm.tsx`: accepts `FieldDefinition[]`, wraps `useForm` + optional Zod `resolver`, renders `Form` > one `Field` + matching control per definition, calls `onSubmit` with typed values on submit. Accept `externalErrors?: Partial<Record<string, string>>` and apply each entry via `methods.setError(name, { type: "server", message })` in a `useEffect` keyed on `externalErrors` — this is the only path for server-side field errors (e.g. a backend 422 result); the Zod `resolver` only ever validates client-side shape and cannot know about a network response.

## Example
Use `Port` through the public interface introduced by this task.

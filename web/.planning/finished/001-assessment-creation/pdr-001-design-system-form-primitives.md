# PDR-001: Design System form primitives — Form, Field, DynamicForm

> **Status:** Accepted
> [← planning/README.md](README.md)

---

## Context

While drafting `story-02/task-03`'s component hierarchy for the Intake screen, the first pass proposed a screen-specific `BriefForm` that would hand-roll its own labels, required markers, and error rendering — the same shape already duplicated ad hoc elsewhere in `web/`: `src/app/login/page.tsx` and `src/app/register/page.tsx` both inline the identical `type="email"` + `z.string().email(...)` validation, and the current DS splits label (`Field.tsx`, label-only) from error rendering (`Input.tsx`, renders its own error text) inconsistently. The Intake screen is the first of several forms this planning will need (Draft Builder's regenerate notes, and future rubric/question-bank screens beyond it), so this reuse rule needs to be decided once, for the Design System, not re-invented per screen. Human review of task-03 (2026-07-15) required this be resolved as a cross-cutting decision before any screen-specific form is built.

---

## Decision

1. Visually equivalent UI elements reuse **one** parametrizable component — e.g. a single `Badge` configured via `variant`, `size`, `icon`, and content — never a copy per screen.
2. The Design System must provide reusable form primitives: `Form`, `Field`, `Input`, `Textarea`, `Select`, `Checkbox`, and related error-message components.
3. `Field` centralizes `label`, `required`, `error`, `hint`, and the accessible association between them (`htmlFor`/`aria-describedby`) and their visual structure. Concrete controls (`Input`, `Textarea`, `Select`, `Checkbox`) reuse `Field` as their wrapper instead of each reimplementing labels, errors, and hints individually.
   - **Narrow exception — `Checkbox`'s inline caption:** unlike `Input`/`Textarea`/`Select`, a checkbox's caption must be spatially and semantically fused with its control (native `<label>` wrapping the `<input>`, so clicking the caption toggles the box and screen readers announce them as one unit) — `Field`'s label-above/control-below layout would break that. `Checkbox` therefore renders its own inline `label`/`description` caption and `required` marker as part of the control itself (matching `design-system/components/forms/Checkbox.jsx`'s existing reference shape), while still passing through `Field` for `error`/`hint` text and their accessible association below the control — `Field` never renders a *second*, stacked-above label for `Checkbox`, and `Checkbox` never renders its own error/hint text. This is the only control-specific exception to this decision item; `Input`, `Textarea`, and `Select` have no such carve-out.
4. A separate declarative component, `DynamicForm` (a.k.a. `FormRenderer`), accepts a list of field definitions and renders a simple, linear form from that configuration.
5. Declarative configuration is not mandatory for every form. Complex or non-linear forms (conditional fields, multi-step, specialized behavior) may compose the same primitives (`Field`, `Input`, `Textarea`, `Select`, `Checkbox`) directly instead of going through `DynamicForm`.
6. `Form` and `DynamicForm` are distinct concepts: `Form` is the semantic container (submit wiring, layout), while `DynamicForm` is one specific way to populate a `Form` from a config list — not a synonym for it.
7. `BriefForm` (Intake screen, `story-02/task-04` onward) must be built from these primitives, and may use `DynamicForm` specifically because its structure stays linear (5 independent required fields, no conditional logic, no multi-step) with no specialized behavior.

> **Amendment (2026-07-15, code review of `task-03`):** decision item 3's `Checkbox` carve-out above was added after review found `task-14-design-system-form-primitives.md` internally inconsistent with the original wording (it told `Checkbox` to render its own label/error inline, contradicting "no control reimplements labels/errors"). The fix keeps `Field` as the sole owner of error/hint for every control, including `Checkbox`, and narrows the exception to the caption only, for the accessibility reason stated in item 3.

---

## Rationale

Ad hoc, per-screen form components fragment fast without a shared contract — this repo already shows it twice (duplicated email validation in `login`/`register`; `Field.tsx` and `Input.tsx` splitting label/error responsibilities inconsistently). The `design-system/` reference kit (`design-system/components/forms/{Field,Input,Textarea,Select}.jsx`) already models the right split — `Field` owns label/required/hint/error, dumb controls take an `invalid` boolean and forward native props — but stops short of a declarative layer. Extending that reference split with `DynamicForm` gives every future simple form (the majority, per this project's own screens: brief intake, regenerate notes, future rubric/question fields) one path to build from without hand-rolling markup, while explicitly keeping direct composition available so `DynamicForm` is never forced onto a form shape it doesn't fit (§ Alternatives).

---

## Alternatives Considered

| Option | Why Rejected |
|--------|-------------|
| Keep building form-specific components per screen (`BriefForm`, future `RubricForm`, etc.), each with its own field markup | Already the confirmed source of drift in this repo (duplicated inline email validation in `login`/`register`) — the exact problem a shared Design System exists to prevent. |
| A single `SuperField` component with a `type` prop switching internally between text/textarea/select/checkbox | Conflates `Field`'s cross-cutting concerns (label/required/error/hint) with control-specific rendering; harder to test or extend per the "parameterizable base vs. specialized derived" pattern already documented in `03-jerarquia-de-componentes.md` §8.1; breaks composition the moment a form needs a genuinely custom control. |
| Only build `DynamicForm` (config-driven), with no direct-composition path | Forces every form — including non-linear ones like Draft Builder (editor + regenerate + version history) — into a declarative shape they don't fit. Direct composition with the same primitives must stay available for exactly that case. |

---

## Consequences

### Positive

- One validation/error/accessibility contract (`Field`) across every present and future form in `web/`, instead of per-screen reinvention.
- `BriefForm` (and later the Draft Builder mockup) gets built once against a stable, shared contract rather than an ad hoc one that later needs retrofitting.
- `Form` vs. `DynamicForm` is an explicit, documented distinction — prevents the two concepts from being conflated later.
- The `design-system/` reference kit's existing `Field`/`Input`/`Textarea`/`Select` split is validated as the right foundation, reducing the amount of new design work needed (mostly TSX migration + `DynamicForm` addition, not a new pattern from scratch).

### Negative / Trade-offs

- Adds a new blocking task before `story-02/task-04` (Intake functional mockup) and `task-09` (Draft Builder functional mockup) can proceed — the primitives must exist first.
- `DynamicForm`'s config-driven abstraction has real upfront design cost (field-definition schema, per-field-type rendering, Zod integration) even though the Intake screen alone would not strictly need it to ship.

---

## Affected Areas

| Area Code | Impact |
|-----------|--------|
| WB | All current and future `web/` forms must be built from the `Form`/`Field`/`Input`/`Textarea`/`Select`/`Checkbox`/`DynamicForm` primitives instead of per-screen markup; `src/components/ds/` gains new components and `Field.tsx`/`Input.tsx` responsibilities are realigned to this contract. |

---

## Related

- **Planning:** 001-assessment-creation
- **Story / Task:** story-02 / new task (design-system-form-primitives, to be created — blocks `task-04-functional-mockup-intake-screen` and `task-09-functional-mockup-draft-builder-screen`)
- **ADR (if technical follow-up):** none
- **Supersedes:** none
- **Superseded by:** none

---

> [← planning/README.md](README.md)

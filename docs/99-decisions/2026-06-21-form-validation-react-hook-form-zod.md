# ADR: React Hook Form + Zod for all web form validation

- Status: Accepted
- Date: 2026-06-21
- Decision owner: Carlos Martínez

## Context

The login form was initially implemented with HTML native validation attributes (`required`, `type="email"`). This approach has several limitations: validation behavior varies across browsers, errors cannot be styled with DS tokens, there is no way to show field errors inline in the DS `Input` component's error slot, and async server-side errors require a separate state mechanism anyway.

Options considered:

1. **HTML native validation** — `required`, `minLength`, `pattern`, etc.
2. **Formik + Yup** — controlled forms, widely used but more verbose and with more re-renders.
3. **React Hook Form + Zod** — uncontrolled by default, minimal re-renders, Zod schema with full TypeScript inference.

## Decision

React Hook Form (`react-hook-form`) + Zod (`zod`) + the `@hookform/resolvers` adapter. All forms in `web/` must use this combination. HTML native validation attributes must not be used as the validation mechanism.

## Rationale

- RHF uses uncontrolled inputs (refs, not state per keystroke), keeping re-renders minimal in forms with many fields.
- Zod schemas produce TypeScript types via `z.infer<typeof schema>`, eliminating the need to define separate interface types for form data.
- Field errors (`errors.<field>.message`) flow directly to the DS `Input` component's `error` prop, which renders them inline below the input — consistent with the DS visual language.
- `isSubmitting` from RHF's `formState` replaces manual `loading` state for submit buttons.
- The combination is the current industry standard for Next.js/TypeScript projects.

## Consequences

- **Easier:** adding new forms follows a consistent pattern; Zod schema doubles as runtime validation and TypeScript type source.
- **Easier:** server-side errors (Firebase codes, API 4xx) are handled separately as `useState`, clearly distinct from client-side field validation.
- **Harder:** team members unfamiliar with RHF must learn the `register`, `handleSubmit`, `formState` API before contributing to forms.
- **Rule:** every form page must have a Zod schema declared at module level. Inline validation logic inside `onSubmit` is not acceptable.

---

← [Web Design System](2026-06-21-web-design-system.md) | [↑ Top](#) | [README](README.md)

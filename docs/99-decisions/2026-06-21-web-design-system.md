# ADR: GradeOps AI Design System implementation in the web frontend

- Status: Accepted
- Date: 2026-06-21
- Decision owner: Carlos Martínez

## Context

The web frontend was scaffolded with Tailwind CSS and generic utility classes. As the product moved from prototype to a real teacher workspace, the UI needed a consistent visual identity — specific typography (Bricolage Grotesque / Hanken Grotesque), a Sprout green brand palette, and reusable components that look and behave the same across every screen.

The design reference exists as a JSX UI kit in `design/design-system/ui_kits/teacher/`. Options considered:

1. **Publish the UI kit as an npm package** and import it in Next.js.
2. **Adopt an existing component library** (shadcn/ui, Radix, MUI) and theme it.
3. **Build thin TSX components from scratch** using CSS custom property tokens and inline styles, coexisting with Tailwind.

## Decision

Option 3: CSS custom property tokens (`src/styles/ds-tokens/`) loaded globally, and TSX components in `src/components/ds/` that apply them via inline `style={{}}` props.

Tailwind CSS is retained for generic layout utilities. DS components do not use Tailwind classes — all branded properties (color, border, radius, shadow, typography) come from tokens.

## Rationale

- The UI kit is JSX, not a package. Treating it as a reference and writing thin TSX wrappers is the lowest-friction path.
- CSS custom properties are compatible with Tailwind — they add brand values on top without conflicts.
- Inline styles give components exact control over token consumption without specificity fights.
- Pseudo-elements that can't be set with inline styles (e.g., `::placeholder`) are handled via a single class in `globals.css` (`.ds-input`).
- No third-party component library introduces opinion on behavior, accessibility, or animation that would conflict with the product's design intent.

## Consequences

- **Easier:** every new screen has immediate access to the full token scale and all DS components via `import { Button, Badge, Card } from "@/components/ds"`.
- **Easier:** DS components remain independent of Tailwind version upgrades.
- **Harder:** components must be written from scratch (no free accessibility primitives from Radix/etc.). Future work should add ARIA attributes and keyboard navigation to interactive DS components.
- **Risk:** icon set is limited to 18 inline SVGs in `LucideIcon.tsx`. If new icons are needed for a feature, they must be added manually to that file.

---

← [Student access via secure link](2026-06-10-student-access-via-secure-link.md) | [↑ Top](#) | [README](README.md)

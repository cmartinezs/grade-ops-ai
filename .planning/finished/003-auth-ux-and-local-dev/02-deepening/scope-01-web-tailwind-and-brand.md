# 🔍 DEEPENING: Scope 01 — web-tailwind-and-brand

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md)

---

## Objective

Install and configure Tailwind CSS v4, create the GradeOps AI visual identity (logo + slogan component), and add a favicon.

---

## Tasks

| # | Task | Area | Status | Output |
|---|------|------|--------|--------|
| 1 | Install `tailwindcss` and `@tailwindcss/postcss` | `web/` | DONE | `package.json` |
| 2 | Create `postcss.config.mjs` with `@tailwindcss/postcss` plugin | `web/` | DONE | `postcss.config.mjs` |
| 3 | Create `src/app/globals.css` with `@import "tailwindcss"` and import in `layout.tsx` | `web/` | DONE | `globals.css`, `layout.tsx` |
| 4 | Create `AppLogo` component (SVG mark + wordmark + slogan) | `web/` | DONE | `src/components/brand/AppLogo.tsx` |
| 5 | Add `AppLogo` to `/login` and `/register` pages above the card | `web/` | DONE | `login/page.tsx`, `register/page.tsx` |
| 6 | Create `src/app/icon.svg` for Next.js App Router favicon auto-detection | `web/` | DONE | `src/app/icon.svg` |
| 7 | Add `icons: { icon: "/icon.svg" }` to `metadata` in `layout.tsx` | `web/` | DONE | `layout.tsx` |

---

## Done Criteria

- [x] Tailwind utility classes apply correctly across all pages.
- [x] `/login` and `/register` display the GradeOps AI logo mark, wordmark, and slogan.
- [x] Browser tab shows the GradeOps AI favicon.
- [x] No `tailwind.config.js` needed — Tailwind v4 uses PostCSS plugin only.

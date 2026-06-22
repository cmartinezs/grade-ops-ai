# 🌱 INITIAL: 005-design-template

> **Status:** Initial → promovido a DEEPENING directamente (scope claro)
> [← planning/README.md](../../README.md)

---

## Intent

Aplicar el **GradeOps AI Design System** al portal docente (`web/`) — reemplazar la UI genérica (TailwindCSS sin branding) por los tokens, tipografía, componentes y layouts del DS, dejando un template funcional que sirva de base para cada funcionalidad futura.

---

## Why

El frontend actual (`web/`) usa TailwindCSS genérico con colores indigo/gray y tipografía del sistema: sin identidad visual propia, sin los tokens del DS, sin el shell sidebar+topbar definido en el design system.

El design system ya está completamente definido y documentado (`design/design-system/`) con:
- Tokens de color (Sprout, Gold, Slate, semánticos, rúbrica)
- Tipografía (Bricolage Grotesque display + Hanken Grotesque body + JetBrains Mono)
- Componentes React (`components/`)
- UI kits de referencia de alta fidelidad (`ui_kits/teacher/`)
- Templates de shell (`templates/teacher-portal/TeacherPortal.dc.html`)

El objetivo es que el template de diseño quede **funcional** — login y dashboard usan los componentes DS reales; rutas no implementadas (evaluaciones, banco, estudiantes, reportes) son maquetas dentro del shell con el look correcto.

---

## Approximate Scope

- [x] `web/` — Frontend Next.js: globals.css, layout root, componentes
- [ ] `design/design-system/` — Solo lectura (referencia, sin modificar)
- [ ] `.planning/` — Este planning

---

## Estado actual del frontend

| Ruta | Estado actual | Meta del template |
|------|--------------|-------------------|
| `/login` | Funcional, TailwindCSS genérico | Funcional + DS branded |
| `/dashboard` | Funcional (carga evaluaciones reales) | Funcional + DS shell + branded |
| `/assessments` | No existe | Maqueta DS dentro del shell |
| `/bank` | No existe | Maqueta DS dentro del shell |
| `/students` | No existe | Maqueta DS dentro del shell |
| `/reports` | No existe | Maqueta DS dentro del shell |

---

## Decisiones clave

1. **Los tokens del DS se cargan vía CSS** (`design/design-system/styles.css` → `tokens/*.css`). Se importan en `globals.css` como `@import` relativo o se copian los tokens.
2. **Los componentes React del DS** (namespace `GradeOpsAIDesignSystem_fcd12b`) son archivos JSX standalone del UI kit — se adaptan a TSX o se crean componentes wrapper que repliquen el look.
3. **El shell** (sidebar 256px + topbar 64px) se implementa como layout Next.js para el grupo `(protected)`.
4. **Google Fonts** (Bricolage Grotesque + Hanken Grotesque + JetBrains Mono) se cargan desde el layout root de Next.js usando `next/font/google`.
5. **Los logos SVG** ya existen en `design/design-system/assets/` — se copian/referencian en `web/public/`.

---

## Initiator

- **Requested by:** human
- **Date:** 2026-06-21
- **Related planning (if continuation):** 004-subpage-identity

---

## Next Step

- [x] Llenar `01-expansion.md` y scopes en `02-deepening/`
- [x] Mover a `planning/active/`

---

> [← planning/README.md](../../README.md)

# 📐 Planning 005 — design-template

> **Status:** DONE
> [← planning/README.md](../../README.md)

---

## Objetivo

Aplicar el **design system de GradeOps AI** (tokens, tipografía, colores Sprout/Gold/Slate, componentes) al frontend Next.js (`web/`), reemplazando el TailwindCSS genérico por la identidad visual propia del producto y dejando un template funcional para todas las rutas ya existentes: login, dashboard, y el shell del portal docente.

El resultado es un **template de diseño funcional** — las pantallas que hoy existen quedan con el look&feel real del design system; las rutas futuras (evaluaciones, banco, estudiantes, reportes) quedan como maquetas/placeholders correctamente tipografiadas dentro del shell.

---

## Archivos de este planning

| Archivo | Fase | Estado |
|---------|------|--------|
| [00-initial.md](00-initial.md) | INITIAL | ✅ |
| [01-expansion.md](01-expansion.md) | EXPANSION | ✅ |
| [02-deepening/scope-01-ds-tokens-base.md](02-deepening/scope-01-ds-tokens-base.md) | DEEPENING | DONE |
| [02-deepening/scope-02-shell-layout.md](02-deepening/scope-02-shell-layout.md) | DEEPENING | DONE |
| [02-deepening/scope-03-login-page.md](02-deepening/scope-03-login-page.md) | DEEPENING | DONE |
| [02-deepening/scope-04-dashboard-page.md](02-deepening/scope-04-dashboard-page.md) | DEEPENING | DONE |
| [02-deepening/scope-05-placeholder-routes.md](02-deepening/scope-05-placeholder-routes.md) | DEEPENING | DONE |
| [TRACEABILITY.md](TRACEABILITY.md) | — | — |

---

## Retrospective

### Qué se entregó

5/5 scopes completados. El frontend Next.js pasó de TailwindCSS genérico a usar el design system propio de GradeOps AI: tokens CSS, tipografía Bricolage/Hanken, colores Sprout/Gold/Slate, shell docente funcional (sidebar + topbar), login rediseñado, dashboard con datos reales, y 4 rutas maqueta navegables.

### Lo que salió distinto al plan

- **`AssessmentSummaryDto` no tenía `type` ni `average`** — el `AssessmentRow` se adaptó a los campos disponibles. El mapeo de tipo quedó pendiente para cuando el API lo exponga (R01).
- **`Hanken_Grotesk`** es el nombre correcto en el paquete `next/font/google`, no `Hanken_Grotesque` como lo nombraba el DS — diferencia descubierta durante la implementación del scope 01.
- **`PlaceholderPage`** quedó en `components/shell/` en vez de `components/ds/` — mejor ubicación dado que es un componente de estructura de navegación, no una primitiva del DS.

### Decisiones clave que conviene recordar

- Los componentes del DS (`GradeOpsAIDesignSystem_fcd12b`) son JSX standalone del UI kit, **no un paquete npm** — siempre se crean thin wrappers TSX propios en `components/ds/`.
- Fuentes vía `next/font/google`, nunca `@import url()` directo — incompatible con la optimización de Next.js.
- TailwindCSS se mantiene conviviendo con los tokens CSS del DS sin conflicto. Los tokens DS cubren marca/identidad; Tailwind cubre utilidades genéricas de layout.
- Los stat cards del dashboard son maqueta explícita — el API no tiene los endpoints de estadísticas agregadas todavía.

### Residuales abiertos para plannings futuros

- Panel derecho del dashboard (cobertura por curso, estudiantes en riesgo) — depende de endpoints de estadísticas en `api/`.
- `AssessmentSummaryDto.type` no existe aún — el `AssessmentRow` no puede mostrar el badge de tipo (Abierta/Cerrada) con datos reales.
- Funcionalidad password reset — cubierta por planning 007.

---

> [← planning/README.md](../../README.md)

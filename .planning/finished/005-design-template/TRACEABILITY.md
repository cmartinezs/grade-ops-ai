# 🔗 Traceability: 005-design-template

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| AG | Agent Runtime (`agents/`) |
| AP | Backend / Domain (`api/`) |
| DO | Documentation (`docs/`) |
| IN | Infrastructure (`infra/`) |
| WB | Frontend (`web/`) |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

| Term / Concept | AG | AP | DO | IN | WB | W | Notes |
|---------------|----|----|----|----|----|---|-------|
| GradeOps AI Design System | N/A | N/A | ✅ | N/A | ✅ | ✅ | Implementado en scopes 01–04; tokens, fuentes, componentes DS activos en web |
| Tokens CSS (--brand, --surface-card, etc.) | N/A | N/A | ✅ | N/A | ✅ | ✅ | `web/src/styles/ds-tokens/` — colors, typography, spacing, base cargados en globals.css |
| Bricolage Grotesque (font-display) | N/A | N/A | ✅ | N/A | ✅ | ✅ | Cargada vía `next/font/google`; mapeada a `--font-display` en globals.css |
| Hanken Grotesque (font-sans) | N/A | N/A | ✅ | N/A | ✅ | ✅ | Cargada vía `next/font/google`; mapeada a `--font-sans` en globals.css |
| Sprout (--brand, --sprout-*) | N/A | N/A | ✅ | N/A | ✅ | ✅ | Tokens en colors.css; usado en Button, AppLogo, panel login, Badge |
| Gold (--accent, --gold-*) | N/A | N/A | ✅ | N/A | ✅ | ✅ | Tokens en colors.css; usado en CreditsWidget, Badge gold, StatCard gold |
| Shell (sidebar 256px + topbar 64px) | N/A | N/A | ✅ | N/A | ✅ | ✅ | `AppShell.tsx` en `components/shell/`; sidebar + topbar + content area |
| StatCard | N/A | N/A | ✅ | N/A | ✅ | ✅ | `web/src/components/ds/StatCard.tsx`; 4 cards maqueta en DashboardPage |
| Badge (tones) | N/A | N/A | ✅ | N/A | ✅ | ✅ | `web/src/components/ds/Badge.tsx`; 7 tones; usado en AssessmentRow |
| Card (header/body/footer) | N/A | N/A | ✅ | N/A | ✅ | ✅ | `web/src/components/ds/Card.tsx`; compound pattern con Header/Title/Body/Footer |
| AssessmentSummaryDto | N/A | ✅ | ⚠️ | N/A | ✅ | ✅ | DTO falta `type`, `average`, `courseName`; adaptado en AssessmentRow con campos disponibles |
| PlaceholderPage | N/A | N/A | N/A | N/A | ✅ | ✅ | `web/src/components/shell/PlaceholderPage.tsx`; usado en /assessments, /bank, /students, /reports |
| ShellContext | N/A | N/A | N/A | N/A | ✅ | ✅ | `ShellContext.tsx`; `ShellProvider`, `useShell`, `useShellConfig` implementados |
| AuthGuard | N/A | N/A | N/A | N/A | ✅ | ✅ | Existente; no modificado |
| Lucide Icons (SVG inline) | N/A | N/A | ✅ | N/A | ✅ | ✅ | `LucideIcon.tsx` en `components/ds/`; 18 íconos como SVG inline |
| Input DS | N/A | N/A | N/A | N/A | ✅ | ✅ | `web/src/components/ds/Input.tsx`; foco ring verde, error state, icon prefijo |
| Field DS | N/A | N/A | N/A | N/A | ✅ | ✅ | `web/src/components/ds/Field.tsx`; wrapper label + slot |
| GoogleButton DS | N/A | N/A | N/A | N/A | ✅ | ✅ | `web/src/components/ds/GoogleButton.tsx`; estilo DS + lógica Firebase |
| AssessmentRow | N/A | N/A | N/A | N/A | ✅ | ✅ | `web/src/components/dashboard/AssessmentRow.tsx`; reemplaza AssessmentCard en dashboard |
| DashboardEmptyState | N/A | N/A | N/A | N/A | ✅ | ✅ | `web/src/components/dashboard/DashboardEmptyState.tsx`; reemplaza EmptyDashboard |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D01 | No migrar el namespace `GradeOpsAIDesignSystem_fcd12b` — crear componentes TSX propios | Los componentes del DS son JSX standalone (UI kit), no un paquete npm. Crear thin wrappers TSX es más limpio para Next.js. | WB (`components/ds/`) | 2026-06-21 |
| D02 | Usar `next/font/google` para fuentes, no copiar `fonts.css` del DS | `@import url()` directo es incompatible con la optimización de Next.js. `next/font/google` es el approach recomendado. | WB (`layout.tsx`) | 2026-06-21 |
| D03 | Mantener TailwindCSS para utilidades genéricas; los tokens DS son CSS custom properties compatibles | No eliminar Tailwind — los tokens se agregan encima, sin conflicto. Usar tokens DS para todo lo relacionado con branding. | WB (`globals.css`) | 2026-06-21 |
| D04 | Los stat cards del dashboard son maqueta en este planning | El API no tiene endpoints para promedios, entregas a tiempo, etc. La lista de evaluaciones sí tiene datos reales. | WB, AP | 2026-06-21 |
| D05 | Íconos Lucide como SVG inline (sin npm, sin CDN) | Evitar dependencias adicionales. Solo se necesitan ~12 íconos. | WB (`components/ds/icons/`) | 2026-06-21 |
| D06 | La lógica de auth (Firebase, Google SSO, AuthGuard) no se toca | Este planning es solo capa visual. Romper auth afectaría la funcionalidad real. | WB (`components/auth/`) | 2026-06-21 |
| D07 | Interfaz en español (Chile) según el DS | El DS especifica español (Chile) como idioma de la interfaz. La página de login actual está en inglés — se traduce. | WB (`login/page.tsx`) | 2026-06-21 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R01 | Panel derecho del dashboard (cobertura por curso, estudiantes en riesgo) | Requiere endpoints API de estadísticas | PENDING | Planning futuro (estadísticas del dashboard) |
| R02 | Funcionalidad password reset | Fuera del scope del template | PENDING | Planning futuro (auth features) |
| R03 | Tipo exacto de `AssessmentSummaryDto.type` y `status` — verificar si los valores del enum coinciden con los esperados por el mapeo | Verificar al implementar scope-04 | RESOLVED | Status enum: DRAFT/OPEN/GRADING/CLOSED (no type field). AssessmentRow adaptado. |

---

> [← planning/README.md](../../README.md)

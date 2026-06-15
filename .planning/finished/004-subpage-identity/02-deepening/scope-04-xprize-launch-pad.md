# 🔍 DEEPENING: Scope 04 — xprize-launch-pad

> **Status:** DONE
> [← 01-expansion.md](../../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Rediseñar la página `/xprize-hackathon` (ES + EN) con identidad "Launch Pad": fondo con degradado radial coral→oscuro y anillos de pulso animados detrás del countdown, y dos secciones nuevas (criterios del jurado con respuesta GradeOps + evidencia que se entregará).

---

## Contexto de diseño

**Identidad:** Cuenta regresiva de lanzamiento — urgencia, energía, enfoque.
**Fondo:** `radial-gradient(ellipse 80% 50% at 50% 0%, rgba(249,115,22,0.12) 0%, transparent 70%)` sobre el fondo oscuro base. Persiste incluso con `prefers-reduced-motion`.
**Anillos de pulso:** 3 divs absolutos concéntricos detrás del countdown, con `@keyframes pulse-ring { 0% { transform: scale(1); opacity: 0.3 } 100% { transform: scale(2.2); opacity: 0 } }`, `animation-delay: 0s / 0.6s / 1.2s`. Se deshabilitan con `prefers-reduced-motion`.
**Color dominante:** Coral `#f97316` con acento dorado `#f59e0b`.
**Animación scroll-reveal:** Las cards de criterios del jurado revelan con stagger de 150ms.

---

## Contenido nuevo a agregar

### Sección "Judging Criteria — How GradeOps Qualifies"
Fuente: `docs/00-project/hackathon-strategy.md` (tabla Judging Criteria Mapping)

Tres cards, cada una con el criterio, qué buscan los jueces, y la evidencia concreta de GradeOps:

**Business Viability**
- Qué evalúan: Real business, real users, real revenue, sustainable model.
- GradeOps: Pilots con educadores reales, pricing table, revenue ledger, cost model.

**AI-Native Operations**
- Qué evalúan: AI live in production executing key workflow decisions.
- GradeOps: Pipeline de 13 agentes, AgentExecutionLog por cada ejecución, approval states visibles en el dashboard.

**Category Impact — Education & Human Potential**
- Qué evalúan: Meaningful improvement in the chosen category.
- GradeOps: Tiempo del docente liberado, feedback personalizado escalable, detección de brechas de aprendizaje, capacidad operativa de academia grande para proveedores pequeños.

### Sección "Evidence We'll Submit"
Fuente: `docs/05-evidence/` (agent-logs, revenue, usage-metrics, users)

Lista de evidencia estructurada que se entregará al hackathon:

| Tipo de evidencia | Entidad del sistema |
|------------------|---------------------|
| Agent execution logs | `AgentExecutionLog` — modelo, tokens, costo, input/output resumido |
| Teacher approval trail | `ApprovalEvent` — cada aprobación/rechazo con timestamp y autor |
| Revenue evidence | `RevenueEvent` — ingresos reales, indicador `related_party` |
| Cost transparency | `CostEvent` — costo por ejecución de agente, por assessment |
| User evidence | Teachers activos, assessments completados, submissions procesadas |

---

## Tasks

| # | Task | Status | Output |
|---|------|--------|--------|
| 1 | Leer `docs/00-project/hackathon-strategy.md` para extraer la tabla de criterios y evidencia GradeOps | PENDING | Datos inline en el scope |
| 2 | Leer `docs/05-evidence/` (README + archivos) para mapear tipos de evidencia a entidades | PENDING | Datos inline en el scope |
| 3 | Modificar `site/src/pages/xprize-hackathon.astro` (ES): agregar fondo radial, anillos de pulso, scroll-reveal en criterios, sección criterios del jurado, sección evidencia | PENDING | `site/src/pages/xprize-hackathon.astro` |
| 4 | Modificar `site/src/pages/en/xprize-hackathon.astro` (EN): mismo diseño, contenido en inglés | PENDING | `site/src/pages/en/xprize-hackathon.astro` |

---

## Done Criteria

- [x] El fondo radial coral→oscuro es visible y da identidad inmediata a la página
- [x] Los 3 anillos de pulso animan detrás del countdown y se detienen con `prefers-reduced-motion`
- [x] La sección "Judging Criteria" muestra 3 cards con criterio, lo que buscan los jueces, y la evidencia GradeOps
- [x] La sección "Evidence We'll Submit" muestra la tabla de tipos de evidencia mapeados a entidades del sistema
- [x] Las cards de criterios revelan al scroll con stagger de 150ms
- [x] Ambas versiones (ES y EN) actualizadas
- [x] TRACEABILITY.md actualizado con nuevos términos de este scope

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../../01-expansion.md) | [← planning/README.md](../../../README.md)

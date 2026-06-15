# 🔍 DEEPENING: Scope 01 — development-mission-control

> **Status:** DONE
> [← 01-expansion.md](../../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Rediseñar la página `/development` (ES + EN) con identidad "Mission Control": fondo con grid de puntos animado teal/negro, animación de reveal por fase al hacer scroll, y dos secciones nuevas de contenido (pipeline de agentes + arquitectura).

---

## Contexto de diseño

**Identidad:** Terminal oscura / sala de control.
**Fondo:** `radial-gradient(circle, rgba(0,212,200,0.06) 1px, transparent 1px)` en `background-size: 28px 28px` sobre `#020f1a`. Div absoluto detrás del contenido, bloqueado por `prefers-reduced-motion`.
**Color dominante:** `var(--teal)` sobre negro profundo.
**Animación scroll-reveal:** `IntersectionObserver` sobre cada `.phase-card`. Clases iniciales `opacity-0 translate-y-6`, al entrar: `opacity-100 translate-y-0 transition-all duration-600`. Stagger de 120ms por índice de fase.

---

## Contenido nuevo a agregar

### Sección "Agent Pipeline"
Fuente: `docs/03-ai-agents/agents-overview.md`

**Open Assessment (8 agentes):**
Assessment → Rubric → Grading → Feedback → Learning Gap → Recovery → Teacher Report → Ops Evidence

**Closed Assessment (5 agentes):**
Question Generation → Distractor Quality → Ambiguity Review → Assessment Assembly → Item Analytics

Presentación: dos filas horizontales tipo "pipeline" con flechas, cada nodo es un chip teal/indigo con el nombre del agente. En mobile: lista vertical con conector.

### Sección "Architecture Decisions"
Fuente: `docs/04-architecture/system-architecture.md`

3 decisiones clave a mostrar como cards:
1. **Modular monolith + separate agent runtime** — API y Agents son servicios distintos deployados por separado en Cloud Run.
2. **Agents don't own data** — Solo reciben Command, devuelven Result. Persistencia siempre en el API.
3. **Versioned prompts as files** — StringTemplate `.st` en `resources/prompts/`. Nunca inline en Java.

---

## Tasks

| # | Task | Status | Output |
|---|------|--------|--------|
| 1 | Leer `docs/03-ai-agents/agents-overview.md` para extraer nombres exactos de los 13 agentes y sus responsabilidades | DONE | Datos inline en el scope |
| 2 | Leer `docs/04-architecture/system-architecture.md` para extraer las 3 decisiones arquitectónicas | DONE | Datos inline en el scope |
| 3 | Modificar `site/src/pages/development.astro` (ES): agregar fondo grid, scroll-reveal en fases, sección pipeline, sección arquitectura | DONE | `site/src/pages/development.astro` |
| 4 | Modificar `site/src/pages/en/development.astro` (EN): mismo diseño, contenido en inglés | DONE | `site/src/pages/en/development.astro` |

---

## Done Criteria

- [x] El fondo grid de puntos es visible en desktop y se deshabilita con `prefers-reduced-motion`
- [x] Las 4 fases de roadmap hacen fade-in+slide al entrar al viewport con stagger de 120ms
- [x] La sección "Agent Pipeline" muestra los 8 agentes Open y 5 Closed como chips conectados
- [x] La sección "Architecture Decisions" muestra 3 cards con las decisiones clave
- [x] Ambas versiones (ES y EN) están actualizadas
- [x] El script de scroll-reveal funciona correctamente sin Intersection Observer leaking entre páginas
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

# 🌱 INITIAL: 004-subpage-identity

> **Status:** Initial
> [← planning/README.md](../../../README.md)

---

## Intent

Dar a cada subpágina del site de GradeOps AI una identidad visual propia — fondo distintivo, animaciones al scrollear, y contenido más profundo extraído de la documentación — para que cada página comunique con fuerza su tema sin verse genérica.

---

## Why

Actualmente las cinco subpáginas (development, requirements, technicals, xprize-hackathon, contact) comparten la misma estructura visual: fondo oscuro plano, `SectionHero` genérico, tarjetas con bordes de color. El resultado es homogéneo y no diferencia el carácter de cada sección.

Para un hackathon y para visitantes (educadores, evaluadores de XPRIZE, potenciales colaboradores), cada página debe capturar la atención de forma inmediata y transmitir una personalidad propia.

Además, el contenido actual es superficial: las páginas de requirements y technicals listan ítems cortos sin profundidad narrativa, y la de development no muestra la riqueza del pipeline de 13 agentes que ya está documentado.

---

## Approximate Scope

- [ ] `site/` — Todas las subpáginas en `src/pages/en/` + posible utility CSS / script inline para scroll reveal
- [ ] `docs/` — Lectura (no modificación) para extraer contenido profundo

---

## Diseño propuesto por página

### 1. `/development` — "Mission Control"
- **Identidad:** Terminal oscura / sala de control. Fondo con sutil grid de puntos animado (`radial-gradient` en pseudo-elemento).
- **Color dominante:** Teal sobre negro profundo (`#010d18`).
- **Animación:** Fases del roadmap se revelan con `IntersectionObserver` al scrollear (fade-in + slide desde la izquierda, stagger por fase).
- **Contenido nuevo:**
  - Sección "Pipeline de agentes" mostrando el flujo Open (8 agentes) y Closed (5 agentes) con íconos — extraído de `docs/03-ai-agents/agents-overview.md`.
  - Sección "Decisiones de arquitectura" con las decisiones clave — extraído de `docs/04-architecture/system-architecture.md`.

### 2. `/requirements` — "Blueprint"
- **Identidad:** Plano técnico / documento de ingeniería. Fondo con patrón de cuadrícula diagonal muy sutil (SVG `<pattern>` en CSS `background-image`).
- **Color dominante:** Indigo (#6366f1) sobre azul oscuro.
- **Animación:** Grupos de requirements aparecen en cascada al hacer scroll (stagger de 80ms por tarjeta, `opacity` + `translateY`).
- **Contenido nuevo:**
  - Una frase de contexto por cada grupo explicando *por qué* ese requerimiento existe — extraído de `docs/02-product/mvp-scope.md` y `docs/02-product/workflows.md`.
  - Sección "Modos de evaluación" comparando Open vs Closed — extraído de `docs/02-product/assessment-modes.md`.

### 3. `/technicals` — "Engine Room"
- **Identidad:** Sala de máquinas / circuito. Fondo con líneas horizontales muy tenues (`repeating-linear-gradient`).
- **Color dominante:** Degradado teal→indigo en bordes y acentos.
- **Animación:** Cards del stack aparecen con escalonado al entrar al viewport. El diagrama ASCII de arquitectura tiene efecto de "typing" al entrar.
- **Contenido nuevo:**
  - Sección "Modelo de datos — entidades clave" con las entidades principales del dominio — extraído de `docs/04-architecture/data-model.md`.
  - Sección "Módulos del API" describiendo la estructura modular — extraído de `docs/04-architecture/api-design.md`.
  - Sección "Seguridad" con modelo de autenticación — extraído de `docs/04-architecture/security.md`.

### 4. `/xprize-hackathon` — "Launch Pad"
- **Identidad:** Cuenta regresiva de lanzamiento. Fondo con degradado radial coral→oscuro, anillos de pulso animados detrás del countdown (CSS `@keyframes` `scale` + `opacity`).
- **Color dominante:** Coral (#f97316) con acento dorado.
- **Animación:** Cards de criterios del jurado se revelan al scroll. Countdown con pulso suave en los números.
- **Contenido nuevo:**
  - Sección "Criterios del jurado" con Business Viability, AI-Native Ops, Category Impact — y cómo GradeOps los cumple, extraído de `docs/07-hackathon/submission-narrative.md` y `docs/00-project/hackathon-strategy.md`.
  - Sección "Evidencia que entregaremos": AgentExecutionLog, ApprovalEvent, RevenueEvent, CostEvent — extraído de `docs/05-evidence/`.

### 5. `/contact` — "Open Door"
- **Identidad:** Cálida, humana, colaborativa. Fondo con degradado muy suave de oscuro a azul medianoche con textura de ruido SVG.
- **Color dominante:** Teal con tonos de blanco cálido.
- **Animación:** Tarjetas de perfil (Educator, Developer, Researcher) tienen hover con elevación 3D suave (`transform: perspective(600px) rotateY`).
- **Contenido nuevo:**
  - Párrafo de visión del producto / por qué importa la colaboración — extraído de `docs/02-product/personas.md`.
  - Sección "¿A quién buscamos?" con bullets concretos para cada perfil.

---

## Initiator

- **Requested by:** human
- **Date:** 2026-06-15
- **Related planning (if continuation):** 001-teacher-onboarding, 003-auth-ux-and-local-dev

---

## Next Step

- [ ] Cuando esté dimensionado → llenar `01-expansion.md` y mover a `planning/active/`
- [ ] Revisar docs relevantes antes de la expansión para confirmar extractabilidad de contenido

### Open Questions

1. ¿Scroll reveal debe ser un componente Astro reutilizable o `<script is:inline>` + `IntersectionObserver` por página? Preferencia: script inline para no añadir dependencias.
2. ¿Las animaciones de fondo deben respetar `prefers-reduced-motion`? Sí — envolver en `@media (prefers-reduced-motion: no-preference)`.
3. ¿Las páginas en `/` (raíz, sin `/en/`) requieren los mismos cambios o solo se trabaja en `en/`? Confirmar antes de la expansión.

---

> [← planning/README.md](../../../README.md)

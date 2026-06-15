# 🔍 DEEPENING: Scope 03 — technicals-engine-room

> **Status:** DONE
> [← 01-expansion.md](../../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Rediseñar la página `/technicals` (ES + EN) con identidad "Engine Room": fondo de líneas horizontales tenues, animación de typing en el diagrama de arquitectura, cards del stack escalonadas al scroll, y tres secciones nuevas (modelo de datos, módulos API, seguridad).

---

## Contexto de diseño

**Identidad:** Sala de máquinas / circuito electrónico.
**Fondo:** `repeating-linear-gradient(0deg, rgba(255,255,255,0.02) 0px, rgba(255,255,255,0.02) 1px, transparent 1px, transparent 48px)` — líneas horizontales cada 48px, muy sutiles. Div absoluto, `pointer-events: none`.
**Color dominante:** Degradado teal→indigo en bordes de sección y acentos. `border-image: linear-gradient(to right, var(--teal), var(--indigo)) 1`.
**Animación — diagrama ASCII:** Al entrar al viewport, el bloque `<pre>` hace un efecto "reveal" de arriba a abajo: cada línea tiene `animation-delay: N*80ms` y `opacity: 0 → 1`. Sin typewriter per-character (demasiado lento para 4 líneas largas).
**Animación — stack cards:** Stagger de 100ms entre las 4 cards de layer (Frontend, API, Agents, Infra).

---

## Contenido nuevo a agregar

### Sección "Core Domain Entities"
Fuente: `docs/04-architecture/data-model.md`

Entidades principales del modelo de dominio, presentadas como tabla compacta:

| Entidad | Propósito |
|---------|-----------|
| `Assessment` | Ciclo de evaluación con modo (Open/Closed), objetivo de aprendizaje y estado de workflow |
| `Rubric` | Criterios y pesos aprobados por el docente; inmutable una vez aprobada |
| `StudentSubmission` | Trabajo entregado por el estudiante; input para el agente de calificación |
| `GradeSuggestion` | Sugerencia de calificación del agente; requiere ApprovalEvent para finalizarse |
| `FeedbackDraft` | Feedback generado por el agente; requiere aprobación antes de entregarse al estudiante |
| `AgentExecutionLog` | Registro estructurado de cada ejecución: modelo, tokens, costo, estado de aprobación |
| `ApprovalEvent` | Evento de aprobación/rechazo explícito del docente sobre cualquier output AI |
| `AssessmentInvitation` | Link firmado para que el estudiante acceda sin cuenta |
| `Question` / `QuestionVersion` | Pregunta del banco (Closed); versionada y curada por el docente |

### Sección "API Modules"
Fuente: `docs/04-architecture/api-design.md`

Recursos REST principales agrupados por función:

| Grupo | Recursos |
|-------|---------|
| Core assessment | `/assessments`, `/rubrics`, `/submissions` |
| AI outputs | `/grading-suggestions`, `/feedback-drafts`, `/learning-gaps`, `/recovery-activities` |
| Reports | `/teacher-reports`, `/agent-runs`, `/evidence` |
| Closed assessment | `/questions` (bank), `/assessment-attempts` |
| Platform | `/auth/me`, `/organizations`, `/usage` |

### Sección "Security Model"
Fuente: `docs/04-architecture/security.md`

3 principios de seguridad como cards compactas:

1. **Tenant isolation** — Cada docente solo accede a registros de su organización. Violación → 404, no 403.
2. **Server-side AI calls only** — La Gemini API key nunca llega al frontend. Solo el API llama a los agentes.
3. **Approval as a security control** — Los outputs AI de alto impacto (rúbrica, calificación, feedback) permanecen en estado `pending` hasta `ApprovalEvent` explícito.

---

## Tasks

| # | Task | Status | Output |
|---|------|--------|--------|
| 1 | Leer `docs/04-architecture/data-model.md` para extraer entidades clave y sus propósitos | PENDING | Datos inline en el scope |
| 2 | Leer `docs/04-architecture/api-design.md` para extraer recursos y agruparlos por función | PENDING | Datos inline en el scope |
| 3 | Leer `docs/04-architecture/security.md` para extraer los 3 principios de seguridad más relevantes | PENDING | Datos inline en el scope |
| 4 | Modificar `site/src/pages/technicals.astro` (ES): agregar fondo líneas, typing effect en diagrama, cards escalonadas, 3 secciones nuevas | PENDING | `site/src/pages/technicals.astro` |
| 5 | Modificar `site/src/pages/en/technicals.astro` (EN): mismo diseño, contenido en inglés | PENDING | `site/src/pages/en/technicals.astro` |

---

## Done Criteria

- [x] El fondo de líneas horizontales tenues es visible en desktop
- [x] El diagrama ASCII hace reveal de línea en línea al entrar al viewport
- [x] Las 4 cards del stack hacen reveal escalonado (stagger 100ms)
- [x] La sección "Core Domain Entities" muestra la tabla de entidades con propósito
- [x] La sección "API Modules" muestra los recursos agrupados por función
- [x] La sección "Security Model" muestra 3 cards de principios de seguridad
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

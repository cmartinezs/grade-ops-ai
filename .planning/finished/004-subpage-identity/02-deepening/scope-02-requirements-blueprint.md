# 🔍 DEEPENING: Scope 02 — requirements-blueprint

> **Status:** DONE
> [← 01-expansion.md](../../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Rediseñar la página `/requirements` (ES + EN) con identidad "Blueprint": fondo de cuadrícula diagonal SVG, cascada de cards al scrollear, y contenido enriquecido con contexto de por qué existe cada grupo de requerimientos + comparativa de modos de evaluación.

---

## Contexto de diseño

**Identidad:** Plano técnico / documento de ingeniería.
**Fondo:** `background-image: url("data:image/svg+xml,...")` con un `<pattern>` SVG de líneas diagonales a 45° en `rgba(99,102,241,0.05)` (indigo muy transparente), `patternUnits="userSpaceOnUse"`, `patternTransform="rotate(45)"`. Div absoluto, `pointer-events: none`, `prefers-reduced-motion` desactiva solo las animaciones, no el fondo.
**Color dominante:** Indigo `#6366f1` sobre azul oscuro.
**Animación scroll-reveal:** Stagger de 80ms por card de grupo. Clases iniciales `opacity-0 translate-y-5`, al entrar `opacity-100 translate-y-0 transition-all duration-500`.

---

## Contenido nuevo a agregar

### Contexto por grupo de requerimientos
Fuente: `docs/02-product/mvp-scope.md` y `docs/02-product/workflows.md`

Agregar 1 frase de contexto ("por qué existe este grupo") antes del listado de ítems de cada grupo:

| Grupo | Contexto a mostrar |
|-------|-------------------|
| Assessment cycle management | El ciclo es la unidad operativa central: define el alcance, trazabilidad y estado de cada evaluación. |
| Open assessment — activity & rubric | La rúbrica es el contrato de calificación: una vez aprobada, no puede modificarse sin crear una nueva versión. |
| Grading & feedback — open | Ningún resultado llega al estudiante sin aprobación explícita del docente — esto no es opcional. |
| Closed assessment — question bank | El banco de preguntas es AI-native: los agentes generan, el docente cuura, el sistema versiona. |
| Reports & evidence | La evidencia es first-class, no un efecto secundario: cada log de agente y evento de aprobación es una entidad del dominio. |
| Student access | Los estudiantes acceden por link firmado, sin cuenta — mínima fricción, máxima privacidad. |

### Sección "Assessment Modes" (nueva)
Fuente: `docs/02-product/assessment-modes.md`

Tabla comparativa compacta Open vs Closed:

| Dimensión | Open | Closed |
|-----------|------|--------|
| Tipo de respuesta | Código, texto, archivo | Selección de alternativas (VF, SC, MC) |
| Generación | AI genera contexto, instrucciones y rúbrica | AI genera preguntas, alternativas y clave |
| Calificación | AI sugiere; docente aprueba | Determinista contra clave congelada |
| Control clave | Aprobación de rúbrica + sugerencias | Curación de preguntas + clave |
| Mejor uso | Tareas prácticas de programación | Verificación conceptual, teoría |

---

## Tasks

| # | Task | Status | Output |
|---|------|--------|--------|
| 1 | Leer `docs/02-product/mvp-scope.md` y `docs/02-product/workflows.md` para confirmar el contexto de cada grupo | PENDING | Datos inline en el scope |
| 2 | Leer `docs/02-product/assessment-modes.md` para extraer la tabla comparativa | PENDING | Datos inline en el scope |
| 3 | Modificar `site/src/pages/requirements.astro` (ES): agregar fondo SVG diagonal, scroll-reveal en cards, contexto por grupo, sección Assessment Modes | PENDING | `site/src/pages/requirements.astro` |
| 4 | Modificar `site/src/pages/en/requirements.astro` (EN): mismo diseño, contenido en inglés | PENDING | `site/src/pages/en/requirements.astro` |

---

## Done Criteria

- [x] El fondo de cuadrícula diagonal SVG es visible y no interfiere con la lectura del contenido
- [x] Las cards de grupos de requerimientos hacen reveal en cascada al hacer scroll (stagger 80ms)
- [x] Cada grupo muestra su frase de contexto antes del listado
- [x] La sección "Assessment Modes" aparece como tabla comparativa styled (Open vs Closed)
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

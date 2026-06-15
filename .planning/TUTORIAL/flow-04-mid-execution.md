# Flujo D — Ajustar el planning durante la ejecución

> [← Tutorial](README.md)

**Cuándo usar este flujo:** hay un planning activo en `.planning/active/` y algo cambió — aparece trabajo nuevo, un scope resulta ser demasiado vago para ejecutar, o un scope es mucho más grande de lo esperado.

Tres situaciones, tres comandos:

| Situación | Comando |
|-----------|---------|
| [Trabajo nuevo no previsto](#trabajo-nuevo-no-previsto) | `/plan-enrich-epic` |
| [Scope mal definido o ambiguo](#scope-mal-definido) | `/plan-enrich-story` |
| [Scope demasiado amplio](#scope-demasiado-amplio) | `/plan-split-story` |

---

## Trabajo nuevo no previsto

### Situación A — El trabajo es solo de ejecución

Planning `005-grading-assistance` está en curso. Al implementar scope-01 se descubre que falta un mecanismo de **rate limiting** para el Grading Agent que no estaba en ningún scope.

```
/plan-enrich-epic 005-grading-assistance
```

Claude muestra los scopes actuales y pregunta qué agregar:

```
Scopes actuales de 005-grading-assistance:
  scope-01  Rubric-Based Grading Suggestion   [DONE]
  scope-02  Uncertainty Flags                 [IN PROGRESS]
  scope-03  Teacher Edit of Score             [TODO]
  scope-04  Reject AI Suggestion              [TODO]

¿Qué scopes nuevos quieres agregar? >
```

```
> scope-05-grading-agent-rate-limiting: Add per-teacher rate limiting
  to the Grading Agent to prevent runaway API costs. Area: AP + AG.
  Depends on: scope-01.
```

Claude agrega la fila en `01-expansion.md`, crea `scope-05-grading-agent-rate-limiting.md` con status `TODO`, y actualiza `active/README.md`.

```
ACTIVE
  005-grading-assistance
    scope-01  Rubric-Based Grading Suggestion   [DONE]
    scope-02  Uncertainty Flags                 [IN PROGRESS]
    scope-03  Teacher Edit of Score             [TODO]
    scope-04  Reject AI Suggestion              [TODO]
    scope-05  Grading Agent Rate Limiting       [TODO]   ← nuevo
```

### Situación B — El trabajo también necesita product tracking

La misma situación, pero quieres que el requisito quede registrado como user story en `docs/` antes de agregarlo al planning.

```
# Paso 1: registrar en el backlog de producto
/us-new epic-05-grading-assistance

# Sigue el flujo interactivo → genera US-044

# Paso 2: agregar el scope al planning activo
/plan-enrich-epic 005-grading-assistance
```

En el segundo comando, menciona `US-044` para que el scope quede enlazado:

```
> scope-05-grading-agent-rate-limiting — based on US-044
```

El scope file generado incluirá la línea `> Source: US-044`.

---

## Scope mal definido

Planning `003-rubric-generation` está activo. Al intentar ejecutar scope-03, las tareas son tan vagas que no está claro qué hay que implementar exactamente.

```
Scope file actual:
  ## Objective
  Implement rubric validation

  ## Tasks
  - [ ] Validate the rubric
  - [ ] Make sure it works

  ## Done Criteria
  - Rubric is validated
```

```
/plan-enrich-story 003-rubric-generation scope-03
```

Claude diagnóstica los problemas:

```
scope-03-rubric-validation — diagnóstico:

  ✗ Objetivo ambiguo: "validate" puede significar formato, lógica de negocio,
    o confirmación del teacher — no está especificado.
  ✗ Tareas no ejecutables: "Validate the rubric" y "Make sure it works"
    no tienen output definido ni pasos concretos.
  ✗ Done criteria no verificables: "Rubric is validated" no es un estado observable.

Propuesta de objetivo reescrito:
  "Implement the RubricValidationService that checks a rubric draft for:
   (1) all criteria have non-empty descriptions and weights > 0,
   (2) weights sum to 100, (3) at least 2 criteria exist."

¿Acepta, edita, o skip? >
```

Para cada problema detectado, Claude propone y espera confirmación. Al terminar, el scope queda con:
- Objetivo preciso
- Tareas con outputs concretos (`RubricValidationService.java`, test class, API error response)
- Done criteria verificables (`weights sum to 100`, `400 response on invalid rubric`)

El status del scope **no cambia** — si estaba en `TODO`, sigue en `TODO`.

---

## Scope demasiado amplio

Planning `009-evidence-metrics` incluye scope-02, que originalmente se definió como "Implement the full evidence dashboard". Al revisar las tareas, el scope tiene 14 ítems mezclando backend, frontend, y agente — demasiado para una sola unidad de trabajo.

```
/plan-split-story 009-evidence-metrics scope-02
```

Claude analiza el scope y propone una división:

```
scope-02 — Evidence Dashboard — análisis:

14 tareas detectadas mezclando:
  - Group A (AP): 5 tareas de backend (endpoints, queries, modelos)
  - Group B (WB): 6 tareas de frontend (componentes, filtros, gráficos)
  - Group C (AG): 3 tareas del Ops Evidence Agent

Propuesta de split:
  scope-02a  Evidence Dashboard — API layer      (AP) — 5 tareas
  scope-02b  Evidence Dashboard — Web UI         (WB) — 6 tareas, depends: scope-02a
  scope-02c  Ops Evidence Agent integration      (AG) — 3 tareas, depends: scope-02a

¿Confirmas este split, o propones otro? >
```

Al confirmar:
1. Se crean `scope-02a`, `scope-02b`, `scope-02c` con las tareas distribuidas
2. El status del scope original (`IN PROGRESS`) se preserva en `scope-02a`; los demás quedan `TODO`
3. Si había tareas ya marcadas `[x]`, se heredan en el scope que las contiene
4. `01-expansion.md` se actualiza: la fila de `scope-02` se reemplaza por tres filas
5. Las dependencias de scopes posteriores que apuntaban a `scope-02` se actualizan para apuntar al último del split (`scope-02c`)

```
ACTIVE
  009-evidence-metrics
    scope-01  Agent Execution Log             [DONE]
    scope-02a Evidence Dashboard — API        [IN PROGRESS]   ← split
    scope-02b Evidence Dashboard — Web UI     [TODO]          ← split
    scope-02c Ops Evidence Agent integration  [TODO]          ← split
    scope-03  Cost Estimate Per Run           [TODO]
```

> **Regla:** no se puede dividir un scope en estado `DONE`. Si el scope ya terminó pero apareció trabajo adicional, usa `/plan-enrich-epic` en cambio.

---

## Coordinar capas producto y ejecución

Cuando el trabajo nuevo es lo suficientemente significativo para merecer una user story permanente en el backlog de producto, el flujo correcto es siempre **product primero, planning después**:

```
# 1. Registrar en el backlog
/us-new epic-NN-slug          → crea US-NNN en docs/

# 2. Agregar al planning activo
/plan-enrich-epic NNN-slug    → crea scope enlazado a US-NNN
```

Si el trabajo es netamente técnico (deuda técnica, refactor, fix de infraestructura) que no corresponde a ninguna user story de producto, solo usar `/plan-enrich-epic` es suficiente — no hay obligación de crear una user story.

---

> [← Tutorial](README.md)

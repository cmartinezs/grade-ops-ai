# Flujo D — Ajustar el planning durante la ejecución

> [← Tutorial](README.md)

**Cuándo usar este flujo:** hay un planning activo en `.planning/active/` y algo cambió — aparece trabajo nuevo, una story resulta ser demasiado vago para ejecutar, una story es mucho más grande de lo esperado, o sus tareas son demasiado gruesas para implementarlas directamente.

Cuatro situaciones, cuatro comandos:

| Situación | Comando |
|-----------|---------|
| [Trabajo nuevo no previsto](#trabajo-nuevo-no-previsto) | `/plan-enrich-epic` |
| [Story mal definida o ambigua](#story-mal-definido) | `/plan-enrich-story` |
| [Story demasiado amplia](#story-demasiado-amplio) | `/plan-split-story` |
| [Tareas demasiado gruesas para ejecutar](#tareas-demasiado-gruesas) | `/plan-atomize` |

---

## Trabajo nuevo no previsto

### Situación A — El trabajo es solo de ejecución

Planning `005-grading-assistance` está en curso. Al implementar story-01 se descubre que falta un mecanismo de **rate limiting** para el Grading Agent que no estaba en ninguna story.

```
/plan-enrich-epic 005-grading-assistance
```

Claude muestra los stories actuales y pregunta qué agregar:

```
Stories actuales de 005-grading-assistance:
  story-01  Rubric-Based Grading Suggestion   [DONE]
  story-02  Uncertainty Flags                 [IN PROGRESS]
  story-03  Teacher Edit of Score             [TODO]
  story-04  Reject AI Suggestion              [TODO]

¿Qué stories nuevos quieres agregar? >
```

```
> story-05-grading-agent-rate-limiting: Add per-teacher rate limiting
  to the Grading Agent to prevent runaway API costs. Area: AP + AG.
  Depends on: story-01.
```

Claude agrega la fila en `01-expansion.md`, crea `story-05-grading-agent-rate-limiting.md` con status `TODO`, y actualiza `active/README.md`.

```
ACTIVE
  005-grading-assistance
    story-01  Rubric-Based Grading Suggestion   [DONE]
    story-02  Uncertainty Flags                 [IN PROGRESS]
    story-03  Teacher Edit of Score             [TODO]
    story-04  Reject AI Suggestion              [TODO]
    story-05  Grading Agent Rate Limiting       [TODO]   ← nuevo
```

### Situación B — El trabajo también necesita product tracking

La misma situación, pero quieres que el requisito quede registrado como user story en `docs/` antes de agregarlo al planning.

```
# Paso 1: registrar en el backlog de producto
/us-new epic-05-grading-assistance

# Sigue el flujo interactivo → genera US-044

# Paso 2: agregar la story al planning activo
/plan-enrich-epic 005-grading-assistance
```

En el segundo comando, menciona `US-044` para que la story quede enlazado:

```
> story-05-grading-agent-rate-limiting — based on US-044
```

El story file generado incluirá la línea `> Source: US-044`.

---

## Story mal definido

Planning `003-rubric-generation` está activo. Al intentar ejecutar story-03, las tareas son tan vagas que no está claro qué hay que implementar exactamente.

```
Story file actual:
  ## Objective
  Implement rubric validation

  ## Tasks
  - [ ] Validate the rubric
  - [ ] Make sure it works

  ## Done Criteria
  - Rubric is validated
```

```
/plan-enrich-story 003-rubric-generation story-03
```

Claude diagnóstica los problemas:

```
story-03-rubric-validation — diagnóstico:

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

Para cada problema detectado, Claude propone y espera confirmación. Al terminar, la story queda con:
- Objetivo preciso
- Tareas con outputs concretos (`RubricValidationService.java`, test class, API error response)
- Done criteria verificables (`weights sum to 100`, `400 response on invalid rubric`)

El status de la story **no cambia** — si estaba en `TODO`, sigue en `TODO`.

---

## Story demasiado amplio

Planning `009-evidence-metrics` incluye story-02, que originalmente se definió como "Implement the full evidence dashboard". Al revisar las tareas, la story tiene 14 ítems mezclando backend, frontend, y agente — demasiado para una sola unidad de trabajo.

```
/plan-split-story 009-evidence-metrics story-02
```

Claude analiza la story y propone una división:

```
story-02 — Evidence Dashboard — análisis:

14 tareas detectadas mezclando:
  - Group A (AP): 5 tareas de backend (endpoints, queries, modelos)
  - Group B (WB): 6 tareas de frontend (componentes, filtros, gráficos)
  - Group C (AG): 3 tareas del Ops Evidence Agent

Propuesta de split:
  story-02a  Evidence Dashboard — API layer      (AP) — 5 tareas
  story-02b  Evidence Dashboard — Web UI         (WB) — 6 tareas, depends: story-02a
  story-02c  Ops Evidence Agent integration      (AG) — 3 tareas, depends: story-02a

¿Confirmas este split, o propones otro? >
```

Al confirmar:
1. Se crean `story-02a`, `story-02b`, `story-02c` con las tareas distribuidas
2. El status de la story original (`IN PROGRESS`) se preserva en `story-02a`; los demás quedan `TODO`
3. Si había tareas ya marcadas `[x]`, se heredan en la story que las contiene
4. `01-expansion.md` se actualiza: la fila de `story-02` se reemplaza por tres filas
5. Las dependencias de stories posteriores que apuntaban a `story-02` se actualizan para apuntar al último del split (`story-02c`)

```
ACTIVE
  009-evidence-metrics
    story-01  Agent Execution Log             [DONE]
    story-02a Evidence Dashboard — API        [IN PROGRESS]   ← split
    story-02b Evidence Dashboard — Web UI     [TODO]          ← split
    story-02c Ops Evidence Agent integration  [TODO]          ← split
    story-03  Cost Estimate Per Run           [TODO]
```

> **Regla:** no se puede dividir una story en estado `DONE`. Si la story ya terminó pero apareció trabajo adicional, usa `/plan-enrich-epic` en cambio.

---

<a id="tareas-demasiado-gruesas"></a>
## Tareas demasiado gruesas para ejecutar

El story está bien definido y tiene el tamaño correcto, pero sus tareas esconden decisiones de diseño, varios entregables, o trabajo sin tests. No hay que dividir la story (es una sola unidad coherente) — hay que **atomizarlo**:

```
/plan-atomize 003-rubric-generation story-03
```

Claude analiza la story y propone el desglose en tareas atómicas:

```
story-03-rubric-validation — propuesta de atomización:

  task-01-validation-rules    Modelo ValidationRule + las 3 reglas del objetivo
  task-02-validation-service  RubricValidationService usando las reglas — depends: task-01
  task-03-api-error-response  Respuesta 400 con detalle de violaciones — depends: task-02

¿Confirmas este desglose, o quieres ajustar? >
```

Al confirmar, cada tarea queda en su propio archivo bajo `02-deepening/story-03-rubric-validation/`, con diseño técnico, pasos de implementación, plan de tests unitarios y done criteria binarios. La tabla de tareas de la story se convierte en el índice. Después:

```
# Ejecutar tarea por tarea
/plan-task 003-rubric-generation story-03 task-01

# O todo la story en orden de dependencias
/plan-story 003-rubric-generation story-03

# Auditar el desglose en cualquier momento (solo lectura)
/plan-task-validate 003-rubric-generation story-03
```

> **Cómo decidir entre los tres comandos de story:** si la story es ambiguo → `/plan-enrich-story`. Si mezcla áreas o es demasiado grande → `/plan-split-story`. Si está claro y bien dimensionado pero sus tareas no son directamente implementables → `/plan-atomize`.

---

## Coordinar capas producto y ejecución

Cuando el trabajo nuevo es lo suficientemente significativo para merecer una user story permanente en el backlog de producto, el flujo correcto es siempre **product primero, planning después**:

```
# 1. Registrar en el backlog
/us-new epic-NN-slug          → crea US-NNN en docs/

# 2. Agregar al planning activo
/plan-enrich-epic NNN-slug    → crea story enlazado a US-NNN
```

Si el trabajo es netamente técnico (deuda técnica, refactor, fix de infraestructura) que no corresponde a ninguna user story de producto, solo usar `/plan-enrich-epic` es suficiente — no hay obligación de crear una user story.

---

> [← Tutorial](README.md)

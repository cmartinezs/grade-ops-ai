# Flujo D — Ajustar el planning durante la ejecución

> [← Tutorial](README.md)

**Cuándo usar este flujo:** hay un planning activo en `.planning/active/` y algo cambió — aparece trabajo nuevo, una story resulta ser demasiado vago para ejecutar, una story es mucho más grande de lo esperado, sus tareas son demasiado gruesas para implementarlas directamente, o pasó algo inesperado que debe alimentar la retrospectiva.

Cinco situaciones, cinco comandos:

| Situación | Comando |
|-----------|---------|
| [Algo inesperado que debe quedar para la retrospectiva](#registrar-un-edge-case) | `/plan-edge-case` |
| [Trabajo nuevo no previsto](#trabajo-nuevo-no-previsto) | `/plan-enrich-epic` |
| [Story mal definida o ambigua](#story-mal-definido) | `/plan-enrich-story` |
| [Story demasiado amplia](#story-demasiado-amplio) | `/plan-split-story` |
| [Tareas demasiado gruesas para ejecutar](#tareas-demasiado-gruesas) | `/plan-atomize` |

---

## Registrar un edge case

Si ocurre algo fuera del flujo esperado y no fue capturado por otro comando, regístralo como nota raw para la retrospectiva:

```bash
/plan-edge-case 005-grading-assistance story-02 -- la validación del agente falló por un límite de rate limit no considerado; se resolvió agregando backoff
```

Esto agrega una entrada en `RETROSPECTIVE-RAW.md`. Al final del planning, `/plan-retrospective` usará estas notas para generar una retrospectiva profesional en `README.md`.

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

## Cambiar de contexto sin perder el trabajo

El plugin detecta automáticamente cuando intentas cambiar de contexto con trabajo en curso y te presenta alternativas seguras antes de tocar nada.

Hay dos situaciones que lo activan:

### Situación E — Iniciar una planificación mientras otra está en progreso

Planning `001-auth-api` tiene story-01 en `IN PROGRESS`. Decides ejecutar una planificación diferente:

```
/plan-run 002-user-dashboard
```

El plugin detecta el conflicto, inspecciona el estado git de la rama activa y muestra:

```
⚠️  Planning context conflict detected

In-progress work:
  Planning : 001-auth-api
  Story    : story-01 — Authentication endpoints
  Branch   : story-01-authentication

Git state on story-01-authentication:
  Modified / staged : 3 files
  Untracked         : 1 file
  Unpushed commits  : 0
  Stashes           : 0

Choose how to proceed:

  A) Abort          — stay on the current planning
  B) Inspect        — show full `git diff` and story file contents; choose again afterwards
  C) Stabilize      — commit pending work as WIP, mark stories STANDBY, return to base branch
  D) Proceed anyway — requires typing CONFIRMAR PROCEDER
```

Si eliges **C (Estabilizar)**:
1. El plugin hace `git add -u` + commit con prefijo `WIP:`
2. Pregunta si quieres hacer push de la rama
3. Marca story-01 con status `STANDBY` en su archivo
4. Vuelve a la rama base
5. Continúa con la ejecución de `002-user-dashboard`

### Situación F — Cambiar de story mientras hay otra en progreso

Estás en la rama `story-01-authentication` (story `IN PROGRESS`). Intentas ejecutar otra story del mismo u otro planning:

```
/plan-story 002-payments story-02
```

El plugin detecta el contexto y muestra:

```
⚠️  Story context conflict detected

Current story : story-01 — Authentication endpoints
Branch        : story-01-authentication
Status        : IN PROGRESS

Git state:
  Staged              : 0 files
  Modified (tracked)  : 4 files
  Untracked           : 2 files
  Unpushed commits    : 1
  Existing stashes    : 0

Choose how to proceed:

  A) Abort              — stay on this story branch
  B) Stash              — stash pending changes, then switch
  C) Commit WIP         — commit tracked changes as WIP, then switch
  D) Commit + Push WIP  — commit and push to remote, then switch
  E) Commit + Push + STANDBY — D plus mark this story as STANDBY
```

La opción **B (Stash)** pregunta adicionalmente si incluir los archivos no trackeados:
```
Include untracked files in the stash? (yes/no)
```
Si respondes `no`, esos archivos quedan en el working tree y se advierten antes de cambiar de rama.

La opción **E** es la más segura cuando no planeas retomar la historia pronto: deja el trabajo guardado, pusheado y el sistema en un estado documentado.

### Retomar una historia en STANDBY

Una historia en `STANDBY` es completamente recuperable. Simplemente vuelve a ejecutarla:

```
/plan-story 001-auth-api story-01
```

El pre-flight detecta que la rama ya existe, pregunta si retomar en ella, y la historia continúa desde donde se detuvo — todas las tareas ya completadas siguen marcadas como `DONE`.

---

> [← Tutorial](README.md)

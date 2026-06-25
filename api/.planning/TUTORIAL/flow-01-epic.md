# Flujo A — Planificar desde un epic existente

> [← Tutorial](README.md)

**Cuándo usar este flujo:** hay un epic en `docs/02-product/user-stories/` con sus stories definidas y quieres convertirlo en un planning ejecutable.

---

## Escenario

Epic 05 — Grading Assistance. Cuatro stories (US-040 a US-043), todas P0, con Story + AC pero sin DoD ni Technical Notes. Se quiere implementar este epic completo.

---

## Paso 1 — Ver qué existe

```
/plan-status
```

```
INITIAL
  (none)

ACTIVE
  (none)

COMPLETED
  (none)
```

Verificar que no hay un planning activo para este epic antes de crear uno.

---

## Paso 2 — Enriquecer las stories

Las stories actuales son delgadas (Story + AC). Para que los stories generados tengan done criteria ejecutables, conviene agregar las secciones de ejecución primero.

El argumento puede ser un path o un ID encontrado en el contenido del archivo:

```bash
# Por path (explícito, siempre funciona)
/us-enrich docs/02-product/user-stories/epic-05-grading-assistance/01-rubric-based-grading-suggestion.md

# Por ID (busca el archivo cuyo contenido contiene "US-040")
/us-enrich US-040
```

Claude lee el archivo, encuentra el epic README en el mismo directorio para tener contexto, y propone valores concretos:

```
Found story: US-040 — Rubric-Based Grading Suggestion
  Narrative:  ✓
  Criteria:   ✓ (6 items)
  DoD:        ✗ missing
  Tech Notes: ✗ missing
  Dependencies: ✗ missing
  Complexity: ✗ missing

Epic context: docs/.../epic-05-grading-assistance/README.md ✓

--- Definition of Done (propuesta) ---
  - [ ] Grading Agent ejecuta sin error para toda submission con rubric aprobada
  - [ ] Score sugerido por criterio está persistido en DB
  - [ ] AgentExecutionLog registrado
  - [ ] Output marcado como suggestion=true
  - [ ] UI muestra score sugerido con evidence snippet

¿Acepta, edita, o skip? >
```

Repetir para cada story del epic:

```
/us-enrich US-041
/us-enrich US-042
/us-enrich US-043
```

> **Tip:** puedes saltar este paso. `/plan-from-epic` genera stories incluso para stories sin DoD — los done criteria quedarán como `[inferred]` y el reporte final listará cuáles necesitan `/us-enrich`.

---

## Paso 3 — Generar el planning

```bash
# Pasar el path del directorio del epic
/plan-from-epic 005 docs/02-product/user-stories/epic-05-grading-assistance/

# Solo P0 (usando --filter sobre el campo Priority encontrado en las stories)
/plan-from-epic 005 docs/02-product/user-stories/epic-05-grading-assistance/ --filter priority=P0
```

Claude detecta los 4 archivos de stories en el directorio, lee el README del epic para contexto, y genera de una sola vez:

**`00-initial.md`** — poblado desde el epic README:
- Intent: "Deliver AI-assisted grading suggestions against the approved rubric, with explicit teacher control at every decision point."
- Why: narrativa del epic (as a teacher / I want / so that)
- Approximate Scope: `api/` + `agents/` + `web/`

**`01-expansion.md`** — una fila por story:

| Story ID | Story | Area | Depends on |
|-------|-------|------|------------|
| story-01 | US-040 Rubric-Based Grading Suggestion | AP + AG | — |
| story-02 | US-041 Uncertainty Flags | AP + AG | story-01 |
| story-03 | US-042 Teacher Edit of Score | AP + WB | story-01 |
| story-04 | US-043 Reject AI Suggestion | AP + WB | story-01 |

**`02-deepening/story-01-rubric-based-grading-suggestion.md`** — ejemplo:

```markdown
## Objective
As a teacher, I want grading suggestions tied to rubric criteria
so I can review rather than score from scratch.

> Source: docs/02-product/user-stories/epic-05-grading-assistance/
>         01-rubric-based-grading-suggestion.md — US-040

## Status: TODO

## Done Criteria
- [ ] Grading Agent ejecuta sin error para toda submission con rubric aprobada
- [ ] Score sugerido por criterio está persistido en DB
- [ ] AgentExecutionLog registrado
- [ ] Output marcado como suggestion=true
- [ ] UI muestra score sugerido con evidence snippet

## Tasks
- [ ] Define implementation tasks  ← a completar con /plan-enrich-story o /plan-story
```

El planning aparece directamente en `active/` — no pasa por INITIAL porque el epic ya contiene la expansión completa.

**`/plan-status` después:**

```
ACTIVE
  005-grading-assistance — Grading Assistance (Epic 05)
    story-01-rubric-based-grading-suggestion  [TODO]
    story-02-uncertainty-flags                [TODO]
    story-03-teacher-edit-score               [TODO]
    story-04-reject-ai-suggestion             [TODO]
```

---

## Paso 4 — Ejecutar los stories

### Opcional — atomizar antes de ejecutar

Si las tareas de una story son demasiado gruesas para implementarlas directamente:

```
/plan-atomize 005-grading-assistance story-01
```

Claude descompone la story en tareas atómicas — un archivo por tarea bajo `02-deepening/story-01-*/`, cada una con diseño técnico, pasos de implementación, tests unitarios y done criteria. Luego puedes ejecutarlas una a una:

```
/plan-task 005-grading-assistance story-01 task-01
```

o todas en orden con `/plan-story`, que detecta la story atomizado.

### Ejecutar la story completo

```
/plan-story 005-grading-assistance story-01
```

Claude lee la story, verifica los contratos de `docs/`, y ejecuta cada tarea. Al terminar la story:

```
/plan-story 005-grading-assistance story-02
/plan-story 005-grading-assistance story-03
/plan-story 005-grading-assistance story-04
```

Si prefieres ejecutar las tareas a mano y solo marcar la story como hecho:

```
/plan-done 005-grading-assistance story-01
```

---

## Paso 5 — Archivar

Cuando todos los stories están en `DONE`, valida la integridad estructural antes de cerrar:

```
/plan-validate 005-grading-assistance
```

Si el reporte sale limpio (sin FAIL), archiva:

```
/plan-archive 005-grading-assistance
```

Claude audita (todos los stories DONE, tareas con output, TRACEABILITY lleno, sección Retrospective presente) y mueve el planning a `finished/`.

---

## Variante: solo P0

`--filter` coincide con cualquier campo de metadata en las stories. Para filtrar por prioridad:

```bash
/plan-from-epic 005 docs/02-product/user-stories/epic-05-grading-assistance/ --filter priority=P0
```

Las stories P1/P2 quedan en el epic como backlog, sin story en el planning.

---

## Variante: story aparece demasiado grande

Si durante la ejecución story-01 resulta ser demasiado amplio:

```
/plan-split-story 005-grading-assistance story-01
```

Ver [Flujo D](flow-04-mid-execution.md#story-demasiado-amplio) para el detalle completo.

---

> [← Tutorial](README.md)

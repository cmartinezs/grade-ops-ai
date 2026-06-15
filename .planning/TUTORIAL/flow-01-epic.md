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

Las stories actuales son delgadas (Story + AC). Para que los scopes generados tengan done criteria ejecutables, conviene agregar las secciones de ejecución primero.

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

> **Tip:** puedes saltar este paso. `/plan-from-epic` genera scopes incluso para stories sin DoD — los done criteria quedarán como `[inferred]` y el reporte final listará cuáles necesitan `/us-enrich`.

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

| Scope | Story | Area | Depends on |
|-------|-------|------|------------|
| scope-01 | US-040 Rubric-Based Grading Suggestion | AP + AG | — |
| scope-02 | US-041 Uncertainty Flags | AP + AG | scope-01 |
| scope-03 | US-042 Teacher Edit of Score | AP + WB | scope-01 |
| scope-04 | US-043 Reject AI Suggestion | AP + WB | scope-01 |

**`02-deepening/scope-01-rubric-based-grading-suggestion.md`** — ejemplo:

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
- [ ] Define implementation tasks  ← a completar con /plan-enrich-story o /plan-scope
```

El planning aparece directamente en `active/` — no pasa por INITIAL porque el epic ya contiene la expansión completa.

**`/plan-status` después:**

```
ACTIVE
  005-grading-assistance — Grading Assistance (Epic 05)
    scope-01-rubric-based-grading-suggestion  [TODO]
    scope-02-uncertainty-flags                [TODO]
    scope-03-teacher-edit-score               [TODO]
    scope-04-reject-ai-suggestion             [TODO]
```

---

## Paso 4 — Ejecutar los scopes

```
/plan-scope 005-grading-assistance scope-01
```

Claude lee el scope, verifica los contratos de `docs/`, y ejecuta cada tarea. Al terminar el scope:

```
/plan-scope 005-grading-assistance scope-02
/plan-scope 005-grading-assistance scope-03
/plan-scope 005-grading-assistance scope-04
```

Si prefieres ejecutar las tareas a mano y solo marcar el scope como hecho:

```
/plan-done 005-grading-assistance scope-01
```

---

## Paso 5 — Archivar

Cuando todos los scopes están en `DONE`, valida la integridad estructural antes de cerrar:

```
/plan-validate 005-grading-assistance
```

Si el reporte sale limpio (sin FAIL), archiva:

```
/plan-archive 005-grading-assistance
```

Claude audita (todos los scopes DONE, tareas con output, TRACEABILITY lleno, sección Retrospective presente) y mueve el planning a `finished/`.

---

## Variante: solo P0

`--filter` coincide con cualquier campo de metadata en las stories. Para filtrar por prioridad:

```bash
/plan-from-epic 005 docs/02-product/user-stories/epic-05-grading-assistance/ --filter priority=P0
```

Las stories P1/P2 quedan en el epic como backlog, sin scope en el planning.

---

## Variante: story aparece demasiado grande

Si durante la ejecución scope-01 resulta ser demasiado amplio:

```
/plan-split-story 005-grading-assistance scope-01
```

Ver [Flujo D](flow-04-mid-execution.md#scope-demasiado-amplio) para el detalle completo.

---

> [← Tutorial](README.md)

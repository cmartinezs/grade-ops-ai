# Flujo C — Refinar el backlog de producto

> [← Tutorial](README.md)

**Cuándo usar este flujo:** no hay intención inmediata de ejecutar — el objetivo es mejorar la calidad del backlog. Esto ocurre antes de una sesión de planning, después de entrevistas con usuarios, o cuando un epic va a ser planificado pronto y sus stories son demasiado delgadas para estimar.

> Este flujo trabaja **solo en `docs/02-product/user-stories/`**. No toca `.planning/`.

---

## Escenario A — Enriquecer stories antes de planificar

Epic 11 (Question Bank and Closed Assessment) tiene 6 stories con P0/P1. Antes de generar el planning, quieres que todas las P0 tengan DoD y Technical Notes definidos.

### Identificar qué stories enriquecer

Lee el epic README para ver qué hay:

```
docs/02-product/user-stories/epic-11-question-bank-closed-assessment/
  README.md
  01-generate-question-batch.md   ← US-100 P0
  02-review-generated-questions.md ← US-101 P0
  03-question-bank.md              ← US-102 P0
  04-compose-closed-assessment.md  ← US-103 P0
  05-publish-freeze-snapshot.md    ← US-104 P0
  06-annul-question-recalculate.md ← US-105 P1
```

### Enriquecer una por una

```
/us-enrich US-100
```

Claude detecta qué secciones faltan, propone valores inferidos del epic + story, y espera confirmación por sección:

```
US-100 — Generate Question Batch With AI

Secciones faltantes:
  ✗ Definition of Done
  ✗ Technical Notes
  ✗ Dependencies
  ✗ Complexity

--- Definition of Done (propuesta) ---
  - [ ] Question Generation Agent produce batch de N preguntas (TF/SC/MC)
  - [ ] Cada pregunta tiene: enunciado, alternativas, answer key, explanation, difficulty
  - [ ] Preguntas entran en estado pending_review (no active todavía)
  - [ ] Distractor Quality Agent y Ambiguity Review Agent ejecutan automáticamente
  - [ ] Flags de calidad visibles en la cola de revisión
  - [ ] AgentExecutionLog registrado para los 3 agentes

¿Acepta, edita, o skip? >
```

Continúa para cada story P0:

```
/us-enrich US-101
/us-enrich US-102
/us-enrich US-103
/us-enrich US-104
```

La P1 (US-105) puede esperar — no bloquea el planning inicial.

---

## Escenario B — Agregar una story nueva a un epic

En una entrevista con un docente surge que necesita **exportar** el question bank a PDF para repartir copias físicas. Esta funcionalidad no existe en ninguna story del epic.

```
/us-new epic-11-question-bank-closed-assessment
```

Claude lee el epic y las stories existentes, luego conduce el flujo interactivo:

```
[1/8] ¿Cómo se llama esta user story? (título corto)
> Export Question Bank to PDF

[2/8] ¿Prioridad? (P0 / P1 / P2 / Out)
> P2

[3/8] Enunciado: "As a [role], I want [feature] so I can [benefit]."
> As a teacher, I want to export selected questions to a PDF so I can
  distribute physical copies to students without internet access.

[4/8] Acceptance Criteria (uno por línea, Enter en blanco para terminar)
> Teacher can select a subset of questions from the bank
> Teacher can choose export format: one-per-page or compact
> PDF is generated with question text, options, and a blank answer space
> Answer key PDF is generated separately (teacher-only)
>

[5/8] ¿Definition of Done ahora? (sí / skip)
> skip

[6/8] ¿Áreas de repos?
> api/ (PDF generation endpoint), web/ (export UI)

[7/8] ¿Dependencias?
> US-102 (Question Bank must exist first)

[8/8] ¿Complejidad?
> M
```

Claude escribe `07-export-question-bank-pdf.md` (US-106), actualiza el epic README y el README global.

---

## Escenario C — El epic completo necesita más cobertura

Epic 09 (Evidence and Metrics) tiene 4 stories. Quieres ver si hay gaps antes de planificarlo.

```
/epic-enrich epic-09-evidence-metrics
```

Claude lee el epic completo y todas sus stories, luego presenta:

```
Epic 09 — Evidence and Metrics
Stories existentes: US-080, US-081, US-082, US-083

Gaps identificados:
1. No hay story para exportar evidencia a un formato externo (CSV/JSON)
   útil para el formulario de hackathon o auditoría externa.
2. No hay story para ver el dashboard de costos acumulados (UsageEvent + CostEvent)
   — el epic menciona cost tracking pero ninguna story lo implementa para el teacher.
3. No hay story para registrar RevenueEvent cuando un teacher activa un plan pago.

¿Cuáles quieres agregar? (números separados por coma, o "ninguno") >
```

Seleccionas los gaps que aplican, y para cada uno Claude conduce el flujo `/us-new`.

---

## Escenario D — Story existente está mal escrita

US-002 (Assessment Dashboard) fue escrita apresuradamente y sus AC son vagos:

```
## Acceptance Criteria
- Teacher can see their assessments.
- Teacher can create a new assessment.
```

```
/us-enrich US-002
```

Además de agregar las secciones faltantes (DoD, Technical Notes), Claude puede proponer AC más precisos si los actuales son demasiado vagos — pero solo los reemplaza si el usuario confirma explícitamente.

```
AC actuales detectados como vagos (sin comportamiento observable):
  - "Teacher can see their assessments" — ¿qué ve? ¿qué orden? ¿qué estados?
  - "Teacher can create a new assessment" — ¿qué campos? ¿qué validaciones?

Propuesta de AC enriquecidos:
  - [ ] Dashboard muestra lista de assessments del teacher en sesión,
        ordenados por fecha de modificación descendente
  - [ ] Cada assessment muestra: título, estado (draft/active/archived),
        tipo (open/closed), y fecha
  - [ ] Teacher puede filtrar por estado y tipo
  - [ ] Botón "New Assessment" navega al flujo de creación (US-010)
  - [ ] Teacher no ve assessments de otros teachers

¿Reemplaza los AC actuales con estos, los agrega como sugerencia, o skip? >
```

---

## Cuándo ir del backlog a la ejecución

Una vez que las stories están enriquecidas, el siguiente paso natural es:

```
/plan-from-epic NNN epic-NN-slug
```

Ver [Flujo A](flow-01-epic.md) para el detalle completo.

---

> [← Tutorial](README.md)

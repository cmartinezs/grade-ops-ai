# Referencia rápida

> [← Tutorial](README.md)

Todos los comandos disponibles con su sintaxis exacta.

---

## Capa de producto — cualquier container de stories

```bash
# Crear una story nueva (interactivo por defecto)
# container = directorio con story files O archivo markdown con secciones de story
/us-new path/to/container/
/us-new path/to/container/ --blank     # plantilla vacía, sin preguntas

# Enriquecer una story (agrega DoD, Technical Notes, Dependencies, Complexity)
# acepta path o cualquier ID encontrado en el contenido / nombre del archivo
/us-enrich path/to/story.md
/us-enrich US-040                      # busca el archivo que contiene "US-040"
/us-enrich story-3                     # busca por nombre parcial si es único

# Ampliar un container con nuevas stories (detecta gaps, flujo interactivo)
/epic-enrich path/to/container/
```

---

## Puente producto → ejecución

```bash
# Genera planning completo en active/ (1 story = 1 user story)
# container puede ser directorio o archivo con secciones de story
/plan-from-epic NNN path/to/container/
/plan-from-epic NNN path/to/container/ --filter priority=P0
/plan-from-epic NNN path/to/requirements.md --filter status=approved
```

---

## Flujo general (sin epic de producto)

```bash
# Capturar idea antes de planificar
/plan-template slug                    # interactivo — 6 preguntas
/plan-template slug --blank            # plantilla vacía en .planning/ideas/slug.md

# Crear el planning
/plan-new NNN-slug -- intent text      # intent inline, llena solo el campo Intent
/plan-new NNN-slug @path/to/idea.md    # desde documento, llena todo lo que encuentre

# Dimensionar (solo para plannings creados con /plan-new)
/plan-expand NNN-slug
```

---

## Planificación de releases

```bash
# Inicializar gestión de releases (una vez por proyecto, independiente de /plan-init)
/release-init

# Crear nueva release
/release-new v1.0.0 -- Primer release público con autenticación JWT

# Agregar plannings a la release
/release-add v1.0.0 001-user-auth-api 002-dashboard

# Eliminar un planning de la release
/release-remove v1.0.0 002-dashboard

# Ver estado de todas las releases (lee .planning/ en tiempo real)
/release-status

# Ver detalle de una release
/release-status v1.0.0

# Transicionar estado de una release
/release-status v1.0.0 --mark-planned
/release-status v1.0.0 --mark-in-progress
/release-status v1.0.0 --mark-blocked
/release-status v1.0.0 --mark-released
/release-status v1.0.0 --mark-cancelled
```

---

## Configuración git

```bash
# Ver configuración git actual (.planning/config.yml)
/plan-git-config

# Establecer o cambiar la rama base
/plan-git-config --base-branch develop
/plan-git-config --base-branch main
```

> Útil en proyectos que ya tienen `.planning/` inicializado antes de que existiera la configuración git. `/plan-init` configura esto automáticamente en proyectos nuevos.

---

## Ciclo de vida del planning

```bash
# Ver estado de todos los plannings
/plan-status

# Recomendar el siguiente comando seguro
/plan-status [NNN-slug]

# Validar integridad estructural (solo lectura: ubicación, stories, workflows, dependencias)
/plan-validate                         # todos los plannings
/plan-validate NNN-slug                # solo uno

# Descomponer una story en tareas atómicas (diseño técnico + implementación + tests unitarios por tarea)
/plan-atomize NNN-slug story-NN

# Ejecutar una sola tarea atómica
/plan-task NNN-slug story-NN task-NN

# Auditar tareas atómicas contra el checklist de atomicidad (solo lectura)
/plan-task-validate NNN-slug [story-NN] [task-NN]

# Ejecutar todas las tareas de una story
# → crea rama de story desde base; cada task usa rama propia y PR hacia la story; limpia ramas locales mergeadas; al final abre PR story→base
/plan-story NNN-slug story-NN

# Marcar story completo (verifica done criteria y avanza al siguiente)
# → requiere PRs de task mergeados; abre PR final story→base si corresponde; tras el merge final limpia la rama local de story
/plan-done NNN-slug story-NN

# Marcar solo una tarea (no avanza la story)
/plan-done NNN-slug story-NN task-N

# Generar retrospectiva final
/plan-retrospective NNN-slug

# Auditar y archivar a finished/
/plan-archive NNN-slug
```

---

## Enriquecimiento del planning (solo en plannings ACTIVE)

```bash
# Agregar stories nuevos al planning
/plan-enrich-epic NNN-slug

# Profundizar una story incompleta o ambigua (no cambia su status)
/plan-enrich-story NNN-slug story-NN

# Dividir una story demasiado amplia en varias (solo en TODO o IN PROGRESS)
/plan-split-story NNN-slug story-NN
```

---

## Pipeline autónomo con agentes

```bash
# Orquestador: detecta estado, confirma una vez, delega a agentes fase a fase
/plan-run NNN-slug                 # retoma desde el estado actual
/plan-run "descripción"            # crea un planning nuevo y ejecuta todo el ciclo
/plan-run                          # sin argumento: lista plannings activos y pregunta cuál correr

# Agentes de fase (invocables de forma independiente)
/plan-agent-plan NNN-slug          # INITIAL → EXPANSION sin interrupciones
/plan-agent-execute NNN-slug       # atomiza + ejecuta todos los stories pendientes (paralelo)
/plan-agent-validate NNN-slug      # valida integridad + plan-done + plan-archive
```

---

## Regla fundamental y bypass

Nada se ejecuta sin estar dentro de un planning activo.

```bash
/cualquier-comando --no-plan           # pide confirmación antes de ejecutar sin planning
/cualquier-comando --no-plan-force     # ejecuta directamente sin preguntar
```

---

## Tabla de decisión rápida

| Situación | Comando |
|-----------|---------|
| Inicializar gestión de releases | `/release-init` |
| Crear nueva release | `/release-new vX.Y.Z -- purpose` |
| Agregar plannings a una release | `/release-add vX.Y.Z NNN-slug` |
| Ver estado de todas las releases | `/release-status` |
| Publicar una release | `/release-status vX.Y.Z --mark-released` |
| Configurar rama base en un proyecto existente | `/plan-git-config --base-branch <branch>` |
| Ver configuración git actual | `/plan-git-config` |
| Configurar smoke tests del proyecto | `/plan-smoke-config [--blank]` |
| Crear una user story nueva | `/us-new epic-NN-slug` |
| Story existe pero le faltan secciones de ejecución | `/us-enrich US-NNN` |
| Epic necesita más stories | `/epic-enrich epic-NN-slug` |
| Convertir un epic en planning ejecutable | `/plan-from-epic NNN epic-NN-slug` |
| Planificar trabajo técnico sin epic | `/plan-template` → `/plan-new` → `/plan-expand` |
| Ver todos los plannings activos | `/plan-status` |
| Revisar el estado antes de decidir | `/plan-status [NNN-slug]` |
| Verificar que un planning está bien formado | `/plan-validate NNN-slug` |
| Las tareas de una story son muy gruesas para ejecutar | `/plan-atomize NNN-slug story-NN` |
| Ejecutar una sola tarea atómica | `/plan-task NNN-slug story-NN task-NN` |
| Verificar que las tareas atómicas están bien formadas | `/plan-task-validate NNN-slug story-NN` |
| Ejecutar una story | `/plan-story NNN-slug story-NN` |
| Trabajo nuevo apareció durante ejecución | `/plan-enrich-epic NNN-slug` |
| Story del planning está mal especificado | `/plan-enrich-story NNN-slug story-NN` |
| Story del planning es demasiado grande | `/plan-split-story NNN-slug story-NN` |
| Story nueva de backlog + story nueva de planning coordinadas | `/us-new` → `/plan-enrich-epic` |
| Algo raro ocurrió fuera de un comando del plugin | `/plan-edge-case NNN-slug -- nota` |
| Preparar retrospectiva final | `/plan-retrospective NNN-slug` |
| Workspace `2.x` antiguo | `/plan-update-version 2.1.0 3.5.0 --dry-run` → `/plan-update-version 2.1.0 3.5.0` |
| Cerrar el planning | `/plan-retrospective NNN-slug` → `/plan-archive NNN-slug` |
| Ejecutar todo el ciclo sin intervención | `/plan-run NNN-slug` |
| Solo la fase de planificación autónoma | `/plan-agent-plan NNN-slug` |
| Solo la ejecución paralela de stories | `/plan-agent-execute NNN-slug` |
| Solo la validación y cierre autónomo | `/plan-agent-validate NNN-slug` |
| Iniciar planificación con otra en curso → pausa automática | detección automática en `/plan-run` y `/plan-agent-execute` |
| Cambiar de story con otra en progreso → pausa automática | detección automática en `/plan-story` |
| Retomar una story en STANDBY | `/plan-story NNN-slug story-NN` |
| Auditar documentación generada por un planning | `/plan-audit-docs NNN-slug` |
| Auditar este plugin o plantilla instalada | `/plan-doctor` |

---

## Comandos parecidos: cuál usar

| Si estás trabajando en... | Usa | No usa |
|---------------------------|-----|--------|
| Backlog o stories fuente | `/us-enrich`, `/us-split`, `/epic-enrich` | `/plan-enrich-story`, `/plan-split-story` |
| Planning activo en `.planning/active/` | `/plan-enrich-story`, `/plan-split-story`, `/plan-enrich-epic` | `/us-enrich`, `/us-split` |
| Chequeo global de `.planning/` | `/plan-health` | `/plan-validate` |
| Auditoría detallada de un planning | `/plan-validate NNN-slug` | `/plan-health` |
| Revisar el estado actual | `/plan-status` | `/plan-health` |
| Comunicación diaria | `/plan-standup` | `/plan-report` |
| Resumen ejecutivo | `/plan-report` | `/plan-standup` |
| Artefacto externo para PR/tickets/issues/docs | `/plan-export` | `/plan-report` |
| Cobertura/frescura de docs | `/plan-audit-docs` | `/doc-generate` |
| Estado de una entrega/version | `/release-status` | `/plan-status` |

---

## Formatos de argumento

| Comando | Formato |
|---------|---------|
| `/us-new` | `path/to/container/ [--interactive\|--blank]` |
| `/us-enrich` | `path/to/story.md` ó ID/nombre parcial |
| `/epic-enrich` | `path/to/container/` ó `path/to/file.md` |
| `/plan-from-epic` | `NNN path/to/container/ [--filter field=value]` |
| `/plan-template` | `slug [--interactive\|--blank]` |
| `/plan-new` | `NNN-slug -- intent` ó `NNN-slug @path.md` |
| `/plan-expand` | `NNN-slug` |
| `/plan-status` | `[NNN-slug]` |
| `/plan-validate` | `[NNN-slug]` (vacío = todos) |
| `/plan-atomize` | `NNN-slug story-NN` |
| `/plan-task` | `NNN-slug story-NN task-NN` |
| `/plan-task-validate` | `NNN-slug [story-NN] [task-NN]` |
| `/release-init` | *(sin argumentos)* |
| `/release-new` | `vX.Y.Z -- <purpose>` |
| `/release-add` | `vX.Y.Z NNN-slug [NNN-slug ...]` |
| `/release-remove` | `vX.Y.Z NNN-slug` |
| `/plan-audit-docs` | `NNN-slug [--docs-dir <path>]` |
| `/plan-doctor` | `[--plugin-root <path>]` |
| `/release-status` | `[vX.Y.Z] [--mark-planned\|--mark-in-progress\|--mark-blocked\|--mark-released\|--mark-cancelled]` |
| `/plan-git-config` | `[--base-branch <branch>]` (vacío = mostrar config actual) |
| `/plan-smoke-config` | `[--blank]` (vacío = generar con preguntas e inferencia) |
| `/plan-story` | `NNN-slug story-NN` |
| `/plan-done` | `NNN-slug story-NN` ó `NNN-slug story-NN task-N` |
| `/plan-edge-case` | `[NNN-slug] [story-NN] -- nota` |
| `/plan-retrospective` | `NNN-slug` |
| `/plan-update-version` | `<from> <to> [--dry-run] [--allow-dirty]` |
| `/plan-archive` | `NNN-slug` |
| `/plan-enrich-epic` | `NNN-slug` |
| `/plan-enrich-story` | `NNN-slug story-NN` |
| `/plan-split-story` | `NNN-slug story-NN` |
| `/plan-run` | `[NNN-slug]` ó `"descripción libre"` (vacío = lista plannings) |
| `/plan-agent-plan` | `NNN-slug` ó `"descripción libre"` |
| `/plan-agent-execute` | `NNN-slug` |
| `/plan-agent-validate` | `NNN-slug` |

**Resolución de argumentos en comandos de producto:**
Los comandos `us-enrich`, `us-new`, `epic-enrich`, y `plan-from-epic` reciben rutas — no IDs ni slugs hardcodeados. Cuando se pasa un ID (`US-040`) o nombre parcial en lugar de ruta, el comando busca recursivamente desde el directorio actual el archivo cuyo contenido o nombre coincida.

---

> [← Tutorial](README.md)

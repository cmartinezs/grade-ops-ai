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
# Genera planning completo en active/ (1 story = 1 scope)
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

## Ciclo de vida del planning

```bash
# Ver estado de todos los plannings
/plan-status

# Validar integridad estructural (solo lectura: ubicación, scopes, workflows, dependencias)
/plan-validate                         # todos los plannings
/plan-validate NNN-slug                # solo uno

# Ejecutar todas las tareas de un scope
/plan-scope NNN-slug scope-NN

# Marcar scope completo (verifica done criteria y avanza al siguiente)
/plan-done NNN-slug scope-NN

# Marcar solo una tarea (no avanza el scope)
/plan-done NNN-slug scope-NN task-N

# Auditar y archivar a finished/
/plan-archive NNN-slug
```

---

## Enriquecimiento del planning (solo en plannings ACTIVE)

```bash
# Agregar scopes nuevos al planning
/plan-enrich-epic NNN-slug

# Profundizar un scope incompleto o ambiguo (no cambia su status)
/plan-enrich-story NNN-slug scope-NN

# Dividir un scope demasiado amplio en varios (solo en TODO o IN PROGRESS)
/plan-split-story NNN-slug scope-NN
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
| Crear una user story nueva | `/us-new epic-NN-slug` |
| Story existe pero le faltan secciones de ejecución | `/us-enrich US-NNN` |
| Epic necesita más stories | `/epic-enrich epic-NN-slug` |
| Convertir un epic en planning ejecutable | `/plan-from-epic NNN epic-NN-slug` |
| Planificar trabajo técnico sin epic | `/plan-template` → `/plan-new` → `/plan-expand` |
| Ver todos los plannings activos | `/plan-status` |
| Verificar que un planning está bien formado | `/plan-validate NNN-slug` |
| Ejecutar un scope | `/plan-scope NNN-slug scope-NN` |
| Trabajo nuevo apareció durante ejecución | `/plan-enrich-epic NNN-slug` |
| Scope del planning está mal especificado | `/plan-enrich-story NNN-slug scope-NN` |
| Scope del planning es demasiado grande | `/plan-split-story NNN-slug scope-NN` |
| Story nueva + scope nuevo coordinados | `/us-new` → `/plan-enrich-epic` |
| Cerrar el planning | `/plan-archive NNN-slug` |

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
| `/plan-validate` | `[NNN-slug]` (vacío = todos) |
| `/plan-scope` | `NNN-slug scope-NN` |
| `/plan-done` | `NNN-slug scope-NN` ó `NNN-slug scope-NN task-N` |
| `/plan-archive` | `NNN-slug` |
| `/plan-enrich-epic` | `NNN-slug` |
| `/plan-enrich-story` | `NNN-slug scope-NN` |
| `/plan-split-story` | `NNN-slug scope-NN` |

**Resolución de argumentos en comandos de producto:**
Los comandos `us-enrich`, `us-new`, `epic-enrich`, y `plan-from-epic` reciben rutas — no IDs ni slugs hardcodeados. Cuando se pasa un ID (`US-040`) o nombre parcial en lugar de ruta, el comando busca recursivamente desde el directorio actual el archivo cuyo contenido o nombre coincida.

---

> [← Tutorial](README.md)

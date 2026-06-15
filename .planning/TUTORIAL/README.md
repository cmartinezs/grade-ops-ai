# Tutorial: Flujos de Planning

> [← .planning/README.md](../README.md)

Guías por escenario real. Cada archivo cubre una situación concreta de principio a fin.

---

## ¿Qué flujo necesitas?

| Situación | Archivo |
|-----------|---------|
| Quiero planificar la implementación de un epic | [→ Flujo A](flow-01-epic.md) |
| Tengo trabajo transversal que no pertenece a ningún epic | [→ Flujo B](flow-02-general.md) |
| Quiero refinar el backlog sin ejecutar nada todavía | [→ Flujo C](flow-03-backlog.md) |
| El planning ya está activo y necesito ajustarlo | [→ Flujo D](flow-04-mid-execution.md) |
| Solo quiero la referencia rápida de comandos | [→ Referencia](reference.md) |

---

## Mapa del sistema

Dos capas conectadas por un puente:

```
docs/02-product/user-stories/       ← PRODUCTO (qué y por qué)
  epic-NN-slug/
    README.md                       ← narrativa, goal, AC del epic, DoD
    NN-story-name.md                ← US-NNN: story + AC + DoD + Technical Notes

          ↓  /plan-from-epic  ↓

.planning/active/                   ← EJECUCIÓN (cómo y cuándo)
  NNN-slug/
    00-initial.md
    01-expansion.md                 ← 1 fila por user story
    02-deepening/
      scope-NN-story-name.md        ← done criteria = AC + DoD de la story
```

---

## Comandos disponibles

**Capa de producto** — cualquier directorio o archivo con stories:

| Comando | Argumento | Qué hace |
|---------|-----------|----------|
| `/us-new` | `path/to/container` | Crea una story nueva en ese container |
| `/us-enrich` | `path/to/story.md` o ID | Agrega DoD, Technical Notes, Dependencies, Complexity |
| `/epic-enrich` | `path/to/container` | Detecta gaps y agrega stories nuevas |

**Puente:**

| Comando | Argumento | Qué hace |
|---------|-----------|----------|
| `/plan-from-epic` | `NNN path/to/container` | Genera planning activo completo (1 story = 1 scope) |

Los comandos leen la estructura que encuentran y se adaptan — no requieren una jerarquía de carpetas específica ni un formato de ID fijo. Para GradeOps AI el container es `docs/02-product/user-stories/epic-NN-slug/`; en otro proyecto podría ser `features/checkout/` o `requirements.md`.

**Ciclo de vida del planning:**

| Comando | Qué hace |
|---------|----------|
| `/plan-template slug` | Genera documento de idea en `.planning/ideas/` |
| `/plan-new NNN-slug -- intent` | Crea planning en INITIAL (inline) |
| `/plan-new NNN-slug @path.md` | Crea planning en INITIAL desde documento |
| `/plan-status` | Estado de todos los plannings |
| `/plan-expand NNN-slug` | INITIAL → EXPANSION (solo flujo general) |
| `/plan-scope NNN-slug scope-NN` | Ejecuta todas las tareas de un scope |
| `/plan-done NNN-slug scope-NN` | Marca scope completo y avanza |
| `/plan-done NNN-slug scope-NN task-N` | Marca una tarea específica |
| `/plan-archive NNN-slug` | Audita y archiva a `finished/` |

**Enriquecimiento del planning (plannings ACTIVE):**

| Comando | Qué hace |
|---------|----------|
| `/plan-enrich-epic NNN-slug` | Agrega scopes nuevos al planning |
| `/plan-enrich-story NNN-slug scope-NN` | Profundiza un scope incompleto o ambiguo |
| `/plan-split-story NNN-slug scope-NN` | Divide un scope demasiado amplio |

---

> [← .planning/README.md](../README.md)

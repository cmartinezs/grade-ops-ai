# Code Review Evidence

Documentación estructurada de verificación para cada tarea/PR. Facilita el code review directo a la evidencia sin búsqueda.

## Estructura

```
docs/code-review-evidence/
├── {epic-id}-{epic-name}/
│   ├── {story-id}-{story-name}/
│   │   └── {task-id}-{task-name}/
│   │       ├── EVIDENCE.md          ← Resultados de verificación
│   │       ├── HOW_TO_VERIFY.md     ← Guía para reproducir
│   │       ├── PROMPT.md            ← Prompt para PR/MR
│   │       └── logs/
│   │           ├── test-results.log
│   │           ├── build-results.log
│   │           └── lint-results.json
```

## Tareas con Evidencia

| Tarea | Estado | Verificación |
|-------|--------|--------------|
| **001/story-02/task-09** | ✅ Complete | [Evidencia](./001-assessment-creation/story-02/task-09/) |
| **001/story-02/task-10** | ✅ Complete | [Evidencia](./001-assessment-creation/story-02/task-10/) |
| **001/story-02/task-11** | ✅ Complete | [Evidencia](./001-assessment-creation/story-02/task-11/) |

## Cómo Usar

1. **Para código reviewer:**
   - Abre la carpeta de la tarea
   - Lee `PROMPT.md` (resumen conciso)
   - Lee `EVIDENCE.md` (detalle completo)
   - Usa `HOW_TO_VERIFY.md` para reproducir localmente

2. **Para agregar evidencia de una nueva tarea:**
   - Crea carpeta siguiendo estructura
   - Ejecuta tests, build, lint
   - Genera logs en `logs/`
   - Escribe `EVIDENCE.md` y `HOW_TO_VERIFY.md`
   - Copia `PROMPT.md` a la PR

3. **Para auditorías futuras:**
   - Los logs quedan documentados
   - Se puede ver histórico de verificación
   - No hay dudas sobre qué se testó

## Convención de Nombrado

- `{epic-id}` = `001` (001-assessment-creation)
- `{story-id}` = `story-02` (assessment-screens-wireframes-and-data-providers)
- `{task-id}` = `task-09` (functional-mockup-draft-builder-screen)

El nombre completo refiere directamente al planning en `.planning/`

## Archivo PROMPT.md

Cada carpeta de tarea debe tener `PROMPT.md` con:
- Resumen de cambios (3 líneas)
- Comandos de verificación (copy-paste ready)
- Resultados esperados
- Links a evidencia detallada

**Esto es lo que se copia directo a la PR.**

## Archivo EVIDENCE.md

Documento completo con:
- Qué se implementó (archivos, líneas de código)
- Qué se testea (por cada test)
- Output completo de tests/build/lint
- Checklist para reviewers
- Recomendación (READY TO MERGE / NEEDS WORK)

## Archivo HOW_TO_VERIFY.md

Guía step-by-step:
- Quick verification (5 min)
- Detailed verification (15 min)
- Comandos exactos para copiar
- Troubleshooting
- Checklist interactivo

## Histórico

Cada tarea con evidencia documenta:
- Cuándo se verificó (fecha en commit)
- Quién verificó (git author)
- Qué se testó (nombres de tests)
- Qué pasó (resultados de logs)

---

**Objetivo:** Que el código review sea ágil. El reviewer no busca nada, simplemente sigue la evidencia.

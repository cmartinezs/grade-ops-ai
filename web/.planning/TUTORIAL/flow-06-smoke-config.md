# Flujo G — Configurar smoke tests del proyecto

> [← Tutorial](README.md)

**Cuándo usar este flujo:** acabas de terminar una tarea o story y necesitas dejar documentados los smoke tests que el agente o el humano deben ejecutar antes del code review final. El objetivo es detectar problemas reales de compilación, conexión y migraciones antes de seguir.

---

## Escenario

Proyecto web con backend Java y base de datos. La story ya está encaminada, pero el equipo necesita un plan explícito para validar:

- arranque de la aplicación,
- conectividad contra la base de datos,
- migraciones o schema changes,
- y una verificación mínima del cambio antes de pedir review humano.

El archivo objetivo es `.planning/SMOKE-TESTS.md`.

---

## Paso 1 — Crear el scaffold

Si todavía no existe un plan de smoke tests, arráncalo con el modo guiado:

```bash
/plan-smoke-config --blank
```

Claude crea el archivo base con secciones mínimas:

- Stack Summary
- Execution Order
- Smoke Checks
- Human Review Gate

Usa este modo cuando quieres que el archivo exista primero y luego completarlo a mano o con el agente.

---

## Paso 2 — Completar el plan

Cuando ya hay contexto del repositorio, ejecuta el comando sin `--blank`:

```bash
/plan-smoke-config
```

Claude inspecciona el código fuente, infiere el stack y propone un plan concreto. En un proyecto Java/Maven con base de datos, normalmente deja algo como:

- levantar dependencias de soporte,
- ejecutar compilación o test de arranque,
- validar conectividad con la base de datos,
- validar consistencia estática entre migraciones/schema y ORM si existe,
- levantar el entorno local necesario para una prueba de persistencia,
- revisar logs de migración o inicialización,
- confirmar que el cambio responde correctamente.

Si el stack no es claro, el comando debe pedir más contexto en vez de inventar comandos destructivos o frágiles.

---

## Paso 3 — Revisar antes del review humano

El plan de smoke tests debe quedar escrito con suficiente precisión para que alguien pueda seguirlo sin adivinar:

| Sección | Qué debe quedar claro |
|---------|-----------------------|
| Stack Summary | Stack principal y servicios de soporte |
| Execution Order | Orden real de arranque y validación |
| Smoke Checks | Checks mínimos de compilación, conexión y migraciones |
| Database / ORM Consistency | Validación estática DB/ORM y smoke test local de persistencia |
| Human Review Gate | Cuándo detenerse y esperar revisión humana |

Si falta un comando, un servicio o un check, corrígelo antes de continuar.

---

## Regla de cierre

Cuando el smoke test plan está completo:

1. El agente puede usarlo para validar la tarea finalizada.
2. El humano revisa el archivo antes de aprobar el cierre.
3. Si hay correcciones, se vuelven a ejecutar los smoke checks afectados.

Esto mantiene la secuencia esperada del plugin: terminar trabajo, validar con smoke tests, esperar review humano y recién después avanzar con commit, push o PR.

---

## Relación con otros flujos

- Usa [Flujo B](flow-02-general.md) si todavía no existe el planning.
- Usa [Flujo D](flow-04-mid-execution.md) si el planning ya está activo y hay que ajustar una story.
- Usa [Flujo E](flow-05-autonomous.md) si quieres que agentes automaticen la ejecución completa después de dejar el smoke plan listo.

> [← Tutorial](README.md)

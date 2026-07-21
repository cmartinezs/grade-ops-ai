# Responsabilidades y límites arquitectónicos

## Principio rector

`api/` gobierna la intención y sus efectos; `agents/` gobierna la ejecución GenAI.

## Matriz de autoridad

| Responsabilidad | UI | API | Agents | Modelo |
|---|---:|---:|---:|---:|
| Autenticar usuario | Inicia | Sí | No | No |
| Autorizar y verificar ownership | No | Sí | No | No |
| Validar transición de workflow | No | Sí | No | No |
| Reservar créditos/presupuesto | No | Sí | Reporta consumo | No |
| Seleccionar agente por intención | No | Sí | Resuelve definición | No |
| Seleccionar provider/modelo | No | Define policy | Sí | No |
| Ejecutar prompt/tool loop | No | No | Sí | Propone acciones |
| Persistir entidad de dominio | No | Sí | No | No |
| Aprobar nota/feedback/rúbrica | Docente | Registra | No | No |
| Publicar al estudiante | Solicita | Sí | No | No |
| Auditoría canónica | No | Sí | Emite evidencia | No |

## Restricciones obligatorias

- La UI no conoce URLs, providers, prompts ni contratos internos de `agents/`.
- La UI no elige libremente `agentName` o herramientas.
- `agents/` no escribe directamente tablas de assessment, rubric, submissions, grading, feedback, billing o approvals.
- Un resultado técnico exitoso no es equivalente a aprobación académica.
- El modelo no recibe credenciales ni acceso irrestricto a repositorios o bases de datos.
- Las herramientas se registran y autorizan por `AgentDefinition`.
- No se persiste chain-of-thought; se persisten acciones, observaciones normalizadas, decisiones de policy, hashes, validaciones y evidencia.

## Persistencia operativa del runtime

Para el MVP se recomienda que `api/` mantenga el estado canónico de operación, run e intentos. `agents/` puede permanecer stateless por solicitud.

Si más adelante el tool loop necesita checkpoints internos para reanudar:

- `api/` conserva la proyección canónica visible y auditable;
- `agents/` puede tener almacenamiento operacional de checkpoints/steps;
- los checkpoints no se convierten en fuente de verdad del dominio;
- debe existir reconciliación mediante `operationId` y `agentRunId`.

No introducir esta dualidad antes de que tool loops o reanudación la necesiten realmente.

## API orientada a intención

La UI debe solicitar:

```text
generar borrador de assessment
generar rúbrica
calificar entregas
generar feedback
analizar brechas
generar recuperación
generar reporte
generar lote de preguntas
```

No debe solicitar:

```text
ejecutar el agente X con el prompt Y y las tools Z
```

Esto mantiene las reglas cerca del dominio y evita que el frontend se convierta en orquestador.


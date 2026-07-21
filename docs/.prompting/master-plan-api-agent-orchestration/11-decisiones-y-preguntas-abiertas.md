# Decisiones propuestas y preguntas abiertas

## Decisiones propuestas para formalizar mediante ADR

| ID | Decisión propuesta | Estado |
|---|---|---|
| D-API-01 | API es la única interfaz de UI y fuente de verdad del workflow | Recomendada |
| D-API-02 | Agents no persiste entidades de dominio | Ya alineada, formalizar |
| D-API-03 | Modelo `AiOperation` → `AgentRun` → `AgentAttempt` | Recomendada |
| D-API-04 | Arquitectura híbrida sync/async | Recomendada |
| D-API-05 | Outbox + Cloud Tasks para asincronía inicial | Recomendada |
| D-API-06 | Polling primero, SSE después | Recomendada |
| D-API-07 | OIDC en producción; secret solo local transitorio | Recomendada |
| D-API-08 | Contratos internos con OpenAPI/JSON Schema y contract tests | Recomendada |
| D-API-09 | Estado técnico separado de aprobación/publicación | Obligatoria |
| D-API-10 | Grading Closed permanece determinístico en API | Ya decidida |

## Preguntas que el plan debe resolver

### Tenancy

El código actual usa `teacherUid` como boundary; la documentación objetivo incorpora `Organization`. Definir cuándo migrar de teacher-scoped a organization-scoped sin bloquear el MVP.

### Nomenclatura

Definir si el recurso público se llamará:

- `ai-operations`;
- `agent-operations`;
- `agent-runs`.

Recomendación: `ai-operations` para intención visible y `agent-runs` como detalle técnico.

### Reutilización del log actual

Decidir entre:

- migrar `agent_execution_logs` a attempts;
- crear tablas nuevas y mantener la actual como compatibilidad/proyección;
- backfill de registros históricos.

Recomendación: tablas nuevas más migración explícita; evitar deformar una tabla terminal para múltiples responsabilidades.

### Transporte

Confirmar Cloud Tasks como primera opción. Pub/Sub puede ser preferible en fan-out/eventos, pero añade semántica y operación innecesarias para el primer executor dirigido.

### Provider policy

Definir:

- provider primario por ambiente;
- fallback permitido;
- qué errores permiten fallback;
- si fallback requiere aprobación por diferencia de costo/calidad;
- cómo se conserva evidencia de ambos attempts.

### Cancelación

Definir qué resultados tardíos se conservan y si pueden recuperarse manualmente después de una cancelación.

### Retención

Definir períodos distintos para:

- inputs sensibles;
- outputs;
- logs operativos;
- evidencia de costo;
- artifacts estudiantiles;
- hashes y metadata.

### Operator authentication

La documentación funcional identifica Operator, pero no existe mecanismo de autenticación claramente resuelto. Debe definirse antes de exponer dashboards internos, retries privilegiados o dead-letter operations.

## Supuestos utilizados

- Cloud Run continúa siendo el runtime de despliegue.
- PostgreSQL/Cloud SQL continúa como persistencia principal.
- Firebase continúa para autenticación de docentes.
- Gemini debe mantenerse como provider obligatorio de al menos parte del producto.
- Groq puede mantenerse como provider alternativo según ambiente/policy.
- La arquitectura continúa como modular monolith en `api/`.
- El Master Plan funcional R01–R06 continúa vigente.


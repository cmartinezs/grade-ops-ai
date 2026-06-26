# 11 — Seguridad, observabilidad y auditoría

## Seguridad base

GradeOps AI maneja datos educacionales, respuestas de estudiantes, feedback, métricas, pagos y uso de IA. La seguridad no es opcional.

## Identidad y autorización

Reglas:

- La identidad viene del token/sesión, no del request body.
- `tenantId`, `teacherId` y `userId` no deben confiarse desde el cliente cuando pueden derivarse del contexto autenticado.
- Toda operación multi-tenant debe validar pertenencia.
- La autorización fina debe vivir en aplicación o en policies dedicadas, no en controllers solamente.

## Datos sensibles

Considerar sensibles:

- Respuestas de estudiantes.
- Nombres, emails, identificadores de estudiantes.
- Feedback individual.
- Prompts con datos reales.
- Outputs de IA con evaluación académica.
- Tokens, API keys y credenciales.
- Información de pago.

## Logging

Usar logs estructurados y con contexto.

Recomendado:

```java
log.info(
    "Assessment generated tenantId={} teacherId={} assessmentId={} agentExecutionId={} model={} estimatedCredits={}",
    tenantId.value(),
    teacherId.value(),
    assessmentId.value(),
    agentExecutionId.value(),
    modelName.value(),
    estimatedCredits.value()
);
```

Prohibido:

```java
log.info("Student answer: {}", rawStudentAnswer);
log.info("Prompt: {}", rawPrompt);
log.info("Authorization header: {}", authorizationHeader);
```

## MDC

Incluir en MDC cuando esté disponible:

- `correlationId`.
- `requestId`.
- `tenantId`.
- `userId`.
- `agentExecutionId`.

Todo filtro que agregue MDC debe limpiarlo en `finally`.

```java
try {
  MDC.put("correlationId", correlationId);
  filterChain.doFilter(request, response);
} finally {
  MDC.clear();
}
```

## Auditoría de agentes AI

Cada ejecución de agente debe registrar, al menos:

- `agentExecutionId`.
- `agentName`.
- `tenantId`.
- `teacherId`.
- Recurso asociado: `assessmentId`, `submissionId`, etc.
- Modelo/proveedor.
- Versión de prompt.
- Input hash.
- Output hash.
- Estado: `STARTED`, `COMPLETED`, `FAILED`, `REJECTED`, `APPROVED`.
- Tokens estimados.
- Créditos consumidos.
- Duración.
- Error code si falla.

Evitar guardar prompt/output completo sin una decisión explícita de privacidad y retención.

## Tabla sugerida `agent_execution_logs`

```sql
create table agent_execution_logs (
  id uuid primary key,
  tenant_id uuid not null,
  teacher_id uuid null,
  resource_type varchar(80) not null,
  resource_id varchar(120) not null,
  agent_name varchar(120) not null,
  provider varchar(80) not null,
  model_name varchar(120) not null,
  prompt_version varchar(80) not null,
  input_hash varchar(128) not null,
  output_hash varchar(128) null,
  status varchar(40) not null,
  estimated_input_tokens integer null,
  estimated_output_tokens integer null,
  consumed_credits bigint null,
  started_at timestamp with time zone not null,
  completed_at timestamp with time zone null,
  error_code varchar(120) null,
  created_at timestamp with time zone not null
);
```

## Correlation ID

Todo request debe tener correlation ID.

- Si llega `X-Correlation-Id`, validarlo y reutilizarlo.
- Si no llega, generarlo.
- Devolverlo en response.
- Incluirlo en logs y errores.

## Manejo de errores

No exponer:

- Stack traces.
- SQL.
- Tokens.
- Prompts.
- Datos personales.
- Mensajes internos del proveedor AI si contienen payload sensible.

Sí exponer:

- Código de error estable.
- Mensaje útil.
- Correlation ID.
- Timestamp.

## Secrets

Reglas:

- Nunca commitear `.env` con secretos reales.
- Usar variables de entorno o secret manager.
- Rotar credenciales si se exponen.
- No loguear secrets.
- No incluir secrets en errores.

## Seguridad en AI

### Prompt injection

Toda entrada de usuario que llegue a un agente AI debe tratarse como no confiable.

Reglas:

- Separar instrucciones del sistema, contexto y contenido del usuario.
- No permitir que el contenido del estudiante modifique reglas del agente.
- Validar outputs con schemas.
- Rechazar outputs incompletos o malformados.
- Registrar versión de prompt.

### Structured outputs

Preferir JSON validado por schema sobre texto libre cuando el resultado alimenta lógica de negocio.

Ejemplos:

- Rúbrica.
- Score.
- Feedback.
- Learning gaps.
- Consumo de créditos.

## Métricas mínimas

Registrar métricas de:

- Requests por endpoint.
- Latencia por endpoint.
- Errores por endpoint.
- Ejecuciones AI por agente/modelo.
- Tokens estimados.
- Créditos consumidos.
- Tiempo ahorrado estimado.
- Aprobaciones/rechazos humanos.

## Health checks

Exponer checks para:

- Aplicación viva.
- Base de datos.
- Storage.
- Proveedor AI si aplica como readiness externo o diagnóstico separado.

No poner secretos ni detalles internos en endpoints públicos de health.

## Anti-patterns

- `System.out.println`.
- Logs con prompts crudos.
- Logs con respuestas completas de estudiantes.
- Logs con JWT/API keys.
- No limpiar MDC.
- Controller que confía en `tenantId` del body.
- Error handler que expone stack trace.
- Guardar AI payloads sin retención ni consentimiento.

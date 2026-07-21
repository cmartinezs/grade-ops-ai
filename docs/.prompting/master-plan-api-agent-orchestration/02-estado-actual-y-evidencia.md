# Estado actual y evidencia verificada

## Baseline

La revisión se realizó sobre `develop`, commit `aa2dc4e`. El monorepo declara:

- `web/`: workspace docente y acceso estudiantil;
- `api/`: estado de dominio, reglas, persistencia, autorización y billing;
- `agents/`: API interna de agentes, providers y resultados estructurados;
- `docs/`: fuente documental de producto y arquitectura.

## Vertical slice implementada

Solo existe una integración GenAI completa: generación y regeneración del borrador de assessment.

### Entrada pública

`AssessmentController` expone:

```text
POST /api/v1/assessments/{id}/draft
POST /api/v1/assessments/{id}/draft/regenerate
PATCH /api/v1/assessments/{id}/draft
GET /api/v1/assessments/{id}/draft
GET /api/v1/assessments/{id}/draft/versions
```

La generación devuelve directamente `GenerateAssessmentDraftResponse`; no devuelve `operationId` ni `agentRunId`.

### Coordinación en API

`DraftGenerationCoordinator`:

1. llama a `AssessmentAgentClient.generate(...)` sin transacción abierta;
2. al recibir éxito, abre una `TransactionTemplate`;
3. persiste `AgentExecutionLog`;
4. persiste `AssessmentDraft`;
5. actualiza el log para enlazar `draftId`;
6. ante `AgentClientException`, persiste un fallo genérico y relanza.

Esta separación es correcta: evita mantener una conexión/transacción de PostgreSQL durante una llamada lenta al LLM y evita el problema de self-invocation de `@Transactional`.

### Cliente interno

`AssessmentAgentClient` usa `RestClient` contra:

```text
POST /internal/agents/assessment
```

Configuración observada:

| Propiedad | Valor |
|---|---|
| Connect timeout | 5 segundos |
| Read timeout | 60 segundos |
| Auth header real | `X-Internal-Key` |
| Correlation | Nuevo `X-Correlation-Id` UUID por llamada |

Clasificación actual de errores:

| Caso | Reason API |
|---|---|
| Conexión, DNS o timeout | `UNREACHABLE` |
| Respuesta 4xx | `AGENT_REJECTED` |
| Respuesta 5xx | `AGENT_ERROR` |

### Runtime actual

`agents/` contiene un Assessment Agent real con:

- `AssessmentCommand`;
- `GenerateAssessmentDraftUseCase`;
- `AssessmentAgentOrchestrator`;
- selección de `AssessmentGenerationPort`;
- adapters Gemini y Groq;
- prompt `assessment-generation.st`;
- validación de entrada y salida;
- `AssessmentResult` estructurado;
- `AssessmentExecutionOutcome`;
- `AgentExecutionLogPayload`;
- `AssessmentAgentException` con log de fallo;
- filtro de autenticación interna y correlación.

El runtime es síncrono. No existe ejecución durable, loop genérico de herramientas, registry, steps persistidos, cancelación o reanudación.

## Persistencia actual

La migración `V12__add_agent_execution_logs.sql` crea:

```text
agent_execution_logs
  id UUID PK
  assessment_id UUID NOT NULL
  draft_id UUID NULL
  agent_execution_id UUID NULL
  agent_name VARCHAR NULL
  provider VARCHAR NULL
  model VARCHAR NULL
  prompt_version VARCHAR NULL
  input_hash VARCHAR NULL
  output_hash VARCHAR NULL
  estimated_input_tokens INTEGER NULL
  estimated_output_tokens INTEGER NULL
  cost_estimate DOUBLE PRECISION NULL
  status VARCHAR NOT NULL
  error_code VARCHAR NULL
  started_at TIMESTAMPTZ NOT NULL
  finished_at TIMESTAMPTZ NOT NULL
```

Consecuencias:

- modela una ejecución ya finalizada, no una ejecución viva;
- exige `finished_at` desde la creación;
- no soporta progreso, intento, lease o heartbeat;
- no tiene idempotency key;
- no tiene actor solicitante explícito;
- depende obligatoriamente de `assessment_id`, aunque futuros agentes pueden operar sobre question bank u otros recursos;
- usa strings libres para estado, agente y provider.

## Documentación ya existente

`docs/master-plan/analysis/agent-runtime-strategy.md` ya establece correctamente:

- runtime como capacidad transversal evolutiva;
- no crear una release técnica previa independiente;
- autoridad del dominio en `api/`;
- autoridad de ejecución en `agents/`;
- incorporación gradual de registry, tools, runs, steps y asincronía;
- no generalizar antes de un segundo consumidor real.

`docs/04-architecture/api-design.md` ya anticipa:

- `/agent-runs`;
- `AGENT_RUN_FAILED`;
- comandos de generación por capacidad;
- operación larga con run ID, polling y resultados parciales.

Estas capacidades aún no están implementadas en el código revisado.

## Limitación de verificación

No fue posible ejecutar la suite Maven porque el entorno de revisión no pudo resolver `repo.maven.apache.org`. El análisis se basó en el árbol completo, código, migraciones, configuración, tests presentes y documentación de `develop`. Esto debe registrarse como limitación de la revisión, no como fallo confirmado del proyecto.


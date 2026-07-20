# Arquitectura objetivo del runtime genérico

## Objetivo

Implementar un runtime headless que reciba una instrucción y contexto estructurado, ejecute un agente especializado con herramientas limitadas y entregue un resultado validado, trazable y controlado por presupuesto.

El runtime no presenta una interfaz conversacional. Es invocado por GradeOps API de manera síncrona o asíncrona.

## Componentes

```text
Agent Runtime
├── Agent Registry
├── Run Manager
├── Agent Loop
├── Model Gateway
├── Model Selection Policy
├── Context Manager
├── Tool Registry
├── Tool Executor
├── Policy Engine
├── Validation Engine
├── Budget Manager
├── Execution Repository
└── Observability
```

## Responsabilidades

### Agent Registry

- registrar definiciones de agentes;
- resolver una definición por nombre y versión;
- exponer contratos y capacidades;
- impedir nombres duplicados o versiones incompatibles.

### Run Manager

- iniciar ejecuciones;
- asignar `runId`;
- controlar estados;
- coordinar cancelación y reanudación;
- aplicar idempotencia;
- finalizar con resultado, error o bloqueo.

### Agent Loop

- construir cada turno del modelo;
- aceptar una acción tipada;
- ejecutar herramientas autorizadas;
- incorporar observaciones;
- solicitar corrección de salidas recuperables;
- detenerse por finalización o límites.

### Model Gateway

- abstraer Spring AI y los SDK de proveedores;
- normalizar mensajes, herramientas y respuestas;
- retornar uso, modelo, proveedor y respuesta identificable;
- evitar dependencias de proveedor en el runtime y los agentes.

### Model Selection Policy

- seleccionar proveedor y modelo;
- considerar capacidad, costo, contexto, latencia y disponibilidad;
- aplicar configuración por organización o plan;
- implementar fallback solo cuando la operación sea segura.

### Context Manager

- mantener instrucción, contexto, mensajes, herramientas y observaciones;
- resumir cuando se aproxime el límite de contexto;
- separar referencias de dominio de contenido sensible;
- no almacenar razonamiento privado del modelo.

### Tool Registry y Tool Executor

- registrar herramientas con contratos tipados;
- resolver herramientas por nombre;
- validar argumentos;
- ejecutar adapters de API, cálculo o sandbox;
- normalizar errores y resultados.

### Policy Engine

- comprobar que el agente puede usar la herramienta;
- aplicar identidad, tenant, curso y alcance;
- bloquear herramientas incompatibles con el nivel de autonomía;
- exigir aprobación cuando corresponda.

### Validation Engine

- validar acciones del modelo;
- validar resultados intermedios;
- validar el contrato final;
- ejecutar reglas determinísticas específicas del agente;
- decidir si el error puede repararse dentro del presupuesto.

### Budget Manager

- controlar pasos;
- llamadas al modelo;
- llamadas a herramientas;
- tokens;
- costo estimado;
- tiempo total;
- reintentos.

### Execution Repository y Observability

- persistir ejecuciones y pasos cuando la modalidad lo requiera;
- registrar métricas y errores;
- permitir correlación con solicitudes de GradeOps API;
- soportar auditoría operacional y consumo de créditos.

## Ciclo de ejecución

```text
1. Recibir AgentRequest.
2. Validar identidad, objetivo y contexto.
3. Resolver AgentDefinition.
4. Aplicar ExecutionPolicy efectiva.
5. Crear AgentRun.
6. Preparar instrucciones, mensajes y herramientas autorizadas.
7. Invocar ModelGateway.
8. Interpretar AgentAction:
   a. UseTool: autorizar, ejecutar, registrar y continuar.
   b. Finish: validar; finalizar o solicitar reparación.
   c. Block: registrar la causa y detenerse.
9. Verificar límites antes de cada iteración.
10. Retornar AgentExecutionResult.
```

## Acciones tipadas

```java
public sealed interface AgentAction {

    record UseTool(
            String toolName,
            Object arguments) implements AgentAction {}

    record Finish(
            Object result,
            String summary,
            List<AgentWarning> warnings) implements AgentAction {}

    record Block(
            String reason,
            List<String> missingInformation) implements AgentAction {}
}
```

El modelo propone una acción. El runtime conserva la autoridad para autorizarla y ejecutarla.

## Contrato genérico de solicitud

```json
{
  "agent": "assessment",
  "objective": "Crear una evaluación de Java sobre POO",
  "context": {
    "teacherId": "TEACHER-01",
    "courseId": "COURSE-10",
    "assessmentId": "ASSESSMENT-42"
  },
  "parameters": {
    "durationMinutes": 90,
    "difficulty": "introductory",
    "language": "Java"
  },
  "executionPolicy": {
    "maxSteps": 12,
    "timeoutSeconds": 120,
    "maxTokens": 30000,
    "maxEstimatedCost": 0.10
  },
  "idempotencyKey": "assessment-42-draft-v1"
}
```

La identidad efectiva y el tenant no deben confiarse exclusivamente a valores enviados en el body. Deben derivarse o comprobarse mediante la autenticación entre servicios y el contexto autorizado por la API.

## Contrato genérico de respuesta

```json
{
  "runId": "RUN-932",
  "agent": "assessment",
  "status": "COMPLETED",
  "result": {},
  "summary": "Se generó una evaluación de POO de 90 minutos.",
  "warnings": [],
  "requiresHumanApproval": true,
  "metrics": {
    "steps": 7,
    "modelCalls": 3,
    "toolCalls": 4,
    "inputTokens": 8200,
    "outputTokens": 2100,
    "estimatedCost": 0.018,
    "durationMs": 18400
  }
}
```

## Estados

```text
QUEUED
RUNNING
COMPLETED
FAILED
BLOCKED
NEEDS_INPUT
NEEDS_APPROVAL
BUDGET_EXCEEDED
TIMED_OUT
CANCELLED
```

`NEEDS_INPUT` y `BLOCKED` son necesarios incluso sin chat. El agente debe finalizar informando datos faltantes en lugar de inventarlos. Una ejecución posterior puede aportar la información y reanudar o crear una nueva versión correlacionada.

## API técnica sugerida

```http
POST /internal/agent-runs
GET  /internal/agent-runs/{runId}
POST /internal/agent-runs/{runId}/cancel
POST /internal/agent-runs/{runId}/resume
```

El endpoint específico existente de Assessment Agent debe mantenerse inicialmente. La introducción de un dispatcher genérico no obliga a exponer un endpoint público genérico ni a romper el contrato con `api/`.

## Restricción arquitectónica

El runtime no debe incluir ramas específicas del tipo:

```java
if (agentName.equals("assessment")) { ... }
```

La especialización debe resolverse mediante registros, estrategias y contratos implementados por cada feature.

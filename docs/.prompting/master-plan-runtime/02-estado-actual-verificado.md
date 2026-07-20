# Estado actual verificado del proyecto `agents`

## Alcance comprobado

El proyecto revisado es una aplicación Java 21 basada en Spring Boot 4.1.0 y Spring AI 2.0.0. Su propósito declarado en `pom.xml` es operar como runtime de agentes de GradeOps AI.

Los perfiles `demo` y `beta` incorporan:

- `spring-ai-starter-model-google-genai` para Gemini;
- `spring-ai-starter-model-openai` para consumir Groq mediante su endpoint compatible con OpenAI.

El proyecto usa además:

- Spring Web MVC;
- StringTemplate 4 para prompts;
- Logstash Logback Encoder;
- Lombok;
- pruebas de Spring Boot.

## Funcionalidad implementada

Existe una vertical slice del Assessment Agent bajo:

```text
agents/src/main/java/cl/gradeops/ai/agents/assessment/
```

Incluye:

- endpoint REST interno;
- comando de generación y regeneración;
- caso de uso;
- orquestador;
- puerto de generación;
- selector de proveedores;
- adapters Gemini y Groq;
- resultado estructurado;
- metadatos de ejecución;
- manejo de errores;
- configuración explícita de beans;
- pruebas unitarias y de adapters.

## Endpoint existente

`AssessmentController` expone:

```http
POST /internal/agents/assessment
```

El controlador depende del puerto de entrada `GenerateAssessmentDraftUseCase` y retorna `AssessmentExecutionResponse` a partir de `AssessmentExecutionOutcome`.

## Contrato de entrada actual

`AssessmentCommand` contiene:

- `learningGoal`;
- `topic`;
- `level`;
- `duration`;
- `language`;
- `adjustmentNotes`;
- `previousDraftId`;
- `previousDraft`;
- `provider`;
- `model`.

Para una regeneración, `adjustmentNotes`, `previousDraftId` y `previousDraft` deben estar todos presentes o todos ausentes.

El servicio `agents` no obtiene el borrador previo por ID. La API, propietaria de la persistencia, debe resolverlo y enviar el contenido mediante `previousDraft`.

## Pipeline del orquestador actual

`AssessmentAgentOrchestrator.generate` realiza:

1. Registra el instante inicial.
2. Valida campos obligatorios.
3. Valida consistencia de regeneración.
4. Valida que el proveedor sea reconocido.
5. Resuelve Gemini o Groq mediante `AssessmentGenerationPortSelector`.
6. Renderiza `prompts/assessment-generation.st`.
7. Calcula el hash SHA-256 del prompt.
8. Invoca una única vez `AssessmentGenerationPort.generate`.
9. Valida los campos obligatorios de `AssessmentResult`.
10. Calcula el hash de la respuesta cruda.
11. Estima el costo cuando existen tokens y tarifa configurada.
12. Construye `AgentExecutionLogPayload`.
13. Retorna `AssessmentExecutionOutcome`.

## Prompt actual

El archivo `assessment-generation.st`:

- declara versión en la primera línea mediante `// assessment-generation.v1`;
- solicita JSON estricto;
- entrega un ejemplo de forma;
- incorpora objetivo, tema, nivel, duración y lenguaje;
- incluye condicionalmente borrador anterior y notas de ajuste;
- no ofrece herramientas al modelo;
- no instruye un proceso iterativo.

## Abstracción de proveedores actual

`AssessmentGenerationPort` representa específicamente:

```text
generar una evaluación estructurada desde un prompt renderizado
```

Los adapters Gemini y Groq:

- construyen una solicitud mediante Spring AI `ChatClient`;
- permiten un modelo específico por solicitud;
- convierten directamente la respuesta a `AssessmentResult`;
- obtienen el texto crudo y metadatos del `ChatResponse`;
- distinguen metadatos de uso ausentes mediante `EmptyUsage`;
- retornan tokens de entrada y salida cuando están disponibles.

Esta abstracción es correcta para el caso actual, pero demasiado específica para un runtime común con llamadas a herramientas y múltiples tipos de agente.

## Selección de proveedor

`AssessmentGenerationPortSelector` recibe un mapa de adapters cuyo nombre de bean actúa como clave:

- `gemini`;
- `groq`.

El proveedor por defecto configurado en `application.yml` es `groq`. El comando puede sobrescribir proveedor y modelo por solicitud.

Las tarifas actuales son valores combinados de referencia para el hackathon:

- Gemini: `0.000075` por 1.000 tokens;
- Groq: `0.0`.

No existe todavía un catálogo de capacidades, modelos compatibles, precios por modelo ni política dinámica de selección.

## Trazabilidad actual

`AgentExecutionLogPayload` contempla datos como:

- ID de ejecución;
- nombre del agente;
- modelo;
- versión del prompt;
- hashes de entrada y salida;
- tokens estimados;
- costo estimado;
- estado;
- código de error;
- inicio y término.

En el flujo revisado el payload es construido y retornado junto al resultado. La persistencia durable y la consulta de ejecuciones deben planificarse explícitamente; no debe suponerse que ya existen solo porque el DTO está implementado.

## Qué no existe todavía

No se encontró implementación de:

- runtime común para múltiples agentes;
- `AgentDefinition` o registro de agentes;
- decisión tipada entre herramienta, finalización y bloqueo;
- registro o executor de herramientas;
- bucle modelo-herramienta-observación;
- políticas de herramientas por riesgo;
- persistencia de `AgentRun` y `AgentStep`;
- reanudación o cancelación;
- ejecución asíncrona de agentes;
- presupuesto global por ejecución;
- fallback dinámico entre proveedores;
- sandbox para código de estudiantes;
- handoffs entre agentes;
- evaluación automatizada de calidad de prompts y resultados.

## Inconsistencias o deuda detectada

### README incompleto

`agents/README.md` contiene solamente el título `grade-ops-ai-agents` y no documenta ejecución, arquitectura ni configuración.

### Guía desactualizada

`docs/09-developer-guide/06-agent-development.md` todavía declara que el repositorio está solo scaffolded y que no hay agentes implementados. Esto contradice la vertical slice existente del Assessment Agent.

### Propiedad de activación mal nombrada

`AssessmentConfig` y `AssessmentController` están condicionados por `app.agents.gemini.enabled`, aunque la configuración activa la feature completa y registra tanto Gemini como Groq. El nombre ya no expresa el comportamiento real.

### Documentación arquitectónica y repositorio

La ADR de separación del runtime describe `grade-ops-ai-agents` como repositorio/servicio separado. El código revisado está presente como `agents/` dentro del repositorio `grade-ops-ai`. El master plan debe verificar la topología de repositorios y despliegues deseada antes de asumir una migración física.

## Evaluación del estado

La base existente debe conservarse y evolucionar. No corresponde reescribirla desde cero. Las primeras tareas deberían extraer capacidades comunes manteniendo operativo el endpoint actual y sus pruebas.

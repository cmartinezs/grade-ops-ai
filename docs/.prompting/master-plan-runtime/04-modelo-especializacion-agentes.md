# Modelo de especialización de agentes

## Definición común

Cada agente debe registrarse mediante una definición tipada y versionada.

```java
public interface AgentDefinition<I, O> {

    String name();

    String version();

    String description();

    Class<I> inputType();

    Class<O> outputType();

    PromptReference systemInstructions();

    Set<String> allowedTools();

    ExecutionPolicy defaultPolicy();

    AutonomyLevel autonomyLevel();

    ValidationResult validateInput(I input);

    ValidationResult validateOutput(O output);
}
```

La forma exacta podrá adaptarse a las convenciones del proyecto, pero deben mantenerse esas responsabilidades.

## Qué aporta el runtime

- ciclo de ejecución;
- comunicación con modelos;
- tool calling;
- control de contexto;
- autorización;
- presupuesto;
- errores normalizados;
- persistencia de ejecución;
- métricas y trazabilidad.

## Qué aporta cada agente

- objetivo acotado;
- prompt/instrucciones especializadas;
- contrato de entrada;
- contrato de salida;
- herramientas permitidas;
- validadores educativos;
- reglas de evidencia;
- nivel de autonomía;
- condiciones de finalización;
- causas de bloqueo.

## Ejemplo: Assessment Agent

```java
public final class AssessmentAgentDefinition
        implements AgentDefinition<AssessmentInput, AssessmentProposal> {

    @Override
    public String name() {
        return "assessment";
    }

    @Override
    public Set<String> allowedTools() {
        return Set.of(
                "load_course_context",
                "load_learning_outcomes",
                "search_previous_assessments",
                "search_question_bank",
                "validate_learning_outcome_coverage",
                "estimate_student_workload");
    }

    @Override
    public AutonomyLevel autonomyLevel() {
        return AutonomyLevel.DRAFT_ONLY;
    }
}
```

## Abstracción de modelo

La interfaz objetivo no debe estar acoplada a `AssessmentResult`.

```java
public interface AgentModelGateway {

    ModelTurnResponse execute(ModelTurnRequest request);
}
```

```java
public record ModelTurnRequest(
        String systemInstructions,
        List<AgentMessage> messages,
        List<ToolDefinition> availableTools,
        OutputSchema outputSchema,
        ModelConfiguration model,
        ExecutionMetadata metadata) {}
```

```java
public record ModelTurnResponse(
        AgentAction action,
        TokenUsage usage,
        String provider,
        String model,
        String responseId) {}
```

Adapters previstos:

```text
AgentModelGateway
├── GeminiAgentModelAdapter
├── GroqAgentModelAdapter
├── OpenAiAgentModelAdapter   (futuro, si se requiere)
└── ClaudeAgentModelAdapter   (futuro, si se requiere)
```

No se deben agregar proveedores futuros hasta que exista una necesidad funcional, comercial o de resiliencia demostrable.

## Selección de modelo

```java
public interface ModelSelectionPolicy {

    SelectedModel select(
            AgentDefinition<?, ?> agent,
            AgentRequest request,
            ExecutionContext context);
}
```

Criterios potenciales:

- soporte de tool calling;
- soporte de salida estructurada;
- soporte multimodal;
- longitud de contexto;
- calidad histórica para la tarea;
- costo;
- latencia;
- disponibilidad;
- configuración del plan;
- política de datos;
- fallback compatible.

El caller puede solicitar un proveedor/modelo como override autorizado, pero la validación debe estar centralizada. El comportamiento actual de reenviar un nombre literal no validado deberá evolucionar hacia un catálogo de capacidades y modelos.

## Niveles de autonomía

```java
public enum AutonomyLevel {
    ADVISORY,
    DRAFT_ONLY,
    EXECUTE_READ_ONLY,
    EXECUTE_REVERSIBLE,
    HUMAN_APPROVAL_REQUIRED
}
```

Interpretación:

| Nivel | Alcance |
|---|---|
| `ADVISORY` | Analiza y entrega recomendaciones |
| `DRAFT_ONLY` | Genera propuestas sin afectar estados de dominio |
| `EXECUTE_READ_ONLY` | Consulta datos y ejecuta cálculos sin mutar dominio |
| `EXECUTE_REVERSIBLE` | Puede realizar acciones explícitas, acotadas e idempotentes |
| `HUMAN_APPROVAL_REQUIRED` | Produce una propuesta que exige confirmación antes del efecto académico |

## Versionado

Se deben versionar al menos:

- definición del agente;
- prompt;
- esquema de entrada;
- esquema de salida;
- herramientas disponibles;
- modelo y parámetros;
- validadores relevantes.

La versión debe permitir explicar con qué configuración se obtuvo un resultado y evaluar regresiones entre releases.

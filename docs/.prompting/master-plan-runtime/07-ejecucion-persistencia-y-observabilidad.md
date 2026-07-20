# Ejecución, persistencia y observabilidad

## Modalidades

### Síncrona

Adecuada para:

- generación breve;
- validación simple;
- una o pocas llamadas;
- tiempos compatibles con HTTP.

### Asíncrona

Necesaria para:

- corrección de código;
- ejecución de pruebas;
- análisis de múltiples entregas;
- reportes agregados;
- flujos con varias herramientas;
- tareas susceptibles de timeout;
- procesamiento masivo.

El master plan no debe forzar ejecución asíncrona antes de que una funcionalidad la necesite, pero los contratos iniciales deben evitar bloquear su incorporación posterior.

## Modelo de ejecución

### AgentRun

Campos mínimos recomendados:

- `runId`;
- `agentName`;
- `agentVersion`;
- `status`;
- `objective`;
- referencias de contexto;
- política efectiva;
- paso actual;
- `idempotencyKey`;
- caller/correlation ID;
- resultado final;
- causa de bloqueo o error;
- inicio y término.

### AgentStep

Campos mínimos:

- `runId`;
- número de paso;
- tipo de acción;
- proveedor y modelo;
- versión de prompt;
- herramienta;
- hashes de input/output;
- tokens;
- costo estimado;
- latencia;
- estado;
- error normalizado;
- timestamps.

No debe guardarse razonamiento privado del modelo. Deben guardarse decisiones operacionales, herramientas, observaciones normalizadas, validaciones y resultados.

## Presupuesto

```java
public record ExecutionPolicy(
        int maxSteps,
        int maxModelCalls,
        int maxToolCalls,
        int maxRetries,
        int maxTokens,
        BigDecimal maxEstimatedCost,
        Duration timeout) {}
```

Los límites deben comprobarse antes de cada turno y herramienta. Una ejecución que alcanza un límite debe finalizar con un estado explícito, no como error genérico.

## Consumo y créditos

La trazabilidad debe permitir calcular costo técnico por acción:

- tokens de entrada y salida;
- tarifa efectiva por modelo;
- herramientas de infraestructura consumidas;
- duración de sandbox;
- almacenamiento y transferencia cuando corresponda;
- reintentos y fallbacks;
- costo total estimado y posteriormente conciliado.

El runtime registra consumo técnico. GradeOps API aplica la política comercial de créditos, planes y cobros.

No debe asumirse que un token equivale directamente a un crédito comercial.

## Métricas mínimas

### Por ejecución

- duración;
- estado;
- pasos;
- llamadas al modelo;
- llamadas a herramientas;
- reintentos;
- tokens;
- costo;
- proveedor y modelo;
- versión de agente y prompt;
- validaciones fallidas;
- intervención humana requerida.

### Por agente

- tasa de éxito;
- tasa de salidas inválidas;
- latencia percentil;
- costo promedio;
- promedio de pasos;
- frecuencia de bloqueos;
- tasa de aprobación docente;
- tasa de edición posterior;
- calidad reportada.

### Por proveedor/modelo

- disponibilidad;
- latencia;
- errores;
- rate limits;
- cumplimiento de esquema;
- costo;
- calidad por tipo de agente.

## Evaluación de calidad

La observabilidad técnica no basta. Deben incorporarse casos de prueba representativos y criterios de calidad por agente:

- alineación curricular;
- evidencia suficiente;
- consistencia;
- ausencia de afirmaciones inventadas;
- cumplimiento del esquema;
- utilidad para el docente;
- estabilidad entre versiones;
- comparación con baseline.

Los cambios de prompt, herramienta o modelo deben poder evaluarse contra un dataset de casos controlados antes de producción.

## Idempotencia

- cada ejecución iniciada por una acción de usuario debe aceptar una clave de idempotencia;
- repetir la solicitud no debe descontar consumo ni duplicar resultados sin intención;
- tool calls con efectos requieren claves derivadas del `runId` y paso;
- el runtime debe distinguir llamada desconocida, en curso, completada y fallida.

## Reanudación

Una ejecución reanudable necesita:

- estado persistido;
- contexto reconstruible;
- herramientas idempotentes;
- versión fija de agente y prompt;
- política de expiración;
- decisión explícita sobre si conserva o recalcula el presupuesto restante.

No todas las ejecuciones iniciales requieren reanudación. Debe introducirse primero en tareas largas o costosas.

# Herramientas, políticas y seguridad

## Contrato común de herramienta

```java
public interface AgentTool<I, O> {

    ToolDefinition definition();

    O execute(I input, ToolExecutionContext context);
}
```

```java
public record ToolDefinition(
        String name,
        String description,
        Class<?> inputType,
        Class<?> outputType,
        ToolRisk risk,
        boolean idempotent) {}
```

## Categorías

### Herramientas de lectura

Obtienen contexto autorizado desde GradeOps API:

- curso;
- resultados de aprendizaje;
- evaluación;
- rúbrica aprobada;
- entrega;
- banco de preguntas;
- historial de uso;
- estadísticas precalculadas.

### Herramientas determinísticas

Ejecutan cálculos o validaciones:

- sumar ponderaciones;
- calcular cobertura;
- medir distribución;
- aplicar escalas;
- validar esquemas;
- estimar carga;
- calcular métricas de ítems.

### Herramientas de sandbox

Procesan contenido potencialmente no confiable:

- inspeccionar archivos;
- compilar código;
- ejecutar pruebas;
- análisis estático;
- extraer evidencia.

### Herramientas de propuesta

Preparan resultados, pero no cambian estados de dominio:

- proponer borrador;
- proponer rúbrica;
- proponer evaluación por criterio;
- proponer feedback.

## Riesgo de herramientas

```java
public enum ToolRisk {
    READ_ONLY,
    COMPUTE_ONLY,
    PROPOSE_CHANGE,
    WRITE_REVERSIBLE,
    WRITE_CRITICAL
}
```

Para el MVP, los agentes educativos deberían operar principalmente con `READ_ONLY`, `COMPUTE_ONLY` y `PROPOSE_CHANGE`.

## Autorización

Antes de ejecutar una herramienta, el Policy Engine debe verificar:

- agente y versión;
- herramienta incluida en `allowedTools`;
- riesgo compatible con la autonomía;
- identidad de servicio;
- tenant y organización;
- profesor, curso, evaluación o entrega dentro del alcance;
- presupuesto restante;
- idempotencia cuando exista escritura;
- aprobación previa cuando corresponda.

La autorización no debe quedar en manos del prompt ni de una decisión textual del modelo.

## Contenido no confiable

Entregas, documentos, código, comentarios y otros materiales de usuarios deben tratarse como datos no confiables. Pueden contener instrucciones dirigidas al modelo.

Controles requeridos:

- separar claramente instrucciones del sistema y contenido analizado;
- advertir al modelo que el contenido no redefine su rol;
- no exponer secretos ni herramientas administrativas;
- limitar los datos entregados a cada herramienta;
- evitar ejecución directa fuera de sandbox;
- registrar artefactos y evidencias sin ejecutar instrucciones embebidas;
- sanitizar nombres de archivo y rutas.

## Sandbox de código

Para Grading Agent debe planificarse un sandbox aislado con:

- imagen o entorno efímero;
- sin secretos;
- red deshabilitada por defecto;
- filesystem acotado;
- límites de CPU, memoria, procesos y disco;
- timeout;
- lista de lenguajes y versiones soportados;
- captura de stdout, stderr y exit code;
- destrucción del entorno al finalizar;
- límites de tamaño de entrada y salida.

La ejecución de código de estudiantes no debe ocurrir en el proceso principal del servicio `agents`.

## Protección de datos

- minimizar PII en prompts;
- usar identificadores internos cuando sea suficiente;
- no enviar datos de otro tenant;
- aplicar políticas de retención;
- registrar hashes o resúmenes cuando no sea necesario conservar contenido completo;
- definir qué proveedores pueden recibir qué categorías de datos;
- evitar incluir credenciales, tokens o configuraciones internas en el contexto.

## Aprobación humana

Requieren control docente antes de un efecto académico:

- publicación de evaluación;
- aprobación de rúbrica;
- nota definitiva;
- envío de feedback;
- modificación de contenido aprobado;
- aplicación de un plan de recuperación individual.

El runtime puede devolver `NEEDS_APPROVAL`, pero la aprobación y el cambio de estado pertenecen a GradeOps API.

## Reintentos y fallback

Solo deben reintentarse automáticamente:

- errores transitorios;
- timeouts recuperables;
- respuestas estructuradas inválidas reparables;
- rate limits respetando backoff;
- herramientas read-only o idempotentes.

No debe repetirse automáticamente una acción con efectos si no existe una clave de idempotencia y confirmación de resultado.

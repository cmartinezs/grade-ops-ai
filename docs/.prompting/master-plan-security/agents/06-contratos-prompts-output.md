# Contratos, prompts y output

## Input

- Bean Validation para null, blank, longitud, cardinalidad y formatos.
- Límite HTTP de payload y archivos.
- JSON Schema/OpenAPI versionado y contract tests API–Agents.
- Rechazar campos desconocidos en comandos internos sensibles o versionarlos explícitamente.
- Normalizar texto Unicode y controlar caracteres invisibles cuando corresponda.

## Prompt injection

El brief y `previousDraft` son datos no confiables. Delimitar instrucciones del sistema y contenido, indicar que texto embebido no modifica policy, minimizar contexto y etiquetar provenance. Esto reduce riesgo, no lo elimina.

Nunca interpolar secretos, credenciales, URLs internas o políticas completas. El modelo no puede decidir proveedor, presupuesto, permisos, aprobación o publicación.

## Output

La validación actual comprueba campos requeridos; debe ampliarse a schema cerrado, longitudes, enums, cardinalidad, semántica y sanitización downstream. Output LLM es datos no confiables: no ejecutar código, HTML, URLs o tool calls sin un mediador.

Registrar hashes y versiones, no contenido raw. Conservar contenido completo solo en almacenamiento autorizado por API y política de retención.

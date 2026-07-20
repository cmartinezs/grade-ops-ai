# Reglas para el agente encargado del master plan

## Instrucción principal

Utiliza estos documentos como restricciones arquitectónicas y técnicas para planificar las funcionalidades de GradeOps asistidas por IA. No conviertas esta documentación en una única mega-release técnica. Distribuye los incrementos del runtime entre releases funcionales, manteniendo trazabilidad explícita.

## Reglas obligatorias

1. Distingue siempre entre **estado existente**, **cambio requerido** y **capacidad futura**.
2. No declares como implementada una capacidad que en `02-estado-actual-verificado.md` aparece como inexistente.
3. Conserva el endpoint y el flujo actual del Assessment Agent mientras se realiza la migración incremental.
4. No propongas reescritura total si una extracción/refactor compatible permite avanzar.
5. Relaciona cada incremento técnico con una US funcional o una historia técnica habilitadora dentro de la misma release.
6. Una historia puramente técnica debe indicar qué funcionalidad presente o inmediatamente siguiente habilita.
7. No introduzcas multiagente, memoria vectorial, RAG, colas, sandbox o nuevos proveedores sin un caso funcional que los necesite.
8. La API conserva autoridad sobre dominio, persistencia, permisos, estados, notas, publicación y créditos.
9. El runtime puede proponer y ejecutar herramientas autorizadas, pero no aprobar decisiones académicas definitivas.
10. Modela herramientas y validadores determinísticos; no resuelvas todo mediante prompts.
11. Incluye seguridad contra contenido no confiable cuando se procesen entregas, documentos o código.
12. Incluye límites de pasos, tokens, costo, tiempo y reintentos desde la primera iteración del bucle agentic.
13. Registra versión de agente, prompt, modelo, proveedor, tokens, costo, latencia, estado y errores.
14. No almacenes cadena de pensamiento privada. Registra acciones, herramientas, observaciones normalizadas, evidencia y decisiones operacionales.
15. Toda acción con efectos debe ser autorizada, idempotente y, cuando corresponda, aprobada por una persona.
16. Incorpora pruebas de calidad, no solo pruebas técnicas de que el endpoint responde.
17. Identifica dependencias entre `web`, `api`, `agents` e `infra` para cada flujo completo.
18. Señala explícitamente residuals, deuda y trabajo diferido por release.

## Preguntas que debe responder cada release

- ¿Qué problema del docente o estudiante resuelve?
- ¿Qué agente se incorpora o amplía?
- ¿Qué puede hacer autónomamente?
- ¿Qué requiere aprobación?
- ¿Qué datos necesita y quién los provee?
- ¿Qué herramientas utiliza?
- ¿Qué reglas se ejecutan determinísticamente?
- ¿Qué parte requiere GenAI?
- ¿Cómo se valida el resultado?
- ¿Qué sucede si faltan datos?
- ¿Qué sucede si el modelo o una herramienta falla?
- ¿Cuánto puede gastar la ejecución?
- ¿Cómo se evita duplicar consumo o resultados?
- ¿Qué trazabilidad queda disponible?
- ¿Qué evidencia demuestra que la release aporta valor?

## Formato recomendado para historias técnicas

```markdown
### AI-PLATFORM-XXX — Nombre de la historia

**Capacidad funcional habilitada:**

**Agente consumidor:**

**Situación actual comprobada:**

**Cambio requerido:**

**Alcance:**

**Fuera de alcance:**

**Contratos afectados:**

**Herramientas/validadores:**

**Seguridad y autonomía:**

**Observabilidad y costo:**

**Compatibilidad/migración:**

**Criterios de aceptación:**

**Pruebas requeridas:**

**Residuals:**
```

## Prohibiciones de planificación

No generar historias vagas como:

- “Implementar IA”.
- “Crear agente inteligente”.
- “Agregar tool calling”.
- “Hacer runtime genérico”.
- “Integrar Gemini”.

Cada historia debe indicar contrato, consumidor, comportamiento observable, límites y criterios verificables.

## Criterio de priorización

Prioriza en este orden:

1. valor funcional demostrable;
2. seguridad y corrección académica;
3. reutilización inmediata;
4. trazabilidad y control de costos;
5. resiliencia;
6. optimización;
7. sofisticación agentic adicional.

## Texto normativo para incorporar al master plan

> La Plataforma de Agentes de GradeOps AI se gestionará como una capacidad arquitectónica transversal y evolutiva. No será implementada como una fase técnica aislada previa a las funcionalidades del producto. Cada release funcional que incorpore asistencia de IA deberá identificar el agente especializado involucrado, las capacidades incrementales requeridas del runtime genérico, las herramientas de dominio necesarias, los validadores determinísticos, los límites de autonomía y los puntos de aprobación humana.
>
> El runtime crecerá mediante vertical slices funcionales: cada incremento técnico deberá quedar integrado y validado por al menos un flujo completo que entregue valor al docente o al estudiante. Las capacidades comunes serán extraídas y generalizadas cuando exista evidencia concreta de reutilización, evitando tanto la duplicación entre agentes como la construcción anticipada de infraestructura no requerida.
>
> La arquitectura mantendrá una separación explícita entre el runtime genérico, encargado de la ejecución, los proveedores de modelos, las herramientas, las políticas y la observabilidad; y los agentes especializados, responsables de aportar instrucciones, contratos, herramientas permitidas, validaciones y reglas propias de cada tarea educativa. GradeOps API continuará siendo la autoridad sobre el dominio, la persistencia, los permisos, las transiciones de estado y las decisiones académicas definitivas.

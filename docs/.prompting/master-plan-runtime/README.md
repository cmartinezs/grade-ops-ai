# GradeOps AI — Base documental para planificar el Agent Runtime

## Propósito

Este paquete entrega al agente encargado de generar el master plan una fuente técnica estructurada sobre la evolución del proyecto `agents/` de GradeOps AI hacia un runtime agentic genérico y headless.

La documentación distingue explícitamente:

1. **Estado comprobado:** código y documentación existentes revisados en el repositorio.
2. **Arquitectura objetivo:** diseño recomendado para soportar agentes especializados.
3. **Estrategia incremental:** forma de incorporar la capacidad técnica dentro de releases funcionales.

El runtime no debe planificarse como un producto independiente ni como una gran fase técnica previa. Es una capacidad transversal que deberá crecer mediante vertical slices que habiliten valor verificable para docentes y estudiantes.

## Fuente y alcance de la revisión

- Repositorio: `cmartinezs/grade-ops-ai`
- Rama revisada: `develop`
- Commit de referencia del checkout: `0058fa048050ebdd86ab1c4be2d07d53bf7c4bfa`
- Proyecto principal revisado: `agents/`
- Documentación relacionada revisada: `docs/03-ai-agents/`, `docs/09-developer-guide/06-agent-development.md` y `docs/99-decisions/2026-06-10-agent-runtime-separation.md`
- Fecha de revisión: 2026-07-20

La revisión fue de diagnóstico y diseño. No se modificó el repositorio original.

## Orden de lectura

1. [`01-resumen-general.md`](01-resumen-general.md)
2. [`02-estado-actual-verificado.md`](02-estado-actual-verificado.md)
3. [`03-arquitectura-runtime-generico.md`](03-arquitectura-runtime-generico.md)
4. [`04-modelo-especializacion-agentes.md`](04-modelo-especializacion-agentes.md)
5. [`05-catalogo-funcional-agentes.md`](05-catalogo-funcional-agentes.md)
6. [`06-herramientas-politicas-y-seguridad.md`](06-herramientas-politicas-y-seguridad.md)
7. [`07-ejecucion-persistencia-y-observabilidad.md`](07-ejecucion-persistencia-y-observabilidad.md)
8. [`08-estrategia-incremental-master-plan.md`](08-estrategia-incremental-master-plan.md)
9. [`09-reglas-para-el-agente-planificador.md`](09-reglas-para-el-agente-planificador.md)

## Principio rector

> GenAI propone, interpreta, redacta y explica. El software determinístico obtiene hechos, valida, calcula, autoriza, persiste y ejecuta las decisiones de dominio.

## Resultado esperado del master plan

El plan generado a partir de estos archivos deberá:

- asociar cada incremento del runtime con una funcionalidad real;
- declarar los agentes involucrados en cada release;
- identificar herramientas, validadores y contratos necesarios;
- mantener la API como autoridad del dominio;
- incorporar límites de autonomía y aprobación humana;
- controlar tokens, costo, tiempo, reintentos y pasos;
- evitar infraestructura especulativa sin consumidor inmediato;
- conservar compatibilidad mientras se migra el flujo ya implementado de Assessment Agent.

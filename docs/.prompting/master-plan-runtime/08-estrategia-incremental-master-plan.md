# Estrategia incremental para el master plan

## Principio de planificación

El runtime se gestionará como una iniciativa técnica transversal, pero se implementará mediante vertical slices funcionales.

Cada release con IA debe declarar:

1. valor para docente o estudiante;
2. agente involucrado;
3. capacidades nuevas del runtime;
4. herramientas necesarias;
5. validadores determinísticos;
6. límites de autonomía;
7. aprobación humana;
8. métricas y evidencia de finalización;
9. consumo y costo observables.

## Épicas transversales sugeridas

### AI-PLATFORM-01 — Ejecución GenAI común

Abstracción de proveedores, prompts, respuestas estructuradas, métricas y errores.

### AI-PLATFORM-02 — Runtime genérico de agentes

Registro, definiciones, contexto, acciones tipadas y ciclo de ejecución.

### AI-PLATFORM-03 — Herramientas de dominio

Registro, autorización, ejecución y observación de herramientas.

### AI-PLATFORM-04 — Ejecución persistente

Runs, steps, estados, idempotencia, reanudación y cancelación.

### AI-PLATFORM-05 — Seguridad y gobernanza

Permisos, aislamiento, presupuestos, aprobación humana y auditoría.

### AI-PLATFORM-06 — Evaluación y observabilidad

Calidad, costos, latencia, regresiones, fallback y comparación de modelos.

Estas épicas no deben convertirse automáticamente en releases independientes.

## Etapas recomendadas

### Etapa 0 — Alinear documentación y baseline

**Objetivo funcional:** asegurar que el plan se base en el código real.

**Trabajo:**

- actualizar README de `agents`;
- corregir guía que indica que no existe implementación;
- documentar contrato actual;
- renombrar o reemplazar `app.agents.gemini.enabled` por una propiedad coherente;
- confirmar topología de repositorio y despliegue;
- establecer tests baseline del Assessment Agent.

### Etapa 1 — Consolidar ejecución GenAI existente

**Funcionalidad:** crear y regenerar borrador de evaluación.

**Capacidades:**

- mantener Gemini y Groq;
- catálogo validado de proveedor/modelo;
- errores normalizados;
- prompt y esquema versionados;
- métricas y costo por modelo;
- persistencia o entrega confiable del log;
- compatibilidad con endpoint actual.

No requiere todavía un bucle agentic completo.

### Etapa 2 — Registro genérico de agentes

**Funcionalidad habilitadora:** incorporar Rubric Agent sin duplicar infraestructura.

**Capacidades:**

- `AgentDefinition`;
- `AgentRegistry`;
- request/response común interno;
- selección de prompt y output schema;
- políticas y validadores por agente;
- `AgentModelGateway` común;
- adapters reutilizables de Gemini/Groq.

### Etapa 3 — Tool calling y ciclo agentic

**Funcionalidad:** Assessment Agent contextual y revisión de calidad.

**Capacidades:**

- `AgentAction`;
- `AgentLoop`;
- `ToolRegistry`;
- `ToolExecutor`;
- `PolicyEngine` básico;
- herramientas read-only;
- validadores determinísticos;
- límite de pasos, llamadas, costo y tiempo;
- `BLOCKED` y `NEEDS_INPUT`.

### Etapa 4 — Preguntas y evaluación cerrada

**Funcionalidades:** generación de preguntas, revisión de distractores, ambigüedad y composición.

**Capacidades:**

- agentes adicionales sobre el mismo runtime;
- herramientas de banco de preguntas;
- validaciones de respuesta, estado, cobertura y distribución;
- ejecución coordinada sin multiagente general prematuro;
- aprobación antes de snapshot/publicación.

### Etapa 5 — Ejecución asíncrona y persistente

**Funcionalidad:** análisis de entregas abiertas.

**Capacidades:**

- `AgentRun` y `AgentStep` persistidos;
- cola/job;
- consulta de estado;
- cancelación;
- idempotencia;
- timeout;
- reanudación donde se justifique.

### Etapa 6 — Sandbox y Grading Agent

**Funcionalidad:** propuesta de evaluación basada en evidencia de código.

**Capacidades:**

- sandbox aislado;
- compilación y pruebas;
- análisis estático;
- recolección de evidencia;
- evaluación por criterio;
- cálculo determinístico de puntaje;
- revisión docente obligatoria.

### Etapa 7 — Feedback y handoffs

**Funcionalidad:** feedback fundamentado a partir de evaluación revisable.

**Capacidades:**

- handoff tipado Grading → Feedback;
- referencias de evidencia;
- prevención de afirmaciones no respaldadas;
- aprobación y envío manejados por API;
- trazabilidad del origen de cada recomendación.

### Etapa 8 — Analítica y recuperación

**Funcionalidades:** reportes, brechas, análisis de ítems y recuperación.

**Capacidades:**

- ejecuciones agregadas;
- herramientas estadísticas;
- contexto longitudinal;
- separación entre hechos e hipótesis;
- optimización de costo y procesamiento masivo.

### Etapa 9 — Operación y optimización

**Funcionalidad:** operación sostenible de la plataforma IA.

**Capacidades:**

- Ops Agent;
- evaluación continua;
- comparación de modelos;
- fallback controlado;
- presupuestos por plan;
- métricas de calidad;
- detección de regresiones;
- conciliación de costo técnico y créditos comerciales.

## Regla de extracción

Una capacidad debe generalizarse cuando exista evidencia concreta de reutilización, idealmente un segundo consumidor real o una necesidad inmediata de la siguiente release.

Ejemplos:

- la abstracción de proveedores ya está justificada por Gemini y Groq;
- el registro genérico se justifica al incorporar un segundo agente;
- la ejecución asíncrona se justifica al procesar código o lotes;
- la memoria semántica de largo plazo no debe anticiparse sin un caso funcional concreto;
- la delegación multiagente no debe anticiparse antes de estabilizar agentes individuales y handoffs tipados.

## Sección obligatoria por release

```markdown
## Capacidades de IA y Agent Runtime

### Agentes involucrados

### Capacidades funcionales habilitadas

### Incrementos del runtime requeridos

### Herramientas requeridas

### Validadores determinísticos

### Contratos de entrada y salida

### Autonomía y controles humanos

### Límites operacionales

### Métricas y consumo

### Evidencia de finalización

### Deuda o capacidades diferidas
```

## Definition of Done mínima para funcionalidad IA

- resultado funcional visible y revisable;
- contrato estructurado validado;
- agente y prompt versionados;
- herramientas restringidas y probadas;
- límites efectivos;
- tokens, costo y latencia registrados;
- errores y bloqueos explícitos;
- aprobación humana aplicada donde corresponde;
- pruebas unitarias y de integración;
- casos de evaluación de calidad;
- documentación sincronizada con código.

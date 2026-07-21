# Resumen general

## Conclusión del análisis

El proyecto `agents/` ya posee una base técnica válida para integrar modelos GenAI, pero su implementación actual no constituye todavía un runtime agentic comparable operacionalmente con Codex o Claude Code.

El flujo existente del Assessment Agent es esencialmente:

```text
validar comando
→ seleccionar proveedor
→ renderizar prompt
→ realizar una llamada al modelo
→ convertir respuesta estructurada
→ validar resultado
→ calcular métricas estimadas
→ retornar resultado y log
```

Es una ejecución LLM estructurada y específica. No existe todavía un bucle genérico en el cual el modelo pueda decidir usar una herramienta, observar su resultado, reevaluar y continuar hasta producir una salida validada o declarar un bloqueo.

## Objetivo arquitectónico

La evolución propuesta consiste en construir un runtime headless común capaz de ejecutar distintas tareas de GradeOps a partir de una instrucción y un contexto estructurado.

```text
instrucción
→ resolver agente especializado
→ preparar contexto
→ consultar modelo
→ decidir acción
→ autorizar y ejecutar herramienta
→ observar resultado
→ continuar o finalizar
→ validar salida
→ registrar ejecución
```

El runtime debe ser genérico y no contener lógica educativa específica. Cada agente aporta la especialización mediante:

- instrucciones de sistema;
- contrato de entrada;
- contrato de salida;
- herramientas autorizadas;
- validadores;
- políticas de autonomía;
- límites de ejecución;
- criterios de finalización y bloqueo.

## Papel dentro del producto

El runtime es una capacidad técnica transversal que habilita funcionalidades como:

- creación y ajuste de evaluaciones;
- generación y revisión de rúbricas;
- generación y control de calidad de preguntas;
- composición de evaluaciones cerradas;
- evaluación de entregas abiertas basada en evidencia;
- redacción de feedback;
- reportes docentes;
- detección de brechas de aprendizaje;
- propuestas de recuperación;
- análisis de preguntas;
- observabilidad operacional y de costos de IA.

No todas estas funciones deben delegarse íntegramente a GenAI. GradeOps debe combinar decisiones del modelo con herramientas determinísticas.

## Separación de responsabilidades

### GradeOps API

Continúa siendo responsable de:

- autenticación y autorización;
- usuarios, organizaciones, cursos y matrículas;
- ciclo de vida de evaluaciones, rúbricas, entregas y feedback;
- persistencia del dominio;
- transiciones de estado;
- cálculo final de notas;
- publicación;
- aprobación docente;
- consumo de créditos y facturación;
- auditoría de negocio.

### Agent Runtime

Es responsable de:

- ejecutar agentes;
- gestionar el ciclo modelo-herramienta-observación;
- abstraer proveedores y modelos;
- controlar contexto y presupuesto;
- autorizar herramientas;
- validar respuestas técnicas;
- persistir y reanudar ejecuciones cuando corresponda;
- registrar pasos, métricas, tokens, costos y errores.

### Agentes especializados

Son responsables de definir cómo se resuelve una clase acotada de tareas, sin asumir autoridad sobre el dominio.

## Estrategia recomendada

El runtime debe crecer de manera incremental junto con releases funcionales. No se recomienda una única release extensa de infraestructura antes de entregar valor.

Ejemplos:

| Funcionalidad | Incremento reutilizable del runtime |
|---|---|
| Crear evaluación | Proveedor, prompt, salida estructurada, métricas |
| Ajustar evaluación | Contexto, regeneración, versionado y reintento |
| Generar rúbrica | Registro genérico de agentes y contratos especializados |
| Revisar calidad | Herramientas, validadores y bucle agentic |
| Corregir entregas | Ejecución asíncrona, sandbox y evidencia |
| Generar feedback | Handoff tipado y trazabilidad entre resultados |
| Analizar curso | Ejecución masiva, agregación y optimización de costos |

## Fórmula de especialización

```text
Agente especializado
= Runtime genérico
+ instrucciones
+ contrato de entrada
+ contrato de salida
+ herramientas autorizadas
+ validadores
+ políticas de autonomía
```

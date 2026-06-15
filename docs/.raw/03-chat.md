# Lectura Técnica Del Artículo TodoDeIA

> Nota raw formateada desde la conversación técnica sobre el artículo: <https://www.tododeia.com/community/gemini-xprize>

> Decisión posterior: este archivo conserva una exploración técnica. La política canónica actual está en `../solution.md` y `../cost-model.md`: usar model routing, no usar suscripciones personales como runtime productivo y verificar precios/modelos oficiales antes de desplegar o postular.

## Prompt Original

> Ok, genial eso. ¿Y la parte técnica de la que habla? Te dejo el enlace nuevamente: <https://www.tododeia.com/community/gemini-xprize>

## Tesis Técnica

La parte técnica del artículo básicamente dice:

> No basta con usar Gemini como "chatbot" dentro de la app; hay que montar una arquitectura donde Gemini + agentes + herramientas + logs + cloud formen el núcleo operativo del negocio.

Para **GradeOps AI**, esto significa que el producto debe demostrar operación, no solo generación de texto.

## 1. Gemini API: Requisito Mínimo, No Diferenciador

El artículo dice que la app desplegada debe:

- hacer al menos una llamada a Gemini API;
- usar al menos un producto de Google Cloud.

Eso es requisito de entrada, no ventaja competitiva.

Para nosotros, Gemini no debería usarse solo para:

> Genera una evaluación.

Debe usarse para varias operaciones reales:

| Operación | Uso De Gemini |
| --- | --- |
| Crear evaluación | Generar enunciado, objetivos e instrucciones. |
| Crear rúbrica | Devolver criterios estructurados. |
| Validar rúbrica | Detectar ambigüedad o dificultad mal calibrada. |
| Analizar entrega | Revisar código/respuesta del estudiante. |
| Corregir | Aplicar rúbrica y sugerir puntaje. |
| Generar feedback | Crear feedback individual accionable. |
| Detectar brechas | Agrupar errores frecuentes. |
| Recomendar recuperación | Sugerir actividad remedial. |
| Generar reporte | Preparar resumen ejecutivo para el docente. |

La diferencia clave:

> Cada operación debe quedar registrada como evento de agente.

## 2. Function Calling: Base Real De Los Agentes

El artículo menciona function calling como "la base de un agente".

Técnicamente, esto significa que Gemini no solo responde texto: puede decidir llamar funciones de negocio con parámetros estructurados. La documentación oficial de Gemini lo define como una forma de conectar el modelo con herramientas y APIs externas para ejecutar acciones reales.

Para **GradeOps AI**, las tools no deberían ser genéricas como `buscar()` o `guardar()`. Deberían representar acciones reales del dominio:

- `create_assessment()`;
- `create_rubric()`;
- `validate_rubric()`;
- `grade_submission()`;
- `generate_feedback()`;
- `detect_learning_gaps()`;
- `create_recovery_activity()`;
- `generate_teacher_report()`;
- `record_agent_execution()`.

Eso permite decir ante los jueces:

> Nuestros agentes no solo redactan contenido; ejecutan operaciones académicas completas.

## 3. Structured Output: Obligatorio Para Que Esto Sea Sistema

El artículo insiste en salida estructurada con JSON. Esto es crítico para nosotros.

### Mala Implementación

> Gemini responde un párrafo con la rúbrica.

### Buena Implementación

```json
{
  "rubric": [
    {
      "criterion": "Uso correcto de condicionales",
      "maxScore": 25,
      "levels": [
        {
          "label": "Logrado",
          "score": 25,
          "description": "Implementa condicionales correctamente..."
        }
      ]
    }
  ]
}
```

La documentación oficial de Gemini confirma soporte de structured outputs, incluyendo combinación con herramientas en modelos Gemini 3.

Para **GradeOps AI**, esto es fundamental porque necesitamos guardar:

- rúbricas;
- puntajes;
- feedback;
- brechas;
- riesgos;
- decisiones;
- costos;
- logs;
- reportes.

Sin JSON estructurado, después todo se vuelve frágil.

## 4. ADK: Útil, Pero No Obligatorio Para Todo

El artículo habla de Google ADK, el Agent Development Kit. ADK es el framework open-source de Google para construir, depurar y desplegar agentes IA; hoy soporta Python, TypeScript, Go, Java y Kotlin.

Mi lectura para **GradeOps AI**:

> Sí conviene usar ADK, pero con foco. No intentaría meter toda la plataforma dentro de ADK.

Separación recomendada:

| Pieza | Tecnología recomendada |
| --- | --- |
| Web app | Next.js |
| API principal | Spring Boot o Next.js API |
| Agentes IA | ADK, idealmente Python o TypeScript |
| Modelo | Gemini 3.5 Flash como base |
| Deploy | Cloud Run |
| Datos | Firestore o PostgreSQL |
| Archivos | Cloud Storage |
| Auth | Firebase Auth |
| Pagos | Stripe |
| Evidencia | Agent logs + dashboard interno |

ADK debería operar el flujo de agentes, no reemplazar toda la arquitectura.

## 5. Cloud Run: Opción Más Limpia Para Cumplir Google Cloud

El artículo propone Cloud Run o Firebase. Para nuestro caso, Cloud Run es la opción más defendible porque permite desplegar contenedores backend/agent sin administrar infraestructura.

La documentación oficial de ADK indica que los agentes pueden desplegarse en Cloud Run con `adk deploy cloud_run` o con `gcloud run deploy`.

### Arquitectura Mínima Defendible

```text
Usuario docente
   ↓
Next.js Web App
   ↓
GradeOps API
   ↓
Agent Orchestrator
   ↓
Gemini API + Tools
   ↓
Firestore / PostgreSQL / Cloud Storage
   ↓
Agent Execution Logs + Dashboard
```

Para la hackathon, esto cumple:

- llamada real a Gemini;
- uso real de Google Cloud;
- agentes operando flujo principal;
- datos persistidos;
- evidencia verificable.

## 6. Grounding Con Google Search: Usarlo Con Cuidado

El artículo menciona grounding con Google Search para datos frescos. La documentación oficial indica que conecta Gemini con contenido web en tiempo real y permite respuestas con fuentes verificables.

Para **GradeOps AI**, no lo usaría en la corrección de estudiantes. Ahí necesitamos consistencia, no búsqueda web.

Sí lo usaría para:

- sugerir ejemplos actualizados;
- generar contexto de una actividad;
- buscar referencias públicas;
- construir material complementario;
- enriquecer actividades de recuperación.

No debería usarse para decidir una nota.

## 7. Multimodal: Potente, Pero No Para El MVP Inicial

El artículo destaca que Gemini entiende texto, imagen, audio y video. Técnicamente es relevante, pero para **GradeOps AI** no metería OCR ni corrección desde foto en el MVP.

Por ahora:

> MVP = texto + código pegado + archivo subido.

Roadmap:

> V2 = imagen/foto/OCR de respuestas manuscritas.

Motivo: el MVP debe vender rápido y ser confiable. Meter OCR ahora aumenta riesgo técnico, validación y tiempo de QA.

## 8. Gemini CLI / Claude Code: Herramienta De Construcción, No Parte Del Producto

El artículo dice algo importante:

> El concurso no obliga a programar con Gemini CLI; obliga a que el producto desplegado llame a Gemini y use Google Cloud.

Eso para nosotros significa:

- podemos construir con Claude Code, Codex, IntelliJ, VSCode o lo que convenga;
- lo importante es que GradeOps AI en producción use Gemini;
- el repo debe dejar claro qué fue generado con IA y qué fue diseñado por el equipo.

Yo usaría agentes de código para acelerar, pero dejaría decisiones técnicas documentadas en `grade-ops-ai-docs`.

## 9. La Parte Técnica Que Más Conviene Copiar

La frase técnica más valiosa:

> La IA debe operar el negocio, no asistirlo.

Bajado a **GradeOps AI**:

### No

> El docente pide ayuda a una IA para crear una prueba.

### Sí

> El docente inicia una operación de evaluación. Los agentes diseñan, validan, corrigen, generan feedback, detectan brechas, preparan recuperación, registran evidencia y dejan todo listo para aprobación humana.

Ese enfoque debe reflejarse en la arquitectura.

## 10. Diseño Técnico Recomendado Para GradeOps AI

Mi recomendación concreta:

```text
grade-ops-ai-docs
grade-ops-ai-web
grade-ops-ai-api
grade-ops-ai-agents
grade-ops-ai-infra
```

### `grade-ops-ai-web`

**Tecnología:** Next.js.

**Responsabilidades:**

- landing;
- login docente;
- dashboard;
- crear evaluación;
- ver entregas;
- aprobar correcciones;
- ver reportes;
- ver logs de agentes en modo demo.

### `grade-ops-ai-api`

**Tecnología:** Spring Boot si queremos aprovechar ventaja técnica.

**Responsabilidades:**

- usuarios;
- cursos;
- evaluaciones;
- entregas;
- rúbricas;
- reportes;
- seguridad;
- persistencia;
- endpoints para la web;
- integración con agentes.

### `grade-ops-ai-agents`

**Tecnología:** ADK o implementación agentic propia.

**Responsabilidades:**

- Assessment Agent;
- Rubric Agent;
- Grading Agent;
- Feedback Agent;
- Learning Gap Agent;
- Recovery Agent;
- Teacher Report Agent;
- Ops Agent.

### `grade-ops-ai-infra`

**Tecnología:** Terraform o scripts simples.

**Responsabilidades:**

- Cloud Run;
- Cloud Storage;
- variables de entorno;
- deploy;
- secrets;
- observabilidad.

## Veredicto Técnico

La parte técnica del artículo no debe interpretarse como:

> Usa todas las features de Gemini.

Debe interpretarse como:

> Construye un flujo donde Gemini + agentes + tools + JSON + logs + Cloud Run ejecuten una operación de negocio medible.

Para **GradeOps AI**, eso significa que el MVP técnico no es "generador de pruebas".

El MVP técnico es:

> Un motor agentic de operación evaluativa: recibe objetivo, genera evaluación, genera rúbrica, recibe respuestas, corrige, entrega feedback, detecta brechas, propone recuperación, registra trazabilidad y muestra evidencia de uso real.

Esa es la arquitectura que sí calza con la hackathon.

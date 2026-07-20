# Catálogo funcional de agentes y tareas GenAI

## Clasificación general

### Tareas apropiadas para GenAI

Requieren generación, interpretación contextual, explicación o tratamiento de contenido no estructurado.

### Tareas híbridas

Combinan algoritmos determinísticos con interpretación o redacción generativa.

### Tareas no delegables a GenAI

Requieren exactitud, autorización, reglas de dominio o efectos persistentes. Deben permanecer en software determinístico.

## Catálogo de agentes

### Assessment Agent

**Objetivo:** transformar la intención del docente en un borrador coherente de evaluación abierta.

**Entradas principales:** objetivo de aprendizaje, tema, nivel, lenguaje, duración, restricciones, tipo de evaluación y contexto del curso.

**Herramientas potenciales:**

- `load_course_context`;
- `load_learning_outcomes`;
- `search_previous_assessments`;
- `search_question_bank`;
- `validate_learning_outcome_coverage`;
- `estimate_student_workload`.

**Salida:** borrador estructurado con objetivos, instrucciones, entregables, restricciones, evidencia esperada, duración estimada y advertencias.

**Autonomía:** `DRAFT_ONLY`.

### Rubric Agent

**Objetivo:** proponer una rúbrica alineada con una evaluación.

**Herramientas:**

- `load_assessment_draft`;
- `load_learning_outcomes`;
- `load_rubric_templates`;
- `validate_rubric_weights`;
- `validate_criterion_observability`;
- `validate_rubric_coverage`.

**Reglas:** cada criterio debe ser observable; los niveles deben diferenciarse; las ponderaciones deben ser válidas; la rúbrica no se aprueba automáticamente.

**Autonomía:** `DRAFT_ONLY`.

### Question Generation Agent

**Objetivo:** generar preguntas candidatas para el banco.

**Herramientas:**

- `load_learning_outcomes`;
- `search_similar_questions`;
- `validate_answer_consistency`;
- `validate_question_schema`;
- `estimate_question_difficulty`.

**Salida:** enunciado, tipo, alternativas cuando corresponda, respuesta correcta, explicación, dificultad, alineación curricular y advertencias.

**Autonomía:** `DRAFT_ONLY`.

### Distractor Quality Agent

**Objetivo:** analizar la calidad de alternativas incorrectas.

**Detecta:** distractores absurdos, pistas gramaticales, redundancia, diferencias reveladoras de longitud, más de una respuesta defendible y falta de plausibilidad.

**Autonomía:** `ADVISORY`.

### Ambiguity Review Agent

**Objetivo:** detectar ambigüedad, contradicciones, falta de información y sesgos.

**Salida:** hallazgos con severidad, evidencia textual y propuesta de corrección. No modifica directamente una pregunta aprobada.

**Autonomía:** `ADVISORY`.

### Assessment Assembly Agent

**Objetivo:** proponer una composición de evaluación cerrada usando preguntas aprobadas.

**Herramientas:**

- `search_approved_questions`;
- `load_question_usage_history`;
- `calculate_difficulty_distribution`;
- `calculate_outcome_coverage`;
- `validate_assessment_composition`.

Los filtros de estado, duplicidad, cobertura y puntaje deben ser determinísticos. GenAI puede explicar y resolver compromisos entre objetivos.

**Autonomía:** `DRAFT_ONLY`.

### Grading Agent

**Objetivo:** proponer evaluación por criterio para una entrega abierta, basada en evidencia.

**Herramientas:**

- `load_submission`;
- `load_approved_rubric`;
- `inspect_submission_files`;
- `compile_code`;
- `run_tests`;
- `collect_static_analysis`;
- `match_evidence_to_criterion`;
- `calculate_proposed_score`.

**Salida mínima por criterio:** nivel propuesto, evidencias referenciables, explicación breve, confianza y necesidad de revisión.

El cálculo numérico final y la confirmación de nota no pertenecen al modelo.

**Autonomía:** `HUMAN_APPROVAL_REQUIRED`.

### Feedback Agent

**Objetivo:** transformar resultados y evidencias aprobadas en feedback pedagógico.

**Herramientas:**

- `load_grading_evidence`;
- `load_feedback_preferences`;
- `load_student_progress_summary`;
- `validate_feedback_grounding`.

No puede formular afirmaciones sin respaldo en evidencia ni enviar feedback directamente al estudiante.

**Autonomía:** `HUMAN_APPROVAL_REQUIRED`.

### Teacher Report Agent

**Objetivo:** resumir resultados para apoyar decisiones docentes.

**Herramientas:**

- `calculate_assessment_statistics`;
- `load_criterion_performance`;
- `load_common_errors`;
- `compare_previous_assessments`.

**Autonomía:** `EXECUTE_READ_ONLY`.

### Learning Gap Agent

**Objetivo:** detectar brechas de aprendizaje y patrones conceptuales.

**Herramientas:**

- `load_performance_history`;
- `load_learning_outcome_map`;
- `calculate_error_clusters`;
- `compare_cohorts`.

Debe diferenciar hechos estadísticos de hipótesis interpretativas.

**Autonomía:** `EXECUTE_READ_ONLY`.

### Recovery Agent

**Objetivo:** proponer actividades de refuerzo alineadas con brechas verificadas.

**Herramientas:**

- `load_learning_gaps`;
- `search_learning_resources`;
- `load_course_constraints`;
- `validate_recovery_alignment`.

**Autonomía:** `DRAFT_ONLY`.

### Item Analytics Agent

**Objetivo:** interpretar el rendimiento de preguntas cerradas.

**Herramientas:**

- `calculate_item_difficulty`;
- `calculate_discrimination_index`;
- `calculate_distractor_distribution`;
- `load_item_history`.

Las métricas son determinísticas; GenAI aporta interpretación y recomendaciones.

**Autonomía:** `EXECUTE_READ_ONLY`.

### Ops Agent

**Objetivo:** explicar anomalías de calidad, disponibilidad, uso y costo del runtime.

**Herramientas:**

- `load_agent_metrics`;
- `load_provider_errors`;
- `compare_model_costs`;
- `detect_usage_anomalies`;
- `load_prompt_versions`.

**Autonomía:** `EXECUTE_READ_ONLY`.

## Tareas híbridas prioritarias

| Tarea | Parte determinística | Parte GenAI |
|---|---|---|
| Corregir código | Compilar, probar y analizar | Interpretar evidencia según rúbrica |
| Calcular nota | Ponderar criterios y aplicar escala | Proponer nivel por criterio |
| Componer evaluación | Filtrar y medir distribución | Explicar o resolver compromisos |
| Analizar resultados | Calcular métricas | Interpretar patrones |
| Evaluar dificultad | Datos históricos y reglas | Analizar complejidad cognitiva |
| Estimar duración | Heurísticas e historial | Analizar carga del enunciado |

## Tareas que deben permanecer fuera de GenAI

- autenticación;
- autorización;
- pertenencia a tenant, curso o evaluación;
- cálculo matemático definitivo de notas;
- aplicación de escalas;
- descuento de créditos;
- facturación;
- persistencia de entidades;
- transición de estados;
- publicación;
- creación de snapshots;
- corrección cerrada con respuesta conocida;
- contabilización de respuestas;
- control de fechas;
- idempotencia;
- aprobación académica definitiva.

## Sobre el enrutamiento

Puede existir un Router Agent para instrucciones abiertas, pero no debe utilizarse cuando el flujo de negocio ya conoce el agente requerido. En esos casos, GradeOps API debe enviar el agente explícitamente para reducir costo, ambigüedad y riesgo.

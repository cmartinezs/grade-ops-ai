# GradeOps AI — Modelo unificado de evaluaciones abiertas, cerradas y acceso de estudiantes sin cuenta

## 1. Decisión de producto

GradeOps AI debe soportar dos tipos principales de evaluación:

1. **Evaluaciones abiertas**
   - El estudiante entrega desarrollo, código, archivo, texto libre o respuesta extensa.
   - La creación de la evaluación es asistida por AI.
   - La pauta/rúbrica de corrección es asistida por AI.
   - La calificación es asistida por AI, pero requiere revisión/aprobación docente.
   - El resultado final es una nota ponderada asociada a un alumno y una asignatura.

2. **Evaluaciones cerradas**
   - El estudiante responde seleccionando alternativas dentro de un universo definido por pregunta.
   - La AI puede generar preguntas, alternativas, metadatos, dificultad, resultados de aprendizaje y pauta.
   - La evaluación se vuelve determinística una vez aprobada y publicada.
   - La calificación se calcula por clave, puntaje y escala.
   - El resultado final es una nota ponderada asociada a un alumno y una asignatura.

Además, el sistema debe permitir que el alumno:

- realice una entrega abierta mediante un link único y seguro;
- responda una evaluación cerrada mediante un link único y seguro;
- vea sus resultados mediante un portal sin cuenta;
- valide acceso mediante link enviado a su email;
- no necesite registro ni contraseña dentro de la solución en la primera versión.

---

## 2. Tesis corregida del producto

```text
GradeOps AI permite a docentes crear, aplicar, revisar y calificar evaluaciones abiertas y cerradas con asistencia AI, manteniendo autoridad docente, trazabilidad, evidencia operacional y acceso seguro para estudiantes sin cuenta mediante links enviados por correo.
```

---

## 3. Diferencia entre evaluación abierta y cerrada

| Dimensión | Evaluación abierta | Evaluación cerrada |
|---|---|---|
| Tipo de respuesta | Desarrollo, código, archivo, texto libre, respuesta extensa | Selección de alternativa(s) |
| Creación | AI genera contexto, caso, instrucciones, entregables y rúbrica | AI genera preguntas, alternativas, clave, metadatos y configuración |
| Corrección | AI sugiere evaluación contra rúbrica | Motor determinístico compara contra clave |
| Control docente | Revisa, edita y aprueba rúbrica, sugerencias y nota | Revisa, edita y aprueba preguntas, pauta y resultados excepcionales |
| Resultado | Nota ponderada con feedback cualitativo | Nota ponderada con análisis objetivo |
| Riesgo principal | Subjetividad / inconsistencia de criterio | Error en clave, alternativas ambiguas o mala configuración |
| Mejor uso de AI | Generación, rúbrica, feedback, brechas, recuperación | Generación de ítems, distractores, balance, explicación, reportes |
| Cálculo de nota | Asistido por AI + aprobación docente | Determinístico |

---

## 4. Ciclo común de toda evaluación

Ambos tipos deben terminar en el mismo objetivo operacional:

```text
Alumno
  -> realiza evaluación
  -> sistema procesa respuesta
  -> docente revisa/controla según tipo
  -> sistema calcula o consolida nota
  -> nota queda asociada a alumno + asignatura + evaluación
  -> alumno puede consultar resultado mediante portal seguro sin cuenta
```

### Estado común de una evaluación

```text
draft
  -> pending_teacher_review
  -> approved
  -> published
  -> accepting_responses
  -> responses_received
  -> grading_in_progress
  -> pending_teacher_review
  -> graded
  -> results_published
  -> archived
```

### Estado común de un alumno frente a una evaluación

```text
invited
  -> link_sent
  -> access_validated
  -> started
  -> submitted
  -> processing
  -> graded
  -> result_available
  -> viewed_result
```

---

## 5. Evaluación abierta

### 5.1 Flujo de creación

```text
Docente define intención evaluativa
  -> AI genera borrador de evaluación
      - contexto
      - caso
      - instrucciones
      - entregables esperados
      - restricciones
      - criterios de evaluación
  -> AI genera rúbrica
      - criterios
      - ponderaciones
      - niveles de desempeño
      - errores frecuentes
      - condiciones de aprobación
  -> docente revisa/edita/aprueba
  -> evaluación se publica
  -> sistema genera links únicos para estudiantes
```

### 5.2 Flujo de entrega del alumno

```text
Alumno recibe email
  -> abre link único
  -> sistema valida token
  -> alumno ve evaluación
  -> alumno responde o sube entrega
  -> alumno confirma envío
  -> sistema registra entrega
  -> link queda cerrado o limitado según política
```

### 5.3 Flujo de corrección

```text
Entrega recibida
  -> AI analiza contra rúbrica aprobada
  -> AI sugiere puntaje por criterio
  -> AI genera feedback
  -> AI marca incertidumbres
  -> docente revisa
      - aprueba
      - edita
      - rechaza
      - pide regeneración
  -> sistema consolida nota final
  -> resultado queda listo para publicar
```

### 5.4 Elementos clave

- La rúbrica aprobada es el contrato de corrección.
- La AI no debe publicar nota sin revisión docente.
- El estudiante no necesita cuenta.
- La entrega debe quedar trazable.
- El docente debe poder comparar sugerencia AI vs nota final.
- El feedback debe ser editable antes de estar disponible para el alumno.

---

## 6. Evaluación cerrada

### 6.1 Flujo de creación

```text
Docente define intención evaluativa
  -> AI genera lote de preguntas
      - enunciado
      - alternativas
      - respuesta correcta
      - explicación
      - dificultad
      - resultado de aprendizaje
      - tags
  -> sistema valida estructura
      - alternativas completas
      - clave definida
      - cardinalidad según tipo
      - sin duplicados
      - sin errores críticos
  -> docente revisa/edita/aprueba preguntas
  -> preguntas aprobadas entran al banco
  -> AI o docente arma evaluación
  -> docente revisa pauta y ponderación
  -> sistema congela snapshot
  -> evaluación se publica
  -> sistema genera links únicos para estudiantes
```

### 6.2 Flujo de respuesta del alumno

```text
Alumno recibe email
  -> abre link único
  -> sistema valida token
  -> alumno ve evaluación cerrada
  -> responde seleccionando alternativas
  -> sistema guarda progreso si aplica
  -> alumno envía intento
  -> sistema bloquea o marca intento como enviado
```

### 6.3 Flujo de calificación

```text
Intento recibido
  -> sistema compara respuestas contra snapshot de clave
  -> calcula puntaje por pregunta
  -> calcula puntaje total
  -> convierte a nota según escala
  -> identifica excepciones
  -> docente revisa resumen/excepciones
  -> docente publica resultados
```

### 6.4 Elementos clave

- La calificación no depende de AI.
- La AI ayuda a crear, mejorar y explicar.
- La clave queda congelada al publicar.
- Cambios posteriores requieren nueva versión o recálculo auditado.
- El docente no debe revisar cada respuesta si no hay excepciones.
- El reporte debe mostrar desempeño por pregunta y resultado de aprendizaje.

---

## 7. Acceso del alumno sin cuenta

### 7.1 Principio

El estudiante no tendrá cuenta en la primera versión.  
El acceso se realiza mediante **links únicos, seguros, enviados al correo del estudiante**.

Esto aplica para:

- realizar una evaluación abierta;
- responder una evaluación cerrada;
- revisar resultados publicados;
- descargar feedback o reporte individual si corresponde.

### 7.2 Flujo de invitación

```text
Docente publica evaluación
  -> sistema genera lista de destinatarios
  -> sistema crea token único por alumno/evaluación
  -> sistema envía email
  -> alumno abre link
  -> sistema valida token
  -> alumno accede solo a su evaluación
```

### 7.3 Tipos de link

| Link | Uso |
|---|---|
| `assessment_access_link` | Permite rendir/responder la evaluación |
| `submission_resume_link` | Permite continuar si se permite guardado parcial |
| `result_access_link` | Permite ver resultado publicado |
| `feedback_access_link` | Permite ver feedback individual |
| `magic_reauth_link` | Permite revalidar acceso desde correo |

### 7.4 Reglas de seguridad mínimas

- Token único por alumno + evaluación.
- Token firmado o aleatorio de alta entropía.
- Expiración configurable.
- Uso único o uso limitado según tipo.
- Validación de estado de evaluación.
- No revelar si un correo existe.
- No permitir listar evaluaciones ajenas.
- Revocación manual por docente.
- Registro de accesos.
- Rate limiting.
- Reenvío de link con invalidación opcional del anterior.
- Vista estrictamente individual.

### 7.5 Portal libre de resultados

El portal libre no es una cuenta de estudiante. Es una zona de acceso validada por link.

Debe permitir:

```text
Alumno abre portal de resultados
  -> ingresa email o abre magic link
  -> recibe link de acceso
  -> ve sus evaluaciones publicadas
  -> entra al detalle de una evaluación
  -> ve nota, puntaje, feedback y estado
```

### 7.6 Qué puede ver el alumno

| Información | Evaluación abierta | Evaluación cerrada |
|---|---|---|
| Nota final | Sí | Sí |
| Puntaje total | Sí | Sí |
| Feedback docente/AI aprobado | Sí | Opcional |
| Rúbrica / criterios | Sí, si el docente lo publica | No aplica o resumen |
| Respuestas correctas | No siempre | Configurable |
| Preguntas erradas | No aplica | Configurable |
| Reforzamiento sugerido | Sí, si se publica | Sí, si se publica |
| Historial completo de auditoría | No | No |
| Logs AI internos | No | No |
| Costos/modelos | No | No |

---

## 8. Modelo de actores corregido

| Actor | Descripción |
|---|---|
| Docente | Crea, configura, aprueba, aplica, revisa y publica resultados |
| Alumno sin cuenta | Accede mediante link seguro para responder o ver resultados |
| Sistema AI | Genera, sugiere, analiza, redacta y resume |
| Motor determinístico | Corrige evaluaciones cerradas |
| Operador/Admin | Ve métricas, evidencia, costos, logs y soporte |
| Coordinador | Futuro: vista agregada por asignatura/cohorte |

---

## 9. Modelo de entidades sugerido

```text
Organization
  └── UserAccount

Subject
  └── CourseSection
      └── Enrollment / LearnerRef

LearnerRef
  ├── id
  ├── email
  ├── display_name
  └── external_identifier

Assessment
  ├── assessment_type: open | closed
  ├── subject_id
  ├── course_section_id
  ├── weighting
  ├── grading_scale
  ├── status
  └── published_at

OpenAssessment
  ├── context
  ├── case_statement
  ├── instructions
  ├── expected_deliverables
  └── rubric

ClosedAssessment
  ├── question_snapshots
  ├── option_snapshots
  ├── answer_key_snapshot
  └── scoring_policy

AssessmentInvitation
  ├── assessment_id
  ├── learner_ref_id
  ├── email
  ├── token_hash
  ├── expires_at
  ├── used_at
  ├── revoked_at
  └── status

AssessmentAttempt
  ├── assessment_id
  ├── learner_ref_id
  ├── started_at
  ├── submitted_at
  ├── status
  └── attempt_no

OpenSubmission
  ├── attempt_id
  ├── text_response
  ├── file_artifacts
  └── submitted_at

ClosedResponse
  ├── attempt_id
  ├── answer_items
  └── submitted_at

GradeResult
  ├── attempt_id
  ├── raw_score
  ├── weighted_score
  ├── final_grade
  ├── grading_status
  ├── teacher_approved_by
  └── published_at

StudentResultAccess
  ├── learner_ref_id
  ├── result_token_hash
  ├── expires_at
  ├── accessed_at
  └── revoked_at
```

---

## 10. Ponderación y nota final

Toda evaluación debe terminar generando una nota para un alumno en una asignatura.

### 10.1 Nota de evaluación

```text
resultado evaluación = puntaje obtenido convertido a escala
```

Ejemplo:

```text
Evaluación cerrada:
  17/20 puntos
  escala 1.0 a 7.0
  exigencia 60%
  nota = 6.1

Evaluación abierta:
  rúbrica 100 puntos
  AI sugiere 82
  docente ajusta a 86
  nota = 6.3
```

### 10.2 Ponderación en asignatura

Una evaluación puede tener ponderación dentro de una asignatura:

```text
nota ponderada = nota evaluación * porcentaje ponderación
```

Ejemplo:

```text
Evaluación 1: nota 6.0, ponderación 30%
Evaluación 2: nota 5.5, ponderación 40%
Evaluación 3: nota 6.4, ponderación 30%

nota asignatura = 6.0*0.3 + 5.5*0.4 + 6.4*0.3
```

### 10.3 Estados de nota

```text
calculated
  -> pending_teacher_approval
  -> teacher_approved
  -> published_to_student
  -> viewed_by_student
  -> locked
```

---

## 11. Implicancias sobre el MVP

La documentación previa que decía que el estudiante no necesitaba cuenta sigue siendo válida, pero debe corregirse la interpretación:

### Antes

```text
El alumno no necesita cuenta y no tiene portal.
```

### Ahora

```text
El alumno no necesita cuenta, pero sí necesita experiencia de acceso seguro mediante links para:
  - responder evaluaciones;
  - entregar desarrollos;
  - ver resultados publicados.
```

Esto evita construir un LMS completo, pero sí reconoce que la operación real de evaluación requiere una superficie mínima para el alumno.

---

## 12. Backlog P0 corregido

### Evaluaciones abiertas

- Crear evaluación abierta con AI.
- Generar contexto/caso/instrucciones.
- Generar rúbrica.
- Aprobar rúbrica.
- Enviar links únicos a estudiantes.
- Recibir entregas.
- Analizar entrega con AI.
- Generar sugerencia de puntaje y feedback.
- Revisar/aprobar por docente.
- Publicar resultado.

### Evaluaciones cerradas

- Generar preguntas con AI.
- Generar alternativas y clave.
- Validar estructura.
- Aprobar preguntas.
- Crear banco de preguntas.
- Armar evaluación desde banco.
- Aprobar pauta.
- Publicar evaluación.
- Enviar links únicos a estudiantes.
- Recibir respuestas.
- Calificar determinísticamente.
- Revisar excepciones.
- Publicar resultado.

### Alumno sin cuenta

- Generar link único seguro.
- Enviar email.
- Validar token.
- Acceder a evaluación.
- Enviar respuesta/entrega.
- Acceder a resultados publicados.
- Reenviar link si corresponde.
- Revocar link.
- Auditar accesos.

### Resultados

- Asociar nota a alumno.
- Asociar nota a evaluación.
- Asociar evaluación a asignatura.
- Aplicar ponderación.
- Publicar resultado individual.
- Portal libre de resultados por link/email.

---

## 13. Decisiones pendientes

1. **¿El link de evaluación será de uso único o permitirá reingreso antes de enviar?**
   - Recomendación: permitir reingreso antes de enviar, bloquear después del envío.

2. **¿El alumno podrá guardar avance?**
   - Recomendación: P1. En P0, envío único con advertencia.

3. **¿El alumno verá respuestas correctas en evaluaciones cerradas?**
   - Recomendación: configurable por docente.

4. **¿El alumno podrá apelar o pedir revisión?**
   - Recomendación: P2. Para MVP, solo vista de resultado.

5. **¿El portal libre requiere ingreso de email o solo magic link?**
   - Recomendación: ambos:
     - magic link directo desde email;
     - opción “reenviar acceso” ingresando email.

6. **¿Se permitirán múltiples intentos?**
   - Recomendación: configurable, pero P0 con intento único.

7. **¿La nota ponderada impacta promedio de asignatura dentro de GradeOps AI?**
   - Recomendación: sí como cálculo básico, no como LMS completo.

---

## 14. Principio final de producto

GradeOps AI debe evitar ser un LMS, pero ya no puede ser solo una consola docente.

Debe ser una **operación completa de evaluación**:

```text
Docente diseña con AI
  -> estudiante responde/entrega sin cuenta
  -> sistema corrige o asiste corrección
  -> docente conserva control
  -> alumno ve resultado seguro
  -> la asignatura recibe una nota ponderada
  -> la operación queda trazada
```

Esa es la versión más coherente del producto.

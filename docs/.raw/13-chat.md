# Ajuste estratégico — Banco de preguntas generado por AI en GradeOps AI

## Contexto del ajuste

En GRADE, el banco de preguntas estaba concebido principalmente como un repositorio construido manualmente por el docente o por roles académicos autorizados.

En GradeOps AI, ese supuesto cambia:

> El banco de preguntas y las evaluaciones construidas a partir de ese banco pueden ser generados por AI, siempre con revisión, curaduría y aprobación final del docente.

Este ajuste cambia el centro del producto. El banco ya no debe verse solo como una base de datos de preguntas reutilizables, sino como un **sistema de producción, curaduría, versionado y operación de ítems evaluativos asistido por AI**.

---

## Nueva tesis de producto

La tesis corregida debería ser:

```text
GradeOps AI permite que un docente genere un banco de preguntas con AI,
revise y apruebe los ítems, construya evaluaciones automáticamente desde ese banco,
aplique la evaluación, califique respuestas objetivas y obtenga reportes pedagógicos
con evidencia operacional.
```

Esto conserva lo mejor de GRADE:

- banco versionado;
- alternativas;
- pauta;
- snapshot;
- aplicación;
- ingesta;
- calificación automática;
- reportes;
- auditoría.

Pero agrega el diferencial de GradeOps AI:

- generación AI de preguntas;
- generación AI de alternativas;
- generación AI de distractores;
- validación AI de ambigüedad;
- sugerencia AI de dificultad;
- sugerencia AI de resultado de aprendizaje;
- armado AI de evaluaciones;
- reporte AI narrativo;
- reforzamiento AI posterior.

---

## Cambio conceptual principal

### Antes

```text
Docente crea manualmente preguntas
  -> banco guarda preguntas
  -> docente arma evaluación
  -> sistema aplica/califica
```

### Ahora

```text
Docente define intención evaluativa
  -> AI genera preguntas y alternativas
  -> AI propone metadatos y dificultad
  -> sistema valida estructura objetiva
  -> docente revisa/aprueba/edita/rechaza
  -> preguntas aprobadas entran al banco
  -> AI o docente arma evaluación desde banco
  -> docente aprueba pauta
  -> sistema aplica/califica/reporta
```

---

## Nuevo rol del banco de preguntas

El banco debe tener cuatro zonas funcionales:

```text
Banco de preguntas
├── Preguntas aprobadas
├── Preguntas generadas por AI pendientes de revisión
├── Preguntas rechazadas / descartadas
└── Preguntas retiradas / históricas
```

Esto es clave porque una pregunta generada por AI **no debe entrar automáticamente como ítem válido**.

Debe pasar por un flujo de curaduría.

---

## Estados sugeridos para una pregunta

```text
ai_generated
  -> pending_teacher_review
  -> approved
  -> active
  -> used_in_assessment
  -> retired
```

Estados alternativos:

```text
ai_generated
  -> rejected

approved
  -> needs_revision
  -> new_version_created
```

Tabla recomendada:

| Estado | Significado | Puede usarse en evaluación |
|---|---|---|
| `ai_generated` | La AI creó el ítem, pero aún no fue revisado | No |
| `pending_review` | Está esperando revisión docente | No |
| `approved` | El docente validó contenido, alternativas y clave | Sí |
| `active` | Está disponible en el banco | Sí |
| `needs_revision` | Tiene advertencias o fue reportada como ambigua | No, salvo confirmación |
| `retired` | Fue retirada para uso futuro | No |
| `rejected` | Fue descartada | No |
| `versioned` | Tiene una versión posterior | No como versión vigente |

---

## Nuevo flujo UX para generar banco con AI

```text
1. Docente define contexto
   - Asignatura / área
   - Unidad / tema
   - Resultado de aprendizaje
   - Nivel
   - Dificultad
   - Cantidad de preguntas
   - Tipo de preguntas
   - Restricciones pedagógicas

2. AI genera lote de preguntas
   - Enunciado
   - Alternativas
   - Respuesta correcta
   - Explicación de la respuesta
   - Dificultad sugerida
   - Resultado de aprendizaje sugerido
   - Tags
   - Riesgos de ambigüedad

3. Sistema valida estructura objetiva
   - TF: exactamente 2 opciones y 1 correcta
   - Selección única: 2+ opciones y 1 correcta
   - Selección múltiple: 2+ opciones y 1+ correctas
   - Sin alternativas duplicadas
   - Sin pregunta sin clave
   - Sin puntaje inválido

4. Docente revisa
   - Aprueba
   - Edita
   - Regenera
   - Rechaza
   - Marca como dudosa

5. Preguntas aprobadas pasan al banco activo

6. Preguntas rechazadas quedan como evidencia/descartadas
```

---

## Flujo UX para generar evaluación desde banco AI-generated

```text
1. Docente define evaluación
   - Objetivo
   - Duración
   - Cantidad de preguntas
   - Distribución de dificultad
   - Temas/resultados a cubrir
   - Tipo de preguntas
   - Puntaje total
   - Escala

2. AI propone composición
   - Selecciona preguntas aprobadas del banco
   - Balancea dificultad
   - Evita repetición excesiva
   - Cubre resultados de aprendizaje
   - Propone orden
   - Propone puntajes

3. Docente revisa matriz de evaluación
   - Preguntas seleccionadas
   - Dificultad
   - Resultado cubierto
   - Puntaje
   - Clave
   - Alertas

4. Sistema valida
   - Todas las preguntas están aprobadas
   - Todas tienen alternativas válidas
   - Todas tienen clave
   - Total de puntaje consistente
   - No hay ítems retirados
   - No hay ítems duplicados

5. Docente aprueba evaluación

6. Sistema crea snapshot
   - Preguntas congeladas
   - Alternativas congeladas
   - Clave congelada
   - Puntajes congelados
   - Escala congelada
```

---

## Nuevos agentes o capacidades AI necesarias

### 1. Question Generation Agent

Genera preguntas objetivas desde un objetivo docente.

Debe producir:

- enunciado;
- tipo de pregunta;
- alternativas;
- respuesta(s) correcta(s);
- explicación;
- dificultad;
- resultado de aprendizaje sugerido;
- tags;
- justificación pedagógica.

### 2. Distractor Quality Agent

Evalúa si las alternativas incorrectas son plausibles.

Debe detectar:

- distractores obvios;
- alternativas absurdas;
- alternativas duplicadas;
- alternativas parcialmente correctas;
- pistas involuntarias;
- longitud desbalanceada;
- sesgos de redacción.

### 3. Ambiguity Review Agent

Detecta problemas de interpretación.

Debe marcar:

- enunciado ambiguo;
- más de una respuesta potencialmente válida;
- falta de contexto;
- términos no enseñados;
- pregunta con doble negación;
- alternativa dependiente de opinión;
- inconsistencia con nivel declarado.

### 4. Assessment Assembly Agent

Arma evaluaciones desde el banco aprobado.

Debe considerar:

- cantidad de preguntas;
- dificultad;
- temas;
- resultados de aprendizaje;
- historial de uso;
- tiempo estimado;
- balance de tipos;
- puntaje total;
- cobertura.

### 5. Item Analytics Agent

Después de aplicar evaluaciones, interpreta desempeño de ítems.

Debe detectar:

- pregunta demasiado fácil;
- pregunta demasiado difícil;
- posible ambigüedad por baja tasa de acierto;
- distractores que nadie eligió;
- distractor demasiado atractivo;
- contenido que requiere reforzamiento.

---

## Regla de oro

La AI puede **crear, sugerir, clasificar, balancear y explicar**.

Pero no debe:

- activar automáticamente preguntas sin aprobación;
- publicar una evaluación sin aprobación docente;
- modificar una clave ya aplicada sin trazabilidad;
- corregir objetivamente usando juicio probabilístico cuando existe clave determinística.

La calificación de alternativas debe seguir siendo:

```text
respuesta del estudiante
+ snapshot de clave aprobada
+ política de puntaje
+ escala de nota
= resultado calculado
```

---

## Cambios requeridos en el análisis anterior

### Donde decía

> “El banco de preguntas debe ser presentado como repositorio pedagógico vivo.”

Debe cambiar a:

> “El banco de preguntas debe ser presentado como una fábrica curada de ítems evaluativos: AI genera, el docente valida, el sistema versiona y las evaluaciones consumen solo preguntas aprobadas.”

### Donde decía

> “Camino A: desde objetivo -> AI sugiere preguntas -> docente aprueba/edit”

Debe quedar como flujo principal, no secundario.

Nuevo orden:

```text
Camino principal:
  Desde objetivo -> AI genera banco/lote -> docente cura -> evaluación se arma desde aprobadas

Camino secundario:
  Desde banco existente -> docente selecciona -> evaluación se arma manualmente

Camino híbrido:
  Banco existente + generación AI para cubrir gaps
```

### Donde decía

> “Banco simple con búsqueda/filtros”

Debe ampliarse a:

> “Banco AI-assisted con cola de revisión, aprobación docente, validaciones automáticas y trazabilidad de generación.”

---

## Cambios en priorización

### P0 corregido

- Generar preguntas con AI desde objetivo.
- Generar alternativas y clave sugerida.
- Validar cardinalidad y estructura de pregunta.
- Cola de revisión docente de preguntas AI-generated.
- Aprobar/editar/rechazar pregunta.
- Guardar pregunta aprobada en banco.
- Crear evaluación desde preguntas aprobadas.
- Permitir que AI arme evaluación desde banco.
- Revisar y aprobar pauta.
- Crear snapshot al publicar.
- Calificar respuestas de forma determinística.
- Generar reporte por evaluación e ítem.
- Registrar logs AI y aprobación docente.

### P1 corregido

- Regenerar lote completo o preguntas individuales.
- Mejorar distractores con AI.
- Detectar preguntas similares/duplicadas.
- Sugerir cobertura por outcome.
- Rebalancear evaluación por dificultad.
- Analítica histórica por ítem.
- Anulación y recálculo auditado.
- PDF/QR si el demo necesita prueba física.

### P2 corregido

- OCR móvil robusto.
- Banco compartido entre docentes.
- Marketplace de bancos.
- Integraciones LMS.
- Proctoring.
- Analítica longitudinal.

---

## Modelo de datos ajustado

Además del modelo objetivo base, se requieren campos para procedencia AI.

### `Question`

Campos recomendados adicionales:

| Campo | Propósito |
|---|---|
| `source_type` | `manual`, `ai_generated`, `imported` |
| `source_agent_execution_id` | vínculo al log AI que generó la pregunta |
| `generation_prompt_summary` | resumen del input usado |
| `ai_confidence` | confianza o calidad estimada |
| `review_status` | estado de revisión docente |
| `reviewed_by` | docente que aprobó/editó/rechazó |
| `reviewed_at` | fecha de revisión |
| `rejection_reason` | motivo si se rechaza |
| `quality_flags_json` | ambigüedad, distractores débiles, duplicidad |
| `approved_version_id` | versión aprobada actual |

### `QuestionOption`

Campos recomendados adicionales:

| Campo | Propósito |
|---|---|
| `is_ai_generated` | identifica alternativa generada por AI |
| `distractor_rationale` | por qué la alternativa es plausible |
| `quality_flags_json` | problemas detectados |
| `edited_by_teacher` | indica intervención humana |

### `QuestionReviewEvent`

Entidad nueva sugerida:

| Campo | Propósito |
|---|---|
| `id` | identificador |
| `question_id` | pregunta revisada |
| `actor_user_id` | docente |
| `action` | approve/edit/reject/regenerate/request_changes |
| `notes` | comentario docente |
| `previous_status` | estado previo |
| `new_status` | estado nuevo |
| `created_at` | auditoría |

---

## UX específica para la cola de revisión

La pantalla clave del nuevo banco no es solo “lista de preguntas”.

Debe existir una pantalla:

# Revisión de preguntas generadas por AI

Cada card de pregunta debe mostrar:

- enunciado;
- tipo;
- alternativas;
- alternativa(s) correcta(s);
- explicación de la respuesta;
- dificultad sugerida;
- outcome sugerido;
- alertas de calidad;
- acciones:
  - aprobar;
  - editar;
  - regenerar alternativas;
  - regenerar pregunta;
  - rechazar;
  - guardar como borrador.

### Indicadores visuales recomendados

| Indicador | Uso |
|---|---|
| `Lista para aprobar` | Sin errores estructurales |
| `Requiere revisión` | Hay advertencias |
| `Inválida` | Falta clave, alternativas o regla |
| `Posible ambigüedad` | AI detectó más de una interpretación |
| `Distractores débiles` | Alternativas incorrectas muy obvias |
| `Duplicada probable` | Similar a otra del banco |

---

## Nuevo pitch funcional del módulo

> GradeOps AI no solo genera evaluaciones. Construye un banco de preguntas curado por el docente: la AI propone ítems, alternativas y claves; el sistema valida estructura y trazabilidad; el docente aprueba; y las evaluaciones se arman automáticamente desde preguntas confiables.

---

## Conclusión corregida

La recomendación ya no debe ser simplemente “absorber el banco de GRADE”.

La recomendación correcta es:

```text
Tomar el modelo robusto de banco de GRADE,
pero convertirlo en un banco AI-native:
AI genera preguntas y evaluaciones,
el sistema valida,
el docente cura,
y solo lo aprobado se aplica/califica.
```

Esto hace que GradeOps AI sea más potente que GRADE en su versión original, porque no solo administra preguntas: **produce, mejora, valida, organiza y opera evaluaciones completas asistidas por AI**.

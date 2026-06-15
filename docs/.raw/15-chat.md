# GradeOps AI — Ingesta de respuestas digitales y físicas

## 1. Decisión de producto

GradeOps AI debe soportar dos canales principales de ingesta de respuestas:

1. **Ingesta digital directa**
   - El alumno responde en una interfaz web/portal.
   - Accede mediante link único y seguro enviado a su correo.
   - No requiere cuenta.
   - El sistema valida identidad operacional mediante token.
   - La respuesta queda asociada automáticamente a alumno, evaluación, asignatura e intento.

2. **Ingesta física digitalizada**
   - El alumno responde en una hoja física.
   - La evaluación puede ser entregada impresa.
   - El alumno marca o escribe sus respuestas en una hoja de respuesta.
   - El profesor escanea o fotografía la hoja.
   - El sistema procesa la imagen mediante OCR/OMR o técnica equivalente.
   - El sistema detecta respuestas y las ingresa como intento del alumno.
   - El docente revisa y confirma casos dudosos.

---

## 2. Principio central

```text
Toda respuesta, independiente del canal de ingreso,
debe terminar convertida en un intento normalizado.
```

Es decir:

```text
Portal digital
  -> respuesta estructurada
  -> intento normalizado

Hoja física escaneada/fotografiada
  -> extracción OCR/OMR
  -> validación
  -> intento normalizado
```

Luego de eso, el flujo de calificación debe ser común:

```text
intento normalizado
  -> evaluación abierta o cerrada
  -> corrección asistida o determinística
  -> nota
  -> publicación de resultado
```

---

## 3. Distinción OCR vs OMR

Para el producto conviene separar los conceptos:

| Técnica | Uso principal | Aplica mejor a |
|---|---|---|
| OCR | Reconocer texto escrito o impreso | nombre, código, respuestas abiertas breves, identificadores |
| OMR | Reconocer marcas, burbujas o alternativas seleccionadas | evaluaciones cerradas de alternativas |
| QR / código único | Identificar evaluación, alumno o intento | vinculación segura de hoja física |
| Computer Vision | Detectar orientación, calidad, bordes, zonas de respuesta | captura fotográfica móvil |

Para evaluaciones cerradas impresas, la técnica más apropiada no es OCR puro, sino **OMR + QR/ID + validación visual**.

---

## 4. Canales de ingesta

## 4.1 Canal A — Respuesta digital directa

### Flujo

```text
Docente publica evaluación
  -> sistema genera link único por alumno
  -> sistema envía email
  -> alumno abre link
  -> sistema valida token
  -> alumno responde en portal
  -> alumno confirma envío
  -> sistema registra intento
  -> intento pasa a calificación
```

### Aplica a

| Tipo de evaluación | Uso |
|---|---|
| Abierta | Respuesta escrita, desarrollo, código, archivo |
| Cerrada | Selección de alternativas en interfaz |
| Mixta futura | Preguntas abiertas + cerradas |

### Ventajas

- Menos fricción operativa.
- Identificación automática.
- Menos errores de lectura.
- Registro inmediato.
- Permite guardado parcial si se implementa.
- Permite calificación inmediata para cerradas.

### Riesgos

- Requiere acceso a dispositivo e internet.
- Requiere control de expiración y uso del link.
- Riesgo de reenvío del link.
- Necesita bloqueo de edición post-envío.

---

## 4.2 Canal B — Hoja física con digitalización docente

### Flujo

```text
Docente publica evaluación cerrada
  -> sistema genera versión imprimible
  -> sistema genera hoja de respuesta
  -> hoja incluye ID/QR de evaluación y alumno
  -> alumno responde en papel
  -> alumno entrega hoja al profesor
  -> profesor escanea o fotografía
  -> sistema detecta hoja y respuestas
  -> sistema valida calidad y consistencia
  -> docente revisa casos dudosos
  -> sistema registra intento
  -> intento pasa a calificación
```

### Aplica principalmente a

| Tipo de evaluación | Uso recomendado |
|---|---|
| Cerrada | Muy recomendado |
| Abierta | Solo si hay OCR/transcripción posterior, no como P0 |
| Mixta | Futuro, con separación de zonas |

### Ventajas

- Funciona en contextos con baja conectividad.
- Compatible con sala de clases tradicional.
- Permite evaluación impresa.
- Reduce barrera de adopción docente.
- No obliga al alumno a usar dispositivo.

### Riesgos

- Calidad variable de foto/escaneo.
- Respuestas ambiguas.
- Marcas dobles.
- Hojas dañadas.
- Error de vinculación alumno/evaluación.
- Necesidad de revisión manual.

---

## 5. Hoja de respuesta física

## 5.1 Elementos obligatorios

Toda hoja de respuesta física debe incluir:

- ID único de evaluación.
- ID único de intento o invitación.
- Identificador del alumno.
- QR o código visual seguro.
- Versión de evaluación.
- Instrucciones de marcado.
- Área de respuestas.
- Marcas de alineación.
- Espacio para validación humana si corresponde.

### Ejemplo conceptual

```text
┌───────────────────────────────────────────────┐
│ GradeOps AI                                   │
│ Evaluación: EVA-2026-00045                    │
│ Alumno: Ana Pérez                             │
│ Curso: Programación I                         │
│ QR: [assessment_invitation_token]             │
├───────────────────────────────────────────────┤
│ Instrucciones: marque una alternativa por ítem │
├───────────────────────────────────────────────┤
│ 1.  A ○   B ○   C ○   D ○                     │
│ 2.  A ○   B ○   C ○   D ○                     │
│ 3.  A ○   B ○   C ○   D ○                     │
│ ...                                           │
└───────────────────────────────────────────────┘
```

---

## 5.2 Reglas de diseño de hoja

- Una hoja debe corresponder a una evaluación y alumno específicos.
- La hoja debe tener un QR/ID que evite asociación manual cuando sea posible.
- La hoja debe incluir versión de evaluación.
- El orden de preguntas y alternativas debe coincidir con el snapshot publicado.
- Si se permite aleatorización, la hoja debe conocer su forma/variante.
- Las marcas deben ser claras y separadas.
- Las zonas de respuesta deben ser detectables.
- Debe existir tolerancia a rotación, sombras y perspectiva.
- Debe haber estados de confianza por respuesta.

---

## 6. Procesamiento de hoja física

## 6.1 Flujo de captura

```text
Profesor abre módulo de ingesta
  -> selecciona evaluación o escanea QR
  -> toma foto/sube escaneo
  -> sistema valida imagen
      - nitidez
      - iluminación
      - orientación
      - hoja completa
      - QR legible
  -> sistema acepta o pide recaptura
```

## 6.2 Flujo de extracción

```text
Imagen aceptada
  -> detectar QR/ID
  -> identificar evaluación
  -> identificar alumno/invitación
  -> identificar versión/forma
  -> detectar zonas de respuesta
  -> leer marcas seleccionadas
  -> calcular confianza por ítem
  -> generar pre-ingesta
```

## 6.3 Flujo de validación docente

```text
Sistema muestra pre-ingesta
  -> respuestas detectadas
  -> nivel de confianza
  -> advertencias
  -> imagen original
  -> docente confirma/corrige
  -> sistema registra intento normalizado
```

---

## 7. Estados de ingesta

## 7.1 Estado de una captura

```text
uploaded
  -> image_quality_check
  -> accepted_for_processing
  -> rejected_quality
  -> processing
  -> extraction_completed
  -> needs_human_review
  -> confirmed
  -> failed
```

## 7.2 Estado de una respuesta detectada

```text
detected
  -> high_confidence
  -> low_confidence
  -> ambiguous
  -> manually_corrected
  -> confirmed
```

## 7.3 Estado de un intento físico

```text
paper_received
  -> scanned
  -> extracted
  -> pending_teacher_confirmation
  -> normalized_attempt_created
  -> graded
```

---

## 8. Casos ambiguos que el sistema debe manejar

| Caso | Acción recomendada |
|---|---|
| QR ilegible | Permitir selección manual de evaluación/alumno con auditoría |
| Hoja sin alumno | Marcar como pendiente de identificación |
| Dos alternativas marcadas en selección única | Marcar ítem como ambiguo |
| Marca débil | Mostrar baja confianza |
| Respuesta borrada | Requiere revisión docente |
| Hoja incompleta | Rechazar o procesar parcialmente con alerta |
| Foto borrosa | Solicitar recaptura |
| Foto con sombra fuerte | Solicitar recaptura |
| Evaluación no coincide con hoja | Bloquear ingesta |
| Versión incorrecta | Bloquear o pedir confirmación controlada |
| Duplicado de hoja ya procesada | Alertar y requerir decisión |
| Alumno ya respondió digitalmente | Marcar conflicto de intento |
| Alumno no pertenece a la sección | Bloquear o permitir excepción auditada |

---

## 9. Conflictos de canal

GradeOps AI debe evitar que un mismo alumno tenga dos respuestas válidas para la misma evaluación, salvo que el docente lo permita.

### Escenarios

| Escenario | Regla recomendada |
|---|---|
| Alumno respondió online y también entregó hoja | Bloquear segundo intento o pedir decisión docente |
| Hoja física procesada dos veces | Detectar duplicado |
| Link digital usado después de ingesta física | Bloquear si ya existe intento confirmado |
| Profesor sube hoja antes de publicar evaluación | No permitir |
| Alumno inició online, pero entrega en papel | Permitir reemplazo solo con aprobación docente |
| Evaluación permite múltiples intentos | Registrar intento N con política explícita |

---

## 10. Relación con evaluaciones abiertas y cerradas

## 10.1 Cerradas

Las evaluaciones cerradas son el caso ideal para doble canal:

```text
cerrada online
  -> selección directa
  -> calificación determinística

cerrada en papel
  -> OMR/QR
  -> validación docente
  -> calificación determinística
```

## 10.2 Abiertas

Las evaluaciones abiertas pueden tener ingesta digital directa como P0:

```text
abierta online
  -> texto / archivo / código
  -> análisis AI
  -> revisión docente
```

La ingesta física de respuestas abiertas debería quedar como P1/P2, porque requiere transcripción OCR y revisión más intensa:

```text
abierta papel
  -> foto/escaneo
  -> OCR
  -> transcripción
  -> revisión docente
  -> análisis AI
```

Recomendación:

```text
P0:
  - abierta digital
  - cerrada digital
  - cerrada papel con OMR/QR básico

P1:
  - abierta papel mediante OCR asistido

P2:
  - corrección avanzada de manuscritos extensos
```

---

## 11. Modelo de datos sugerido

```text
Assessment
  ├── assessment_type: open | closed
  ├── delivery_modes_allowed: online | paper | both
  └── status

AssessmentInvitation
  ├── assessment_id
  ├── learner_ref_id
  ├── secure_token_hash
  ├── email
  ├── status
  └── expires_at

AssessmentAttempt
  ├── assessment_id
  ├── learner_ref_id
  ├── channel: online | paper_scan
  ├── status
  ├── started_at
  ├── submitted_at
  └── confirmed_by_teacher_id

PaperAnswerSheet
  ├── assessment_id
  ├── learner_ref_id
  ├── invitation_id
  ├── form_version
  ├── qr_payload_hash
  ├── generated_at
  └── status

PaperCapture
  ├── paper_answer_sheet_id
  ├── uploaded_by_teacher_id
  ├── file_url
  ├── image_quality_status
  ├── processing_status
  ├── created_at
  └── processed_at

ExtractedAnswer
  ├── paper_capture_id
  ├── question_snapshot_id
  ├── selected_option_snapshot_id
  ├── confidence
  ├── detection_status
  ├── manually_corrected
  └── confirmed_value

IngestionReviewEvent
  ├── paper_capture_id
  ├── actor_user_id
  ├── action
  ├── previous_value
  ├── new_value
  ├── reason
  └── created_at
```

---

## 12. UX del docente para ingesta física

La pantalla de ingesta no debe ser una simple carga de archivo. Debe funcionar como una estación de control.

### Pantalla: Ingesta de hojas

Debe mostrar:

- evaluación seleccionada;
- curso/sección;
- cantidad de alumnos esperados;
- hojas procesadas;
- hojas pendientes;
- hojas con error;
- hojas duplicadas;
- hojas con baja confianza;
- botón para subir archivo o tomar foto;
- estado del lote.

### Pantalla: Revisión de captura

Debe mostrar:

- imagen original;
- datos detectados:
  - alumno;
  - evaluación;
  - versión;
  - fecha;
- respuestas detectadas;
- confianza por pregunta;
- alertas;
- acciones:
  - confirmar;
  - corregir respuesta;
  - rechazar captura;
  - pedir nueva captura;
  - marcar como duplicada;
  - asociar manualmente.

---

## 13. UX del alumno para ingesta digital

### Evaluación cerrada online

```text
Alumno abre link
  -> ve instrucciones
  -> responde preguntas
  -> revisa resumen
  -> confirma envío
  -> recibe confirmación
```

### Evaluación abierta online

```text
Alumno abre link
  -> ve contexto/caso/instrucciones
  -> escribe respuesta o sube archivo
  -> revisa entrega
  -> confirma envío
  -> recibe confirmación
```

### Reglas UX

- Mostrar claramente fecha límite.
- Mostrar si el envío es único.
- Bloquear edición después del envío.
- Confirmar recepción.
- No mostrar resultados hasta publicación docente.
- Permitir reenvío de link al correo si corresponde.
- No pedir contraseña.

---

## 14. Reglas de seguridad

- El link digital no debe exponer IDs internos.
- La hoja física no debe contener datos sensibles innecesarios.
- El QR debe apuntar a un identificador firmado o tokenizado.
- El sistema debe guardar hash del token, no token plano.
- El acceso debe expirar.
- El docente debe poder revocar links.
- El sistema debe auditar:
  - apertura de evaluación;
  - envío de respuesta;
  - subida de hoja;
  - procesamiento;
  - corrección manual;
  - confirmación docente.
- Las imágenes de hojas deben tratarse como evidencia sensible.
- No permitir acceso cruzado entre alumnos.

---

## 15. Relación con calificación

Después de la ingesta, el sistema debe crear un intento normalizado.

### Cerrada online

```text
ClosedResponse
  -> GradeEngine
  -> GradeResult
```

### Cerrada papel

```text
PaperCapture
  -> ExtractedAnswer
  -> TeacherConfirmation
  -> ClosedResponse normalizada
  -> GradeEngine
  -> GradeResult
```

### Abierta online

```text
OpenSubmission
  -> AI Grading Suggestion
  -> TeacherReview
  -> GradeResult
```

### Abierta papel futura

```text
PaperCapture
  -> OCR transcription
  -> TeacherConfirmation
  -> OpenSubmission normalizada
  -> AI Grading Suggestion
  -> TeacherReview
  -> GradeResult
```

---

## 16. Backlog P0 recomendado

### Ingesta digital

- Link seguro por alumno/evaluación.
- Portal de respuesta sin cuenta.
- Respuesta cerrada online.
- Entrega abierta online.
- Confirmación de envío.
- Bloqueo post-envío.
- Registro de intento.

### Ingesta física cerrada

- Generar hoja de respuesta imprimible.
- Incluir QR/ID de evaluación, alumno, intento y versión.
- Subir foto/escaneo.
- Validar calidad básica.
- Leer QR/ID.
- Detectar alternativas marcadas.
- Calcular confianza por respuesta.
- Mostrar pre-ingesta al docente.
- Permitir corrección manual.
- Confirmar ingesta.
- Crear intento normalizado.

### Auditoría y conflictos

- Detectar duplicados.
- Detectar conflicto online vs papel.
- Registrar correcciones manuales.
- Mantener evidencia de captura.
- Asociar canal de respuesta al intento.

---

## 17. Backlog P1 recomendado

- Procesamiento por lote de varias hojas.
- Recorte automático de hoja.
- Corrección de perspectiva.
- Comparación visual de original vs detección.
- Reintentos de captura.
- OCR para respuestas abiertas breves.
- Modo móvil docente para captura en sala.
- Estado de avance por curso/sección.
- Reenvío de links a alumnos pendientes.
- Exportación de reporte de ingesta.

---

## 18. Backlog P2 recomendado

- OCR avanzado de manuscritos extensos.
- Evaluaciones mixtas papel/digital.
- App móvil nativa para captura.
- Reconocimiento offline.
- Corrección automática de hojas sin QR.
- Banco de plantillas físicas.
- Firma o comprobante del alumno.
- Revisión colaborativa por ayudante/co-docente.

---

## 19. Principio final de ingesta

GradeOps AI debe permitir que el docente elija el canal más realista para su contexto:

```text
Si hay dispositivos e internet:
  alumno responde online por link seguro.

Si la evaluación es presencial/impresa:
  alumno responde en hoja física,
  docente escanea/fotografía,
  sistema extrae respuestas,
  docente valida excepciones.
```

En ambos casos, el producto debe llegar al mismo resultado:

```text
respuesta válida
  -> intento normalizado
  -> calificación
  -> nota ponderada
  -> resultado publicable para el alumno
```

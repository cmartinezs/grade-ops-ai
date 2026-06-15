# Análisis UX/Product — Absorción de capacidades de GRADE para GradeOps AI

**Rol de análisis:** Product Designer Senior / Product Strategist  
**Foco:** experiencia funcional, arquitectura de información y flujos de usuario  
**Alcance funcional estricto:** evaluaciones basadas en preguntas con alternativas  
**Fuentes revisadas:**  
- `Wanku-CL/wiki-wanku`, carpeta `products/grade/`
- `cmartinezs/grade-ops-ai-docs`
- Antecedente estratégico de GradeOps AI / hackathon

---

## 1. Resumen ejecutivo

GRADE y GradeOps AI no están documentando exactamente el mismo producto.

**GRADE** está planteado como una plataforma de evaluación objetiva y cerrada: banco de preguntas, alternativas, pauta, versiones, generación de entregables, aplicación, ingesta de respuestas, calificación automática, publicación de resultados, reportes pedagógicos y trazabilidad.

**GradeOps AI**, en su estado actual, está planteado como una consola operativa para evaluaciones prácticas de programación: el docente define un objetivo, agentes generan evaluación y rúbrica, se cargan respuestas/código, se sugieren notas, feedback, brechas, reforzamiento y evidencia operacional.

Para el alcance solicitado —**preguntas con alternativas**— GradeOps AI debe absorber de GRADE tres cosas centrales:

1. **El modelo de Banco de Preguntas como fuente de verdad.**
2. **El ciclo de vida formal de una evaluación objetiva.**
3. **La trazabilidad histórica entre pregunta, alternativa, versión, evaluación, respuesta y resultado.**

La principal brecha actual es que GradeOps AI tiene muy bien resuelto el discurso de operación, revisión docente, evidencias y logs, pero no tiene documentado con suficiente fuerza el flujo específico de evaluación objetiva: ítems, alternativas, clave, snapshots, escala, aplicación, respuestas cerradas, revisión de excepciones y reportes por ítem.

La oportunidad no es copiar GRADE tal cual. La oportunidad es convertir su estructura funcional en un **modo “Objective Assessment Ops”** dentro de GradeOps AI: un flujo simple, vendible y demostrable para que un docente genere, aplique y revise pruebas de alternativas con control, velocidad y evidencia.

---

## 2. Lectura comparada de producto

### 2.1 Qué representa GRADE

GRADE es un sistema centrado en el ciclo completo de evaluación objetiva.

Su flujo natural es:

```text
Banco de preguntas
  -> Composición de evaluación
  -> Generación de entregable
  -> Aplicación
  -> Ingesta de respuestas
  -> Calificación automática
  -> Revisión/publicación
  -> Reportes y trazabilidad
```

Los elementos más valiosos desde UX/Product son:

- Banco centralizado de preguntas.
- Preguntas versionadas.
- Tipos objetivos: verdadero/falso, selección única, selección múltiple.
- Alternativas como objetos propios.
- Metadatos pedagógicos: asignatura, unidad, tema, dificultad, resultado de aprendizaje.
- Ítems activos, retirados o reactivables.
- Composición de evaluación desde preguntas vigentes.
- PDF o formato digital con ID/QR.
- Estado de evaluación: borrador, publicada/lista, aplicada, calificada, archivada.
- Asociación a curso/sección y alumnos.
- Ingesta por CSV, web, foto u OCR.
- Validación de integridad antes de calificar.
- Calificación automática contra clave.
- Conversión de puntaje a nota.
- Reportes por evaluación y por ítem.
- Auditoría de acciones críticas.

### 2.2 Qué representa GradeOps AI actual

GradeOps AI está posicionado como operación de evaluación con agentes.

Su flujo natural es:

```text
Objetivo de aprendizaje
  -> Evaluación generada
  -> Rúbrica generada
  -> Aprobación docente
  -> Carga de submissions
  -> Sugerencia de calificación
  -> Feedback
  -> Brechas
  -> Reforzamiento
  -> Reporte docente
  -> Evidencia operacional
```

Los elementos más valiosos desde UX/Product son:

- Teacher workspace.
- Flujo guiado y no administrativo.
- Aprobación docente obligatoria.
- Estados claros de outputs generados por IA.
- Logs de agentes.
- Costos por ejecución.
- Evidencia para negocio/hackathon.
- Métrica de uso basada en submissions procesadas.
- Revisión, edición, rechazo y aprobación.
- Reporte docente con valor narrativo.
- Foco en no construir LMS completo.

### 2.3 Diagnóstico central

GRADE tiene la **estructura evaluativa objetiva**.  
GradeOps AI tiene la **estructura operativa, de revisión y evidencia**.

El nuevo producto debe unir ambas:

```text
GRADE aporta:
  dominio evaluativo cerrado, banco, pauta, alternativas, estados, reportabilidad

GradeOps AI aporta:
  operación guiada, teacher control, evidencia, logs, UX de consola, negocio
```

---

## 3. Principales gaps detectados

| Gap | Qué tiene GRADE | Qué falta o está débil en GradeOps AI | Impacto UX/Product | Recomendación |
|---|---|---|---|---|
| Banco de preguntas | Banco centralizado, versionado, reutilizable | El MVP actual desprioriza banco grande y se centra en evaluación generada | Sin banco no hay reutilización, trazabilidad ni consistencia | Crear banco liviano P0 para alternativas, aunque no sea institucional |
| Modelo de alternativas | Opciones con `is_correct`, posición y score parcial | No está modelado el ítem objetivo como entidad principal | No se puede hacer flujo cerrado confiable | Agregar `Question`, `QuestionOption`, `AnswerKey` y reglas por tipo |
| Tipos objetivos | TF, SC, MC con reglas de cardinalidad | GradeOps se centra en rúbricas y submissions de programación | La evaluación con alternativas queda fuera del lenguaje del producto | Crear modo “Alternatives Assessment” |
| Snapshot al publicar | GRADE congela opciones para preservar historia | No aparece equivalente para evaluación objetiva | Editar una pregunta podría alterar pruebas pasadas | Incorporar snapshot de preguntas/opciones al publicar |
| Ciclo formal de evaluación | Draft → Published/Applied/Graded/Archived | GradeOps tiene estados, pero orientados a rubrica/submission | Los estados no cubren bien aplicación de prueba objetiva | Definir lifecycle específico para alternativas |
| Entregable aplicable | PDF/digital con ID/QR | GradeOps no prioriza PDF/QR | Pierde capacidad de evaluación física/simple | Mantener PDF/QR como P1 o P0 según demo |
| Ingesta de respuestas cerradas | CSV, web, foto/OCR | GradeOps solo contempla submissions de texto/código | Falta flujo natural de alternativas | P0: web/CSV. P1: hoja física/OCR |
| Manejo de errores de ingesta | Reporte de errores por fila, alumno, columnas, OCR | GradeOps tiene errores genéricos de upload | Docente necesita resolver rápido sin perder confianza | Crear “intake validation queue” |
| Calificación automática objetiva | Comparación contra clave, puntaje, escala | GradeOps usa sugerencias por rúbrica | Alternativas no requieren IA para corregir; requieren exactitud | Motor determinístico + revisión de excepciones |
| Resultados por ítem | Dificultad, tasa de acierto, distribución | GradeOps reporta brechas y feedback, no necesariamente ítem | Se pierde análisis pedagógico de prueba objetiva | Reporte por pregunta obligatorio |
| Cursos/alumnos | Modelo explícito de curso y estudiantes | GradeOps minimiza identidad estudiantil con `student_identifier` | Bien para MVP, pero falta “roster ligero” | Adoptar lista liviana de participantes por evaluación |
| Roles y gobernanza | Admin, Coordinador, Docente | GradeOps simplifica Teacher/Operator/Admin | Correcto para MVP, pero falta vista agregada de coordinador | Mantener simple; agregar `Reviewer/Coordinator` como futuro cercano |
| Auditoría académica | Quién/cuándo/qué en acciones críticas | GradeOps audita agentes, costos y aprobaciones | Faltan eventos académicos no-IA | Unificar auditoría académica + agent logs |
| Publicación de resultados | Confirmación antes de publicar | GradeOps tiene aprobación de outputs | Falta evento “publicar resultados” | Separar “aprobado por docente” de “publicado/exportado” |
| Notificaciones | Hitos de calificación/errores/publicación | GradeOps no lo usa como flujo central | Aumenta carga del docente si debe revisar manualmente | P1: notificación de carga inválida/calificación lista |

---

## 4. Oportunidades de mejora para preguntas con alternativas

### 4.1 Convertir el banco en un asset de producto, no solo una tabla

El banco debe ser presentado como una herramienta de trabajo docente:

- “Mis preguntas”.
- “Preguntas sugeridas”.
- “Preguntas reutilizadas”.
- “Preguntas retiradas”.
- “Preguntas con bajo desempeño”.
- “Preguntas con posible ambigüedad”.
- “Preguntas usadas recientemente”.

Esto permite que el docente no vea el banco como una base de datos, sino como un **repositorio pedagógico vivo**.

### 4.2 Crear un flujo de generación de evaluación por dos caminos

El docente debe poder partir de:

```text
Camino A: desde objetivo
  "Quiero evaluar X contenido con Y dificultad"
  -> AI sugiere preguntas
  -> docente aprueba/edit

Camino B: desde banco
  "Quiero armar prueba con preguntas existentes"
  -> búsqueda/filtros
  -> selección manual
  -> generación de evaluación
```

Ambos caminos deben converger en la misma pantalla de revisión.

### 4.3 Usar AI para diseño, no para corrección objetiva

En preguntas con alternativas, la corrección debe ser determinística.

La IA puede ayudar en:

- Generar preguntas.
- Detectar ambigüedad.
- Sugerir distractores.
- Clasificar por dificultad.
- Asociar resultado de aprendizaje.
- Explicar resultados.
- Generar reporte narrativo.
- Proponer reforzamiento.

Pero la nota objetiva debe salir de:

```text
respuestas del estudiante + clave congelada + política de puntaje + escala
```

### 4.4 Agregar revisión de excepciones

La revisión docente no debe obligar a mirar todo cuando la prueba es objetiva.

Debe concentrarse en excepciones:

- Respuestas incompletas.
- Preguntas anuladas.
- Preguntas con más de una alternativa marcada en selección única.
- Estudiante duplicado.
- Respuesta sin estudiante asociado.
- CSV inválido.
- OCR con baja confianza.
- Cambio de clave después de aplicar.
- Ítem detectado como ambiguo.
- Reclamo o recorrección.

### 4.5 Diferenciar estados académicos de estados operativos

Para alternativas, el estado del ciclo debe separar:

- estado de la evaluación;
- estado del entregable;
- estado de ingesta;
- estado de calificación;
- estado de publicación.

Esto evita pantallas confusas y mejora la trazabilidad.

---

## 5. Arquitectura de información sugerida

### 5.1 Navegación principal propuesta

```text
GradeOps AI
├── Inicio / Workspace
│   ├── Evaluaciones recientes
│   ├── Pendientes de revisión
│   ├── Cargas con errores
│   ├── Reportes listos
│   └── Crear evaluación
│
├── Banco de preguntas
│   ├── Todas las preguntas
│   ├── Crear pregunta
│   ├── Generar con AI
│   ├── Preguntas retiradas
│   ├── Taxonomías
│   └── Desempeño de ítems
│
├── Evaluaciones
│   ├── Borradores
│   ├── Listas para aplicar
│   ├── Aplicadas
│   ├── Calificadas
│   ├── Publicadas
│   └── Archivadas
│
├── Aplicación / Ingesta
│   ├── Respuestas web
│   ├── Importar CSV
│   ├── Subir capturas / OCR
│   ├── Validaciones
│   └── Errores por resolver
│
├── Revisión y resultados
│   ├── Calificaciones
│   ├── Excepciones
│   ├── Ajustes / anulaciones
│   ├── Publicación
│   └── Exportación
│
├── Reportes
│   ├── Reporte por evaluación
│   ├── Dificultad por ítem
│   ├── Cobertura por resultado
│   ├── Distribución de notas
│   └── Reforzamiento sugerido
│
└── Evidencia / Auditoría
    ├── Eventos académicos
    ├── Aprobaciones docentes
    ├── AI/Agent logs
    ├── Costos estimados
    └── Export de evidencia
```

### 5.2 Pantallas críticas

#### Pantalla 1 — Workspace docente

Objetivo: mostrar al docente qué requiere acción.

Debe incluir:

- botón “Crear evaluación”;
- evaluaciones en curso;
- estados visibles;
- errores pendientes;
- resultados listos para publicar;
- evidencias/logs accesibles sin protagonismo excesivo.

#### Pantalla 2 — Banco de preguntas

Objetivo: administrar preguntas reutilizables sin sentirse como sistema institucional pesado.

Debe incluir:

- buscador;
- filtros por asignatura, unidad, tema, dificultad, resultado;
- tipo de pregunta;
- estado: activa, retirada, borrador;
- uso histórico;
- tasa de acierto;
- acciones rápidas: usar, editar, versionar, clonar, retirar.

#### Pantalla 3 — Crear pregunta

Campos mínimos:

- enunciado;
- tipo: verdadero/falso, selección única, selección múltiple;
- alternativas;
- correcta(s);
- puntaje o score parcial;
- explicación opcional;
- dificultad;
- taxonomía;
- resultado de aprendizaje;
- estado inicial.

#### Pantalla 4 — Crear evaluación

Debe ser wizard o stepper:

```text
1. Datos básicos
2. Selección/generación de preguntas
3. Pauta y puntajes
4. Revisión docente
5. Publicar/generar entregable
```

#### Pantalla 5 — Aplicación

Debe permitir:

- generar PDF;
- compartir link/formulario;
- descargar plantilla CSV;
- ver ID/QR;
- marcar evaluación como aplicada;
- habilitar recepción de respuestas.

#### Pantalla 6 — Ingesta y validación

Debe mostrar:

- respuestas recibidas;
- cantidad esperada vs recibida;
- estudiantes no asociados;
- respuestas incompletas;
- errores de formato;
- duplicados;
- OCR baja confianza;
- acciones para corregir.

#### Pantalla 7 — Calificación y revisión

Debe mostrar:

- nota sugerida/determinada;
- respuestas correctas/incorrectas;
- puntaje por estudiante;
- excepciones;
- overrides docentes;
- opción de recalcular;
- publicar resultados.

#### Pantalla 8 — Reporte pedagógico

Debe mostrar:

- promedio;
- mediana;
- distribución;
- tasa de aprobación;
- dificultad por ítem;
- preguntas problemáticas;
- cobertura de resultados;
- sugerencia de reforzamiento;
- exportación PDF/CSV.

#### Pantalla 9 — Auditoría/evidencia

Debe mostrar:

- creación de preguntas;
- versionado;
- publicación de evaluación;
- carga de respuestas;
- calificación;
- cambios de pauta;
- anulaciones;
- publicación de resultados;
- logs AI si hubo generación/reporte asistido.

---

## 6. Flujo recomendado: generación, aplicación y revisión

### 6.1 Flujo de generación

```text
Docente inicia evaluación
  -> define propósito, curso/sección, fecha, duración
  -> elige camino:
      A. generar preguntas con AI
      B. seleccionar desde banco
      C. combinar ambos
  -> revisa preguntas y alternativas
  -> valida que cada pregunta tenga respuesta correcta
  -> define puntajes
  -> define escala/umbral
  -> sistema ejecuta validaciones
  -> docente aprueba pauta
  -> sistema genera snapshot
  -> evaluación queda lista para aplicar
```

#### Reglas UX

- La evaluación no puede publicarse si hay preguntas sin alternativa correcta.
- La evaluación no puede publicarse si la suma de puntajes no es válida.
- La evaluación no puede publicarse si existen ítems retirados, salvo confirmación explícita y justificada.
- Toda pregunta generada por AI debe entrar como borrador y requerir aprobación.
- La clave debe quedar congelada al publicar.

### 6.2 Flujo de aplicación

```text
Evaluación lista
  -> docente elige modo de aplicación:
      - formulario web
      - PDF imprimible
      - CSV de respuestas
      - captura/OCR
  -> sistema genera ID/QR/link
  -> docente aplica evaluación
  -> sistema marca estado "Aplicada"
  -> recepción de respuestas queda habilitada
```

#### Reglas UX

- El ID/QR debe identificar evaluación, versión y eventualmente estudiante/copia.
- Si es PDF, debe tener pauta/hoja de respuestas separada o estructura clara para OCR.
- Si es digital, debe bloquear cambios en estructura después de iniciar aplicación.
- El docente debe poder descargar plantilla CSV antes o después de aplicar.

### 6.3 Flujo de revisión

```text
Respuestas recibidas
  -> sistema valida integridad
  -> si hay errores: cola de corrección
  -> si pasa validación: calificación automática
  -> sistema calcula puntajes y notas
  -> docente revisa resumen y excepciones
  -> docente puede anular pregunta / ajustar clave / excluir intento
  -> sistema recalcula con trazabilidad
  -> docente confirma resultados
  -> sistema genera reporte
  -> docente publica/exporta
```

#### Reglas UX

- Calificación objetiva no debe depender de IA.
- El sistema debe distinguir “calificado” de “publicado”.
- Los cambios posteriores a la calificación deben crear evento de auditoría.
- Si se modifica la pauta después de aplicar, debe existir recalculo versionado.
- El reporte debe separar datos calculados, ajustes docentes y recomendaciones generadas.

---

## 7. Matriz comparativa funcional

| Área | GRADE | GradeOps AI actual | Recomendación para nuevo producto |
|---|---|---|---|
| Posicionamiento | Plataforma de gestión de evaluaciones | Operación AI-native de evaluaciones | Posicionar como operación de evaluación, pero con modo objetivo cerrado |
| Usuario primario | Docente independiente / docente institucional | Docente de programación / bootcamp | Mantener docente como usuario primario |
| Banco de preguntas | Central y estratégico | Fuera o débil en MVP | Absorber como P0 liviano para alternativas |
| Tipos de pregunta | TF, selección única, selección múltiple | Evaluaciones prácticas/rúbricas | Agregar `ObjectiveQuestion` como entidad clave |
| Metadatos pedagógicos | Unidad, dificultad, resultado, tema | Topic/level/language para programación | Adaptar a taxonomía simple: área → tema → outcome |
| Versionado de preguntas | Sí | Versionado de rúbrica, no banco | Absorber versionado de ítems |
| Vigencia de ítems | Activo/retirado | No documentado | Absorber estado activo/retirado/borrador |
| Composición de evaluación | Desde banco | Desde objetivo generado por AI | Permitir ambos caminos |
| Generación AI | Fuera del MVP GRADE | Núcleo del MVP | Usar AI para generar preguntas, no para cerrar notas |
| Pauta/clave | Clave por alternativas | Rúbrica aprobada | Agregar answer key aprobada |
| Snapshot | Opciones congeladas al publicar | No documentado para alternativas | Debe ser obligatorio |
| Entregable | PDF/digital con ID/QR | No prioritario | P0 si demo necesita impresión; P1 si demo web |
| Aplicación | Curso/sección/alumnos | StudentSubmission sin cuenta | Adoptar lista liviana por evaluación |
| Ingesta | CSV, web, foto/OCR | paste/upload code/text | P0 web/CSV; P1 OCR |
| Calificación | Automática determinística | Sugerencia AI contra rúbrica | Motor objetivo determinístico |
| Revisión docente | Incongruencia detectada: falta ajuste manual | Muy fuerte: approve/edit/reject | Absorber teacher control de GradeOps |
| Reportes | Promedio, distribución, dificultad ítem, cobertura | Reporte, gaps, time saved, evidence | Fusionar: métricas objetivas + narrativa AI opcional |
| Auditoría | Académica/sistema | Agent logs/evidence/costos | Unificar ambos eventos |
| Roles | Admin, Coordinador, Docente | Teacher, Operator/Admin | Mantener simple; coordinador como futuro |
| Notificaciones | Hitos y errores | No central | P1 para calificación lista y errores |
| Business evidence | No es foco | Muy fuerte | Mantener como diferenciador |

---

## 8. Casos de borde detectados y mitigaciones

### 8.1 Banco de preguntas

| Edge case | Riesgo | Mitigación UX/Product |
|---|---|---|
| Pregunta sin alternativa correcta | Evaluación inválida | Bloquear publicación |
| Selección única con dos correctas | Calificación incorrecta | Validación por tipo |
| Verdadero/falso con más de dos opciones | Confusión y error de pauta | Regla fuerte de cardinalidad |
| Selección múltiple con todas correctas | Ítem de baja calidad | Advertencia de calidad |
| Alternativas duplicadas | Ambigüedad | Validación de textos duplicados |
| Pregunta editada después de ser usada | Se altera historia | Crear nueva versión, no editar aplicada |
| Pregunta retirada ya usada | Pérdida de trazabilidad | Retirada solo bloquea uso futuro |
| Pregunta clonada sin cambios | Duplicidad de banco | Detección de similitud |
| Taxonomía eliminada con preguntas asociadas | Inconsistencia curricular | No eliminar; desactivar |
| Dificultad mal clasificada | Evaluación desbalanceada | Permitir revisión y métricas post-aplicación |

### 8.2 Composición de evaluación

| Edge case | Riesgo | Mitigación UX/Product |
|---|---|---|
| Evaluación sin preguntas | Entregable inválido | Bloquear avance |
| Puntaje total cero | Nota imposible | Bloquear publicación |
| Puntaje parcial mayor al puntaje de la pregunta | Nota inflada | Validación |
| Ítem retirado seleccionado | Uso de contenido obsoleto | Advertencia o bloqueo |
| Mismo ítem repetido | Sobreexposición | Advertencia |
| Evaluación publicada editada | Descalce con respuestas | Requiere nueva versión |
| Docente cambia escala después de aplicar | Reclamos | Recalculo auditado |
| AI genera distractores ambiguos | Mala evaluación | Validación + revisión humana |
| Evaluación sin resultado de aprendizaje | Reporte pobre | Permitir, pero advertir |
| Fecha/duración inconsistentes | Mala aplicación | Validaciones blandas |

### 8.3 Aplicación e ingesta

| Edge case | Riesgo | Mitigación UX/Product |
|---|---|---|
| CSV con columnas incorrectas | Error de carga | Reporte por columna |
| CSV con estudiante duplicado | Doble calificación | Cola de resolución |
| Estudiante no asociado | Nota huérfana | Requiere asignación o exclusión |
| Respuestas incompletas | Nota incorrecta | Marcar como incompleta |
| Respuesta con alternativa inexistente | Error de pauta/plantilla | Rechazo de fila |
| QR no coincide con evaluación | Asociación incorrecta | Bloquear lote |
| Lote contiene respuestas de varias evaluaciones | Mezcla de datos | Separar por ID o rechazar |
| OCR baja confianza | Error silencioso | Cola de revisión |
| Foto borrosa | Respuesta inválida | Solicitar recaptura |
| Alumno ausente | Cálculo de promedio incorrecto | Estado ausente/excluido |
| Intento repetido | Doble nota | Política: reemplaza, conserva o bloquea |
| Entrega tardía | Decisión docente | Flag de tardanza |

### 8.4 Calificación y revisión

| Edge case | Riesgo | Mitigación UX/Product |
|---|---|---|
| Clave mal configurada | Notas masivamente erróneas | Prevalidación + recalculo auditado |
| Pregunta anulada después de aplicada | Reclamos | Acción “anular ítem” con recalculo |
| Más de una respuesta en selección única | Criterio incierto | Política visible: 0, parcial o revisión |
| Selección múltiple con crédito parcial | Inconsistencia | Configuración explícita de scoring |
| Nota publicada prematuramente | Daño de confianza | Confirmación antes de publicar |
| Cambio de nota manual | Falta de evidencia | Override con motivo obligatorio |
| Recalificación | Versionamiento de resultado | Guardar cálculo anterior y nuevo |
| Exportación expone datos sensibles | Riesgo privacidad | Export según rol y minimización |
| Coordinador ve datos individuales | Riesgo privacidad | Vista agregada por defecto |
| Reporte interpreta mal dificultad | Mala decisión pedagógica | Definir tooltip/documentación de métricas |

---

## 9. Requerimientos funcionales clave para backlog de diseño

### Epic A — Banco de preguntas objetivo

#### RF-A01 — Crear pregunta de alternativas
Como docente, quiero crear una pregunta con alternativas para reutilizarla en evaluaciones.

**Criterios:**
- Permite tipos: verdadero/falso, selección única, selección múltiple.
- Permite ingresar enunciado y alternativas.
- Permite marcar correcta(s).
- Valida cardinalidad por tipo.
- Guarda estado inicial: borrador o activa.

#### RF-A02 — Metadatos pedagógicos
Como docente, quiero clasificar preguntas por tema, dificultad y resultado para encontrarlas y reportarlas.

**Criterios:**
- Pregunta contiene dificultad.
- Pregunta contiene taxonomía mínima.
- Pregunta puede asociarse a resultado de aprendizaje.
- Los filtros usan estos metadatos.

#### RF-A03 — Versionar, clonar y retirar pregunta
Como docente/coordinador, quiero modificar preguntas sin perder historia.

**Criterios:**
- Editar pregunta usada genera nueva versión o advertencia fuerte.
- Clonar crea nueva pregunta editable.
- Retirar oculta de nuevas evaluaciones.
- Reactivar vuelve a habilitar.

#### RF-A04 — Generar pregunta con AI y aprobar
Como docente, quiero generar preguntas sugeridas desde un objetivo, pero aprobarlas antes de usarlas.

**Criterios:**
- AI genera pregunta en estado borrador.
- AI sugiere alternativas y correcta.
- Docente puede editar/aprobar/rechazar.
- Acción queda registrada.

---

### Epic B — Creación de evaluación

#### RF-B01 — Crear evaluación desde objetivo o banco
Como docente, quiero crear una evaluación partiendo de un objetivo, banco o combinación.

**Criterios:**
- Permite datos básicos: título, fecha, duración, curso/lista, escala.
- Permite agregar preguntas existentes.
- Permite agregar preguntas generadas.
- Evaluación inicia como borrador.

#### RF-B02 — Configurar puntajes y pauta
Como docente, quiero definir puntajes y revisar la clave antes de aplicar.

**Criterios:**
- Cada pregunta tiene puntaje.
- Se calcula total.
- Se valida que todas tengan clave.
- Se visualiza pauta completa antes de publicar.

#### RF-B03 — Definir escala de calificación
Como docente, quiero convertir puntaje a nota según mi criterio.

**Criterios:**
- Permite escala 1–7, 0–100 u otra.
- Permite umbral de aprobación.
- Muestra preview de conversión.
- Cambios quedan auditados.

#### RF-B04 — Publicar evaluación y congelar snapshot
Como docente, quiero publicar una evaluación sin riesgo de que cambios futuros alteren la prueba.

**Criterios:**
- Publicar crea snapshot de preguntas y alternativas.
- Bloquea edición estructural.
- Genera versión de evaluación.
- Permite crear nueva versión si se requieren cambios.

---

### Epic C — Aplicación

#### RF-C01 — Generar entregable aplicable
Como docente, quiero obtener un entregable para aplicar la evaluación.

**Criterios:**
- Genera formato digital o PDF.
- Incluye ID/QR.
- Permite descargar o compartir.
- El entregable referencia evaluación y versión.

#### RF-C02 — Gestionar participantes simples
Como docente, quiero asociar una lista de estudiantes/identificadores a la evaluación.

**Criterios:**
- Permite ingresar manualmente o importar lista.
- Cada participante tiene identificador único en la evaluación.
- No requiere cuenta de estudiante en MVP.
- Permite marcar ausente/excluido.

#### RF-C03 — Marcar evaluación como aplicada
Como docente, quiero marcar que la prueba fue aplicada para iniciar recepción de respuestas.

**Criterios:**
- Cambia estado a aplicada.
- Habilita ingesta.
- Registra fecha/hora.
- Bloquea cambios de estructura.

---

### Epic D — Ingesta y validación

#### RF-D01 — Recibir respuestas por formulario o CSV
Como docente, quiero cargar respuestas cerradas para calificación automática.

**Criterios:**
- Permite respuesta web o CSV.
- Valida formato.
- Valida cantidad de preguntas.
- Valida estudiante/identificador.
- Muestra errores claros.

#### RF-D02 — Cola de errores de ingesta
Como docente, quiero resolver errores antes de calificar.

**Criterios:**
- Lista errores por fila, estudiante, pregunta o archivo.
- Permite corregir, excluir o reintentar.
- Bloquea calificación si hay errores críticos.
- Guarda historial de intentos.

#### RF-D03 — Ingesta por captura/OCR
Como docente, quiero subir capturas de respuestas físicas cuando el flujo lo requiera.

**Prioridad sugerida:** P1, no P0, salvo que la demo dependa de papel.

**Criterios:**
- Captura/subida de imagen.
- Detección de baja calidad.
- OCR con confianza.
- Cola de revisión para baja confianza.

---

### Epic E — Calificación objetiva

#### RF-E01 — Calificar automáticamente respuestas cerradas
Como docente, quiero que el sistema calcule puntajes y notas.

**Criterios:**
- Compara respuestas con snapshot de clave.
- Calcula puntos por pregunta.
- Calcula total.
- Convierte a nota.
- Genera estado calificada.

#### RF-E02 — Revisión de excepciones
Como docente, quiero revisar solo casos problemáticos.

**Criterios:**
- Lista respuestas incompletas, duplicadas o inválidas.
- Lista preguntas anuladas o con conflicto.
- Permite resolver antes de publicar.
- Registra acciones.

#### RF-E03 — Ajustes y recálculo auditado
Como docente, quiero corregir errores de pauta sin perder trazabilidad.

**Criterios:**
- Permite anular pregunta.
- Permite corregir clave con motivo.
- Recalcula resultados.
- Conserva cálculo anterior.

---

### Epic F — Resultados y reportes

#### RF-F01 — Publicar resultados
Como docente, quiero publicar resultados cuando estén revisados.

**Criterios:**
- Separar estado calificada de publicada.
- Confirmación obligatoria.
- Publicación registra auditoría.
- Resultados quedan exportables.

#### RF-F02 — Reporte pedagógico objetivo
Como docente, quiero entender cómo resultó la evaluación.

**Criterios:**
- Promedio.
- Mediana.
- Distribución.
- Tasa de aprobación.
- Tasa de acierto por pregunta.
- Índice de dificultad.
- Cobertura por resultado.
- Preguntas críticas.

#### RF-F03 — Reforzamiento sugerido
Como docente, quiero recibir sugerencias de reforzamiento a partir de los resultados.

**Criterios:**
- Detecta temas con bajo desempeño.
- Sugiere acción de reforzamiento.
- Puede usar AI, pero queda como sugerencia.
- Docente puede editar.

---

### Epic G — Auditoría y evidencia

#### RF-G01 — Auditoría académica
Como administrador/docente, quiero saber quién hizo qué y cuándo.

**Criterios:**
- Registra creación, edición, publicación, ingesta, calificación, ajuste y exportación.
- Permite filtro por usuario, evaluación, fecha y acción.
- Exporta CSV/JSON.
- No editable por usuarios comunes.

#### RF-G02 — Logs AI y evidencia operacional
Como operador, quiero registrar ejecuciones AI cuando correspondan.

**Criterios:**
- Registra generación de preguntas, reportes o reforzamientos.
- Registra input/output resumido.
- Registra costo/modelo si aplica.
- Vincula aprobación docente.

---

## 10. Modelo conceptual recomendado para alternativas

```text
Organization
  └── UserAccount

QuestionBank
  ├── Question
  │   ├── QuestionVersion
  │   ├── QuestionOption
  │   ├── Difficulty
  │   ├── Topic
  │   └── LearningOutcome

Assessment
  ├── AssessmentQuestionSnapshot
  ├── AssessmentOptionSnapshot
  ├── ScoringPolicy
  ├── Deliverable
  ├── Participant
  └── AssessmentRun

AssessmentRun
  ├── ResponseBatch
  ├── StudentAttempt
  ├── StudentAnswer
  ├── SelectedOption
  ├── GradeResult
  ├── ReviewException
  └── PublicationEvent

Report
  ├── AssessmentReport
  ├── ItemAnalysis
  ├── OutcomeCoverage
  └── RecoverySuggestion

Audit
  ├── AcademicAuditEvent
  ├── ApprovalEvent
  └── AgentExecutionLog
```

### Entidades nuevas o adaptadas necesarias en GradeOps AI

| Entidad | Motivo |
|---|---|
| `Question` | Ítem objetivo reutilizable |
| `QuestionOption` | Alternativas y correctas |
| `QuestionVersion` | Control histórico |
| `QuestionTaxonomy` | Búsqueda y reportes |
| `AssessmentQuestionSnapshot` | Congelar evaluación publicada |
| `AssessmentOptionSnapshot` | Congelar alternativas y clave |
| `ScoringPolicy` | Escala, umbral, crédito parcial |
| `Participant` | Identidad mínima por evaluación |
| `StudentAttempt` | Rendición del participante |
| `StudentAnswer` | Respuesta por pregunta |
| `SelectedOption` | Alternativas elegidas |
| `ReviewException` | Cola de casos problemáticos |
| `ItemAnalysis` | Reporte de dificultad/aciertos |
| `AcademicAuditEvent` | Auditoría no ligada a AI |

---

## 11. Priorización sugerida

### P0 — Debe estar para el flujo de alternativas

- Crear pregunta con alternativas.
- Validar tipos TF/SC/MC.
- Banco simple con búsqueda/filtros.
- Crear evaluación desde banco o generación AI aprobada.
- Configurar puntajes y escala.
- Publicar evaluación con snapshot.
- Responder por formulario web o importar CSV.
- Calificación automática determinística.
- Revisión de excepciones.
- Publicar/exportar resultados.
- Reporte básico por evaluación e ítem.
- Auditoría académica mínima.

### P1 — Importante para producto vendible

- PDF con ID/QR.
- Plantilla CSV descargable.
- Lista liviana de participantes.
- Anulación de pregunta y recálculo.
- Reforzamiento sugerido por AI.
- Notificaciones de calificación lista/errores.
- Desempeño histórico del ítem.
- Clonar/versionar con UX más completa.

### P2 — Futuro

- OCR móvil robusto.
- Banco institucional compartido.
- Coordinador con vistas agregadas.
- Integraciones LMS/SIS.
- Múltiples versiones A/B.
- Analítica longitudinal.
- Proctoring.
- Marketplace de bancos.

---

## 12. Decisiones de diseño recomendadas

### Decisión 1 — Crear un modo explícito: “Evaluación con alternativas”

No mezclarlo con el flujo de programación/rúbrica. Debe ser un modo propio:

```text
Crear evaluación
  -> Con alternativas
  -> Práctica / código
```

Esto evita que el usuario se enfrente a conceptos innecesarios.

### Decisión 2 — Separar “pauta” de “rúbrica”

Para alternativas no corresponde usar rúbrica como concepto principal.

Usar:

- `Pauta` o `Clave de respuestas`.
- `Política de puntaje`.
- `Escala de nota`.

La rúbrica puede quedar para evaluaciones abiertas/prácticas.

### Decisión 3 — Usar AI como copiloto de construcción, no como juez final

En el flujo de alternativas:

- AI sugiere preguntas.
- AI revisa ambigüedad.
- AI sugiere distractores.
- AI explica resultados.
- AI sugiere reforzamiento.

Pero el cálculo objetivo debe ser determinístico.

### Decisión 4 — Mantener estudiantes sin cuenta en MVP

Adoptar la lógica de GradeOps AI actual:

```text
student_identifier dentro de la evaluación
```

No crear portal estudiante ni cuenta obligatoria en el MVP.

### Decisión 5 — Incorporar snapshot obligatorio

Es una de las mejores decisiones de GRADE.

Una evaluación aplicada nunca debe depender de la pregunta viva del banco.

### Decisión 6 — Diseñar la revisión desde excepciones

El docente no quiere revisar 40 pruebas una por una si son alternativas.

Quiere revisar:

- errores;
- ambigüedades;
- anulaciones;
- duplicados;
- cambios de pauta;
- estudiantes no asociados.

### Decisión 7 — Reporte objetivo + narrativa AI

El reporte debe tener datos duros primero y narrativa después.

Orden recomendado:

1. resultados numéricos;
2. desempeño por ítem;
3. cobertura;
4. hallazgos;
5. sugerencia de reforzamiento.

---

## 13. Backlog mínimo recomendado para diseño de pantallas

| ID | Pantalla / flujo | Prioridad |
|---|---|---|
| UX-01 | Workspace docente con estados de evaluación | P0 |
| UX-02 | Banco de preguntas: lista, filtros, acciones | P0 |
| UX-03 | Crear/editar pregunta con alternativas | P0 |
| UX-04 | Generar preguntas con AI y aprobar | P0 |
| UX-05 | Wizard crear evaluación | P0 |
| UX-06 | Seleccionar preguntas desde banco | P0 |
| UX-07 | Configurar pauta/puntaje/escala | P0 |
| UX-08 | Revisión previa a publicación | P0 |
| UX-09 | Generar aplicación web/CSV/PDF | P0/P1 |
| UX-10 | Cargar respuestas | P0 |
| UX-11 | Validación y errores de ingesta | P0 |
| UX-12 | Calificación automática y resumen | P0 |
| UX-13 | Cola de excepciones | P0 |
| UX-14 | Publicar/exportar resultados | P0 |
| UX-15 | Reporte por evaluación | P0 |
| UX-16 | Reporte por ítem | P0 |
| UX-17 | Auditoría académica | P1 |
| UX-18 | Evidencia AI/operación | P1 |
| UX-19 | Anular pregunta y recalcular | P1 |
| UX-20 | OCR/captura móvil | P2 |

---

## 14. Conclusión estratégica

Para evaluaciones con alternativas, GradeOps AI debe absorber el corazón funcional de GRADE: banco de preguntas, alternativas, pauta, snapshots, aplicación, ingesta, calificación objetiva y reportes por ítem.

Pero debe evitar absorber el peso institucional completo de GRADE: demasiados roles, integraciones complejas, administración profunda y OCR como dependencia inicial.

La dirección recomendada es:

```text
GradeOps AI — Objective Assessment Ops

Un flujo simple para que docentes creen, apliquen y revisen evaluaciones de alternativas,
con banco reutilizable, calificación automática confiable, revisión de excepciones,
reportes pedagógicos y evidencia operacional.
```

La propuesta más fuerte no es “hacer un generador de quizzes”.  
Es convertir la evaluación objetiva en una operación controlada:

```text
Generar
  -> Validar
  -> Aplicar
  -> Calificar
  -> Revisar excepciones
  -> Publicar
  -> Aprender del resultado
```

Ese es el puente correcto entre GRADE y GradeOps AI.

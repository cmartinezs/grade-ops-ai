# Lectura Estratégica De La Hackathon

> Nota raw formateada desde la primera conversación estratégica.

> Decisión posterior: este archivo conserva la exploración original. El nombre final de trabajo para el proyecto es **GradeOps AI**; **ClassOps AI** queda como alternativa histórica de naming, no como marca principal.

## Contexto Inicial

Voy a revisar las bases públicas de la hackathon, extraer criterios, restricciones, entregables y señales de evaluación. Con eso te propongo una idea vendible, no solo "interesante técnicamente", y la bajo a pitch, MVP y estrategia de demo.

Ya hay una señal crítica: no es una hackathon clásica de "demo bonita". La evaluación está diseñada como una mini startup: negocio real, usuarios reales, ingresos reales, operación con agentes y evidencia verificable.

## Tesis Principal

Esta hackathon no premia "la mejor app con IA". Premia el negocio real más convincente operado por IA.

La página lo deja bastante claro: el reto pide construir un negocio en 90 días, con clientes reales, ingresos reales, operación con IA y uso de al menos un producto de Google Cloud. Además, si el proyecto usa LLM, debe usar Gemini API en al menos una llamada del producto desplegado.

La fecha límite es **17 de agosto de 2026 a las 13:00 PDT**, y hoy, **8 de junio de 2026**, quedan aproximadamente **70 días**. La competencia ya muestra alrededor de **11.6k participantes**, por lo que no conviene competir con una idea genérica tipo "chatbot educativo", "copilot de finanzas" o "asistente para pymes".

Los criterios pesan igual, pero el desempate favorece primero **Business Viability**, luego **AI-Native Operations** y luego **Category Impact**. Eso significa que vender puede pesar más que tener la arquitectura más elegante.

## Recomendación Estratégica

Mi recomendación: no compitamos con una app. Compitamos con una operación.

La idea ganadora debería ser:

> Un negocio educativo operado por agentes de IA que ayuda a docentes, bootcamps, tutores e instituciones pequeñas a crear, vender, ejecutar y corregir evaluaciones prácticas con feedback automático.

### Nombre Tentativo

**ClassOps AI**

**Subtítulo en inglés:**

> AI agents that run assessment operations for real educators. From lesson goals to graded evidence, feedback, reports, and student recovery plans.

**Subtítulo en español:**

> Agentes de IA que operan el ciclo completo de evaluación para docentes reales: creación, publicación, corrección, feedback, reportes y reforzamiento.

## Por Qué Esta Idea Tiene Más Opciones

Tienes una ventaja injusta: eres docente, desarrollador senior, conoces Spring Boot, APIs, evaluación, PSeInt, programación inicial, testing, rúbricas, feedback académico y el dolor real del profesor.

Además, el problema es medible. En Chile, TALIS 2024 reporta que docentes de jornada completa dedican **4,9 horas semanales** a marcar/corregir trabajos y **4,9 horas semanales** a trabajo administrativo, este último por sobre el promedio OCDE de **3 horas**.

Ese dato es oro para el pitch:

> ClassOps AI devuelve horas docentes cada semana, sin reemplazar al profesor: automatiza la operación repetitiva de evaluación y deja al humano tomando decisiones pedagógicas.

## Producto

ClassOps AI es una plataforma SaaS para docentes y pequeños equipos educativos que permite:

- Cargar un objetivo de clase, temario o guía.
- Generar una evaluación práctica.
- Generar rúbrica y criterios.
- Publicar la actividad a estudiantes.
- Recibir respuestas o evidencias.
- Corregir automáticamente cuando sea posible.
- Sugerir feedback individual.
- Detectar errores comunes.
- Crear actividades de recuperación.
- Preparar reportes para el docente.
- Automatizar comunicación con estudiantes.
- Registrar logs de cada decisión de IA.

No es solo "crear pruebas con IA". Eso está saturado.

La diferencia es que el negocio opera con agentes.

## Categoría Principal

**Education & Human Potential**

También tiene borde con **Small Business Services**, porque ayuda a docentes independientes, tutores y academias pequeñas a operar como una microinstitución educativa. Pero yo entraría por **Education & Human Potential**, porque tu historia, el impacto y el acceso a usuarios reales calzan mejor.

## Narrativa Para Jueces

Los jueces no van a comprar "una app para hacer evaluaciones".

Van a comprar esta narrativa:

> Millones de docentes y tutores no necesitan otro LMS complejo. Necesitan una operación educativa automatizada que les permita enseñar, evaluar y acompañar sin ahogarse en corrección y administración. ClassOps AI convierte a un docente en una microacademia operada por agentes.

Ese ángulo vende mejor porque toca tres puntos de la hackathon:

- **Business Viability:** docentes, tutores y bootcamps pueden pagar rápido por ahorrar tiempo.
- **AI-Native Operations:** los agentes toman decisiones operativas reales: generar evaluación, clasificar evidencias, corregir, priorizar alumnos, enviar feedback, crear reportes.
- **Category Impact:** mejora acceso a feedback personalizado y reduce carga docente.

Los criterios oficiales piden negocio real, usuarios reales, revenue, agentes en producción y evidencia de logs, uso de API, dashboards y clientes.

## MVP Ganador

No hagamos un monstruo. El MVP debe ser vendible, demostrable y cobrable en menos de 70 días.

### MVP v1

**AI Assessment Ops for Programming Teachers**

### Nicho Inicial

Docentes de programación inicial, bootcamps, tutores de informática y profesores que trabajan con pseudocódigo, Python, JavaScript, Java o fundamentos de algoritmos.

### Por Qué Este Nicho

- Lo conoces.
- Puedes conseguir usuarios rápido.
- El dolor es real.
- Las evidencias son fáciles de evaluar.
- Permite demos visuales potentes.
- Tiene impacto claro en educación y empleabilidad.

## Flujo MVP

### 1. Docente Crea Una Actividad

El docente escribe:

> Quiero evaluar condicionales, ciclos y funciones en estudiantes de primer semestre. Duración 90 minutos. Nivel básico.

El agente genera:

- Enunciado.
- Objetivos de aprendizaje.
- Rúbrica.
- Criterios de evaluación.
- Casos de prueba.
- Pauta de corrección.
- Feedback esperado por tipo de error.

### 2. Estudiante Responde

Puede responder de tres formas en MVP:

- Texto/código pegado en plataforma.
- Archivo `.txt`, `.java`, `.py`, `.js`.
- Respuesta manual simple.

Evitaría OCR en la primera fase, salvo que sea muy necesario. OCR suma complejidad y riesgo.

### 3. Agentes Corrigen

| Agente | Responsabilidad |
| --- | --- |
| Assessment Designer Agent | Genera actividad y rúbrica. |
| Rubric Validator Agent | Revisa coherencia, dificultad y criterios. |
| Submission Analyzer Agent | Analiza respuesta/código del estudiante. |
| Feedback Agent | Genera feedback pedagógico accionable. |
| Risk Detector Agent | Detecta alumnos con bajo desempeño o patrones de error. |
| Teacher Copilot Agent | Resume resultados y recomienda siguiente clase. |
| Business Ops Agent | Registra uso, prepara reportes, genera evidencia para clientes y hackathon. |

Esto es clave: la hackathon quiere ver negocio operado por IA, no solo IA dentro de una funcionalidad.

### 4. Docente Revisa Y Aprueba

Para evitar problemas de confianza:

- La IA sugiere.
- El docente aprueba.
- Se registra que hizo la IA y que corrigió el humano.

Esto te permite decir:

> AI operates the assessment workflow, while teachers retain pedagogical authority.

### 5. Plataforma Genera Reporte

Reporte por actividad:

- Promedio.
- Distribución.
- Errores frecuentes.
- Objetivos menos logrados.
- Estudiantes en riesgo.
- Actividad de reforzamiento sugerida.
- Mensaje personalizado para cada estudiante.

## Arquitectura Recomendada Para Competir

Como la hackathon exige Google Cloud, no basta con "uso Gemini API desde cualquier hosting". Las reglas dicen que el proyecto debe usar al menos un producto de Google Cloud y que los proyectos con LLM deben usar Gemini API para al menos una llamada en la app desplegada.

Arquitectura simple y defendible:

| Capa | Tecnología |
| --- | --- |
| Frontend | Next.js o Angular |
| Backend | Spring Boot o Node/NestJS |
| Hosting backend | Google Cloud Run |
| IA | Gemini API / Vertex AI Gemini |
| Logs | Cloud Logging |
| Base de datos | Firestore, Cloud SQL PostgreSQL o Supabase si se justifica |
| Storage | Cloud Storage para archivos |
| Pagos | Stripe |
| Auth | Firebase Auth o KeyGo si quieres usar algo propio, pero con cuidado |
| Observabilidad | Cloud Logging + dashboard interno |
| Video demo | Mostrar agentes ejecutando decisiones reales |

Cloud Run calza bien porque Google lo define como una plataforma completamente administrada para ejecutar contenedores invocables por requests o eventos, sin gestionar infraestructura.

Para los agentes, Gemini permite function calling y salidas estructuradas, útil para que la IA no devuelva solo texto libre, sino JSON validable para rúbricas, feedback, notas, riesgos y acciones.

## Evidencia Necesaria Para Vender

La entrega pide varias evidencias:

- Repo.
- Video de menos de 3 minutos.
- Narrativa de 500 a 1000 palabras.
- Evidencia de ingresos.
- Gastos.
- Logs de ejecución de agentes.
- Uso de API.
- Dashboards.
- Evidencia de clientes reales.

Por eso el producto debe tener desde el día 1 evidencia interna obligatoria.

### Logs Por Ejecución De Agente

Cada ejecución de agente debe guardar:

- Fecha/hora.
- Usuario/docente.
- Actividad.
- Agente ejecutado.
- Input recibido.
- Decisión tomada.
- Output estructurado.
- Modelo usado.
- Tokens/costo aproximado.
- Estado: sugerido, aprobado, corregido, rechazado.
- Impacto: minutos estimados ahorrados.

Esto sirve para la app, pero también para ganar.

## Modelo De Negocio

Necesitamos revenue rápido, no una proyección bonita.

### Pricing Recomendado

| Plan | Precio | Incluye |
| --- | ---: | --- |
| Free | US$0 | 3 evaluaciones, 20 estudiantes, marca de agua, sin reportes avanzados. |
| Teacher Pro | US$9/mes | 20 evaluaciones, 150 estudiantes/mes, feedback automático, reportes básicos. |
| Cohort Pro | US$29/mes | 100 evaluaciones, 500 estudiantes/mes, reportes por curso, exportación PDF/CSV, soporte prioritario. |
| Pilot Pack | US$99 pago único | Setup, 3 evaluaciones reales y reporte ejecutivo para bootcamp, docente independiente o pequeña academia. |

Para la hackathon, el mejor producto no es necesariamente el más escalable todavía. Es el que puede generar evidencia de pago en 70 días.

Yo apuntaría a vender **10 a 20 Pilot Packs** o **30 suscripciones Teacher Pro**.

Con eso ya puedes mostrar:

- Ingresos reales.
- Clientes reales.
- Uso real.
- Testimonios.
- Costos controlados.
- CAC bajo o cero.
- Validación clara.

## Go-To-Market En 70 Dias

### Mercado Inicial

No iría primero por colegios grandes ni universidades completas. Ciclos de venta lentos.

Iría por:

- Docentes independientes.
- Profesores de programación.
- Tutores.
- Bootcamps pequeños.
- Academias online.
- Coordinadores académicos con poder de piloto pequeño.
- Comunidades de profesores de informática.

Tu ventaja está en la red docente y técnica. El pitch debe ser directo:

> Te ahorro horas corrigiendo y te entrego feedback personalizado para tus estudiantes sin cambiar tu forma de enseñar.

### Oferta Comercial Inicial

No vender "software".

Vender:

> Piloto de corrección y feedback automático para tu próxima evaluación.

Landing:

- Sube tu pauta.
- Creamos actividad + rúbrica.
- Tus estudiantes responden.
- Recibes corrección, feedback y reporte.
- Tú apruebas antes de entregar.

Call to action:

> Run my next assessment with AI

## Demo De 3 Minutos

La demo debe vender como startup, no como tutorial técnico.

### Guion

| Tiempo | Bloque | Mensaje / Acción |
| --- | --- | --- |
| 0:00-0:20 | Dolor | "Teachers spend hours every week grading, correcting and preparing feedback. In Chile, full-time teachers report 4.9 hours per week marking student work and 4.9 hours on administrative work." |
| 0:20-0:40 | Solución | "ClassOps AI is an AI-native assessment operations platform. It helps educators create, run, grade and improve learning activities with AI agents." |
| 0:40-1:30 | Demo producto | Mostrar: docente crea actividad, agente genera evaluación, agente genera rúbrica, estudiante entrega respuesta, agente corrige, docente aprueba, se genera feedback y dashboard muestra resultados. |
| 1:30-2:10 | AI-native operations | Mostrar pantalla de logs: Assessment Designer Agent, Rubric Validator Agent, Feedback Agent, Risk Detector Agent, Teacher Copilot Agent, cada uno con estado y decisión. |
| 2:10-2:40 | Business traction | Mostrar usuarios reales, pagos Stripe, testimonios, revenue, costos y uso de Gemini/Google Cloud. |
| 2:40-3:00 | Cierre | "ClassOps AI turns every educator into an AI-operated learning business. Teachers stay in control. Students get better feedback. Small education providers can scale." |

## Lo Que No Debemos Hacer

No hagamos esto:

- Un chatbot para estudiantes.
- Un generador genérico de quizzes.
- Un LMS completo.
- Una plataforma enorme tipo Moodle con IA.
- Una app que solo "usa Gemini".
- Un proyecto sin pagos.
- Un proyecto sin logs.
- Un proyecto donde el humano hace todo y la IA solo redacta texto.
- Un proyecto viejo reciclado sin aclarar.

Ojo con esto último: las reglas dicen que los proyectos deben ser creados después del inicio de la hackathon, el **19 de mayo de 2026**. Se pueden usar templates, frameworks o boilerplates preexistentes, pero se debe explicar cómo se usaron.

Por tanto, no conviene presentar un proyecto anterior como si fuera nuevo. Conviene construir ClassOps AI desde cero, aunque esté inspirado en tu experiencia docente y en dolores que ya conoces.

## Diferenciador Fuerte

La mayoría va a presentar:

> AI tutor for students.

Nosotros deberíamos presentar:

> AI operations layer for educators.

Eso es más fuerte porque:

- Tiene comprador claro.
- Tiene dolor económico.
- Tiene uso recurrente.
- Tiene evidencia de ahorro de tiempo.
- Puede generar revenue rápido.
- Puede expandirse a instituciones.
- Encaja con "AI agents operating a business".

## Roadmap De Ejecución

### Semana 1: Validación Y Preventa

**Objetivo:** conseguir 5-10 docentes interesados antes de construir demasiado.

**Entregables:**

- Landing simple.
- Formulario de waitlist.
- Oferta de piloto pagado.
- 10 entrevistas.
- 3 pagos o compromisos.
- Definición del nicho exacto.

### Semana 2-3: MVP Funcional

**Entregables:**

- Login docente.
- Crear actividad.
- Generar rúbrica con Gemini.
- Cargar respuestas.
- Corrección asistida.
- Feedback individual.
- Logs de agentes.
- Dashboard básico.

### Semana 4-5: Pilotos Reales

**Entregables:**

- 5 actividades reales.
- 100+ respuestas procesadas.
- Testimonios.
- Métrica de tiempo ahorrado.
- Ajuste de pricing.
- Export PDF/CSV.

### Semana 6-7: Producto Vendible

**Entregables:**

- Stripe.
- Planes.
- Onboarding automático.
- Mejor dashboard.
- Reportes por curso.
- Emails automáticos.
- Página pública con casos reales.

### Semana 8-10: Hackathon Packaging

**Entregables:**

- Video de 3 minutos.
- Narrativa 500-1000 palabras.
- Repo limpio.
- Testing instructions.
- Revenue evidence.
- Expense report.
- Agent logs.
- Customer evidence.

## Métricas Que Debemos Perseguir

Para vender la historia:

| Métrica | Meta razonable |
| --- | ---: |
| Docentes registrados | 30-50 |
| Clientes pagados | 10-20 |
| Estudiantes evaluados | 200-500 |
| Evaluaciones creadas | 30-80 |
| Feedbacks generados | 300-1000 |
| Revenue | US$300-US$2000 |
| Tiempo docente ahorrado | 100+ horas estimadas |
| Agentes ejecutados | 1000+ eventos logueados |

No necesitas millones de usuarios. Necesitas una señal real, verificable y creíble.

## Pitch Final

> ClassOps AI helps educators run assessment workflows with AI agents. Teachers describe what they want to evaluate, and our agents generate the activity, rubric, correction criteria, feedback, recovery plan and performance report. Students get faster, more personalized feedback. Teachers stay in control. Small education providers can operate like full academic teams. In 90 days, we are proving this with real teachers, real students, real usage and real revenue.

## Veredicto

La mejor apuesta para ti es **ClassOps AI**, no porque sea la idea "más grande", sino porque es la más ganable para tu perfil.

Tienes dominio del problema, acceso natural a usuarios, capacidad técnica para construirlo, historia personal creíble, categoría clara, impacto real y posibilidad de vender rápido.

La hackathon no se gana diciendo:

> Mira lo que construí.

Se gana diciendo:

> Mira el negocio que ya está operando con IA, mira quién pagó, mira los logs, mira el impacto y mira por qué esto puede crecer.

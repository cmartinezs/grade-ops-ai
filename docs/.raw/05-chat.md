# Herramientas Personales De IA Y Costos Productivos

> Nota raw formateada desde la conversación sobre cómo considerar las suscripciones personales de IA en el costo de GradeOps AI.

> Decisión posterior: este archivo conserva el razonamiento original. La fuente canónica separa `Development tooling` de `Production runtime` en `../cost-model.md`.

## Prompt Original

> Yo pago Google One con plan "Google AI Pro"; además cuento con suscripciones de Claude Code Pro, ChatGPT Plus con Codex y GitHub Copilot Pro porque soy docente. Además trabajo con OpenCode con modelos gratuitos. Todo esto como parte para desarrollo utilizando AI.

## Tesis Principal

Esto cambia el costo de desarrollo, pero no cambia el costo operativo del producto.

Tu stack de IA personal te da una ventaja enorme para construir **GradeOps AI** con bajo gasto inicial, pero para la hackathon hay que separar cuatro bolsas:

| Bolsa | Qué incluye | Afecta pricing SaaS |
| --- | --- | --- |
| Desarrollo asistido por IA | Claude Code, ChatGPT Plus/Codex, Copilot, OpenCode, Gemini/Antigravity. | Parcialmente. |
| Operación del producto | Gemini API, Cloud Run, DB, Storage, logs, emails. | Sí, directo. |
| Marketing/adquisición | Ads, landing, dominios comerciales, campañas, outreach pagado. | Sí, pero separado. |
| Costo hundido/personal | Herramientas que ya pagabas antes y usas para varias cosas. | No debería definir precio. |

Punto clave:

> Tus suscripciones ayudan a construir más rápido, pero no reemplazan la facturación real del producto.

Para producción, **GradeOps AI** debe usar API/Cloud facturable y trazable, no sesiones personales de herramientas de desarrollo.

## 1. Suscripciones Personales: Construcción, No Operación SaaS

Tu stack actual:

| Herramienta | Uso correcto en GradeOps AI | Clasificación |
| --- | --- | --- |
| Google AI Pro | Ideación, prototipos, Gemini en herramientas Google, Antigravity, AI Studio. | Desarrollo/R&D. |
| Claude Code Pro | Implementación, refactor, arquitectura, debugging. | Desarrollo/R&D. |
| ChatGPT Plus + Codex | Diseño funcional, revisión, generación de código, documentación. | Desarrollo/R&D. |
| GitHub Copilot Pro | Autocompletado, generación local, productividad IDE. | Desarrollo/R&D. |
| OpenCode + modelos gratuitos | Tareas de código de bajo costo, asistencia local/CLI. | Desarrollo/R&D. |

Lo importante:

> Ninguna de esas suscripciones debería ser usada como motor productivo de GradeOps AI.

El producto desplegado debe usar APIs/billing trazable:

- Gemini API o Vertex AI para cumplir la hackathon;
- opcionalmente otros proveedores vía API si más adelante conviene.

Notas de referencia:

- OpenAI declara que ChatGPT Plus cuesta US$20/mes y que el uso de API se factura por separado.
- Codex está incluido en planes elegibles de ChatGPT, con límites según plan.
- Anthropic separa sus planes de suscripción de la API/Console.
- Claude Pro cuesta US$20/mes.
- Claude Code puede usarse con planes Pro/Max, pero los usos intensivos o por API tienen otra lógica de consumo.
- GitHub permite Copilot Pro gratis para profesores verificados, con reevaluación mensual de elegibilidad.

## 2. Google AI Pro Ayuda, Pero No Elimina El Costo Productivo

Google AI Pro sirve mucho porque incluye:

- límites ampliados en AI Studio;
- Google Antigravity;
- Jules;
- beneficios del Google Developer Program.

Google One muestra que Google AI Pro incluye **US$10 mensuales en créditos de Google Cloud**, mientras planes superiores muestran montos mayores.

Además, la ayuda oficial de Google indica que Google AI Pro da acceso a:

- Google AI Studio;
- Antigravity con cuotas más altas;
- créditos/beneficios de Google Developer Program.

Las cuotas dependen del producto y disponibilidad.

### Lectura Práctica

Tu Google AI Pro puede bajar el gasto de prototipado y despliegue inicial, pero **GradeOps AI** debe tener su propio:

- proyecto Google Cloud;
- billing;
- logs;
- presupuestos;
- costos medidos.

No debemos decir:

> No tenemos costos porque usamos mi plan Google AI Pro.

Debemos decir:

> Usamos créditos y herramientas personales para acelerar desarrollo, pero el producto mide costos reales por ejecución de agente, tokens, hosting y operación.

Eso suena mucho más serio ante jueces.

## 3. Cómo Reportarlo Para La Hackathon

Criterio contable simple:

### A. Costos Operativos Del Producto

Estos sí van directo a **Total Costs**:

| Concepto | Reportar como |
| --- | --- |
| Gemini API / Vertex AI | AI API usage. |
| Cloud Run | Hosting/backend execution. |
| Firestore/Cloud SQL | Database. |
| Cloud Storage | File storage. |
| Cloud Logging/Monitoring | Observability/logging. |
| Dominio | Product/domain cost. |
| Email transaccional | Product communication. |
| Stripe/PayPal/MercadoPago fees | Payment processing. |
| Contractors, si existen | Contractor fees. |

### B. Suscripciones Personales De IA

Estas no son COGS puro, pero pueden reportarse como **allocated development tooling cost** si se facturaron durante el período y fueron usadas directamente para construir el proyecto.

| Suscripción | Cómo reportarla |
| --- | --- |
| Claude Code Pro | Allocated AI development tooling. |
| ChatGPT Plus/Codex | Allocated AI development tooling. |
| Google AI Pro | Pre-existing AI development tooling + Google Cloud credits. |
| GitHub Copilot Pro docente | Free verified teacher benefit, US$0 cash cost. |
| OpenCode modelos gratuitos | Free/open tooling, US$0 cash cost. |

### Estrategia Transparente

Reportar dos números:

- cash cost;
- allocated cost.

Ejemplo conservador:

> Total Costs: US$244.32. Includes US$184.32 product operating costs and US$60.00 allocated AI development tooling during the hackathon period. Marketing and customer acquisition spend: US$0.

Ejemplo separando tooling preexistente:

> Total Costs: US$184.32 cash cost. Includes Google Cloud/Gemini API usage, Cloud Run hosting, Firestore, domain, and payment fees.

> Additional pre-existing AI development tooling used: Claude Code Pro, ChatGPT Plus/Codex, Google AI Pro, GitHub Copilot Pro teacher benefit and OpenCode. These were used for development acceleration and are not included in product COGS.

Preferencia:

> Usar el enfoque conservador si queremos mostrar máxima rigurosidad. Los jueces valoran que entendamos los costos.

## 4. Efecto En El Costo De Desarrollo

Antes estimé el costo de desarrollo valorizando horas. Con tus suscripciones, la caja real baja mucho.

### Desarrollo Con Tus Herramientas Actuales

| Concepto | Costo incremental mensual probable |
| --- | ---: |
| Claude Code Pro | US$20 si lo asignamos completo |
| ChatGPT Plus/Codex | US$20 si lo asignamos completo |
| GitHub Copilot Pro | US$0 si estás con beneficio docente |
| OpenCode + modelos gratuitos | US$0 |
| Google AI Pro | Usar valor real de tu factura; además considera créditos incluidos |
| Google Cloud/Gemini testing | US$0-100 inicial |
| Dominio | US$10-30 anual aprox. |
| Email/landing/tools | US$0-30 mensual |
| Caja incremental fuerte | Baja |

Tu ventaja real no es solo pagar herramientas. Es que puedes operar como "mini equipo":

| Tarea | Herramienta principal |
| --- | --- |
| Arquitectura, decisiones, documentación | ChatGPT / Claude |
| Implementación fuerte | Claude Code / Codex |
| Autocompletado IDE | GitHub Copilot |
| Experimentación con Gemini | Google AI Pro / AI Studio / Antigravity |
| Bajo costo / alternativas | OpenCode |
| Revisión final y estrategia | ChatGPT + documentación propia |

Esto reduce el costo caja, pero no elimina el costo de horas.

Para la hackathon, lo podemos narrar como:

> The team used AI development tools to compress product development time, while the deployed product runs on Google Cloud and Gemini API with full operational cost tracking.

## 5. Esto No Debe Bajar Demasiado El Precio De Venta

No conviene razonar así:

> Como desarrollo barato con mis suscripciones, puedo cobrar barato.

El precio no debe salir del costo de construir. Debe salir de:

- valor ahorrado al docente;
- costo operativo por evaluación;
- soporte;
- margen;
- recuperación futura del desarrollo;
- percepción profesional del producto.

Tus suscripciones bajan el R&D cash burn, no el costo unitario de cada cliente.

El costo unitario sigue siendo:

```text
Costo por evaluación =
Gemini/API + hosting + storage + logs + emails + soporte + retries + margen de error
```

## 6. Pricing Ajustado Considerando La Ventaja

Mantendría precios en USD para la hackathon, aunque localmente puedas cobrar en CLP.

| Plan | Precio recomendado | Uso incluido | Motivo |
| --- | ---: | --- | --- |
| Free | US$0 | 1 evaluación / 30 entregas | Demo controlada. |
| Teacher Starter | US$12/mes | 3 evaluaciones / 90 entregas | Entrada fácil. |
| Teacher Pro | US$29/mes | 10 evaluaciones / 300 entregas | Plan principal. |
| Cohort Pro | US$79/mes | 30 evaluaciones / 1.000 entregas | Bootcamps/tutores. |
| Pilot Pack | US$49-99 único | 3 evaluaciones reales + reporte | Mejor para hackathon. |

Para Chile/LatAm:

| Plan | Precio CLP sugerido |
| --- | ---: |
| Teacher Starter | $9.990 CLP/mes |
| Teacher Pro | $19.990-$24.990 CLP/mes |
| Cohort Pro | $59.990-$79.990 CLP/mes |
| Pilot Pack | $39.990-$79.990 CLP único |

Para la hackathon, el **Pilot Pack** es el más importante. Necesitamos pagos reales más que MRR perfecto.

## 7. Archivo Recomendado Para El Repo

En `grade-ops-ai-docs`, agregaría:

```text
/business/cost-model.md
```

Estructura recomendada:

```markdown
# GradeOps AI — Cost Model

## 1. Product Operating Costs
- Gemini API / Vertex AI
- Google Cloud Run
- Firestore / Cloud SQL
- Cloud Storage
- Cloud Logging
- Email provider
- Payment processing fees

## 2. AI Development Tooling
The team used pre-existing AI subscriptions and free tools to accelerate development:
- Google AI Pro
- Claude Code Pro
- ChatGPT Plus with Codex
- GitHub Copilot Pro teacher benefit
- OpenCode with free models

These tools were used for software development, documentation, architecture and testing support. They are not part of the runtime product COGS.

## 3. Cost Reporting Method
We separate:
- Cash operating costs
- Allocated development tooling
- Marketing and customer acquisition spend
- Credits and free-tier benefits

## 4. Unit Economics
Primary cost unit:
- Cost per assessment
- Cost per graded submission
- Cost per active teacher
```

## 8. Riesgo Que Debemos Evitar

No mezclar herramientas personales con producción.

### Mala Práctica

> GradeOps AI usa mi sesión de ChatGPT/Claude/Gemini web para corregir entregas.

### Buena Práctica

> GradeOps AI usa Gemini API en backend desplegado, registra cada ejecución de agente, calcula tokens/costo estimado, guarda output estructurado y permite auditoría.

La segunda opción cumple mucho mejor con la hackathon y con un futuro SaaS real.

## 9. Decisión Estratégica

Tu stack actual permite:

| Sin tus herramientas | Con tus herramientas |
| --- | --- |
| Desarrollar lento. | Iterar rápido. |
| Contratar más apoyo. | Automatizar parte del trabajo. |
| Gastar más en prototipado. | Gastar poco en R&D. |
| Depender de una sola IA. | Comparar salidas entre modelos. |
| Menos documentación. | Generar documentación viva. |
| Más riesgo de bloqueo. | Más alternativas. |

Pero para pricing, la regla sigue siendo:

> Cobrar por valor y volumen de uso, no por lo barato que nos salió construir.

## Recomendación Final

Usa tus suscripciones como palanca de desarrollo, no como subsidio del negocio.

Para **GradeOps AI**, dejaría el modelo así:

| Área | Decisión |
| --- | --- |
| Development tooling | Mostly covered by pre-existing personal AI subscriptions. |
| Production runtime | Measured separately using Google Cloud + Gemini API billing. |
| Pricing | Based on assessments, graded submissions, reports and teacher value. |
| Hackathon reporting | Disclose operating costs, marketing spend, revenue, and optionally allocated AI development tooling. |

Principio interno:

> Las suscripciones personales aceleran la construcción; la API productiva demuestra el negocio.

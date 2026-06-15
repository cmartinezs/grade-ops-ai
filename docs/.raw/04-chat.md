# Costos, Créditos Y Pricing Para GradeOps AI

> Nota raw formateada desde la conversación sobre costos de desarrollo, operación y pricing para la hackathon.

> Decisión posterior: este archivo conserva la estimación original. La fuente canónica de pricing, unit economics y reporting financiero es `../cost-model.md`.

## Prompt Original

> Me queda una duda importantísima, sobre cómo costear este desarrollo. Como hay que utilizar servicios de Google y Gemini, eso no es gratis. ¿Qué dice el artículo original de la hackathon (<https://xprize.devpost.com>) al respecto? Por otra parte, estima detalladamente los costos de desarrollo y de operación para poder saber bien cuánto cobrar por las suscripciones.

## Enfoque De Respuesta

Voy a separar la respuesta en dos partes:

1. Qué dicen las bases originales sobre costos, créditos e ingresos.
2. Una estimación práctica de desarrollo, operación y precios para **GradeOps AI**.

Hallazgo concreto desde las bases:

> Sí hablan de costos, pero no como "te damos todo gratis". Piden reportar **Total Costs** y marketing por separado, lo que obliga a diseñar unit economics desde ahora.

## Conclusión Ejecutiva

No podemos tratar Google/Gemini como "gratis", aunque existan créditos y free tiers.

Para ganar la hackathon necesitamos demostrar negocio real, y eso incluye:

- costos reales;
- margen bruto;
- pricing defendible;
- evidencia financiera;
- costos operativos trazables.

## 1. Qué Dice El Artículo Oficial Sobre Costos

Las reglas oficiales de Devpost dicen tres cosas importantes:

1. Hay créditos gratuitos disponibles, pero no dice que todo sea gratis ni ilimitado. La regla indica que hay *free credits* accesibles en sitios relevantes para Google AI Ultra y Google Cloud.
2. Es obligatorio usar al menos un producto de Google Cloud, y si el proyecto usa LLM, debe hacer al menos una llamada a Gemini API en la aplicación desplegada.
3. La postulación exige reportar costos, no solo ingresos. Devpost pide declarar:
   - revenue total;
   - revenue por mes;
   - **Total Costs** excluyendo marketing;
   - **Marketing and Customer Acquisition Spend** por separado, incluso si el gasto de marketing fue cero.

La lectura correcta:

> La hackathon no te pide gastar mucho. Te pide demostrar que sabes cuánto cuesta operar tu negocio y que el modelo puede sostenerse.

Además, las reglas piden evidencia del producto corriendo:

- logs de agentes;
- registros de uso de API;
- screenshots de dashboards;
- evidencia que fortalezca el caso de que la operación está en producción.

## 2. Cuidado Con Los Créditos

No todos los créditos cubren Gemini API.

Google Cloud tiene una prueba gratuita de **US$300 por 90 días**, pero hay una advertencia crítica: la documentación oficial dice que ese crédito no puede usarse para pagar costos de Gemini API en AI Studio. Para Gemini API en AI Studio se debe considerar el free tier de Gemini o un plan de facturación/prepago propio de Gemini API.

Por otro lado, Google anunció créditos mensuales para suscriptores de Google AI Pro y Ultra:

- **US$10/mes** para AI Pro;
- **US$100/mes** para AI Ultra;
- integrados como beneficios de Google Developer Program.

Google indica que esos créditos pueden usarse para desplegar y escalar en:

- Cloud Run;
- Vertex AI;
- Gemini API vía AI Studio o Vertex AI, según disponibilidad y condiciones del beneficio.

### Recomendación Operativa

> No diseñar el negocio dependiendo de créditos.

Usarlos para reducir caja durante la hackathon, pero calcular precios como si tuviéramos que pagar todo.

## 3. Costos Base De Gemini Para GradeOps AI

Para el MVP, usaría **model routing**:

| Uso | Modelo recomendado | Motivo |
| --- | --- | --- |
| Generar evaluación | Gemini 3 Flash o 3.5 Flash | Mejor calidad pedagógica. |
| Generar rúbrica | Gemini 3 Flash o 3.5 Flash | Requiere consistencia. |
| Corregir entregas masivas | Gemini 3.1 Flash-Lite | Bajo costo por volumen. |
| Feedback individual | Gemini 3.1 Flash-Lite / Gemini 3 Flash | Mucho volumen. |
| Reporte docente | Gemini 3 Flash | Buena relación calidad/costo. |
| Casos complejos | Gemini 3.5 Flash | Solo fallback. |

Precios oficiales usados para estimar:

| Modelo | Input / 1M tokens | Output / 1M tokens |
| --- | ---: | ---: |
| Gemini 3.1 Flash-Lite | US$0.25 | US$1.50 |
| Gemini 3 Flash Preview | US$0.50 | US$3.00 |
| Gemini 3.5 Flash | US$1.50 | US$9.00 |

## 4. Estimación Por Evaluación Real

Unidad de negocio:

> 1 evaluación = creación de actividad + rúbrica + corrección de 30 estudiantes + feedback + reporte docente.

Supuesto técnico razonable por evaluación:

| Operación | Input tokens estimados | Output tokens estimados |
| --- | ---: | ---: |
| Crear actividad + rúbrica | 15.000 | 8.000 |
| Validar rúbrica | 10.000 | 2.000 |
| Corregir 30 entregas | 480.000 | 150.000 |
| Reporte final docente | 40.000 | 8.000 |
| **Total** | **545.000** | **168.000** |

Costo estimado por evaluación de 30 estudiantes:

| Modelo usado | Costo IA estimado | Con 25% de margen por retries/logs |
| --- | ---: | ---: |
| Gemini 3.1 Flash-Lite | US$0.39 | US$0.49 |
| Gemini 3 Flash Preview | US$0.78 | US$0.97 |
| Gemini 3.5 Flash | US$2.33 | US$2.91 |

Traducción directa:

- Con buen routing, una evaluación completa de 30 estudiantes debería costar entre **US$0.50 y US$1.20** en IA.
- Si usamos Gemini 3.5 Flash para todo, sube a cerca de **US$3** por evaluación.

El riesgo no está en una evaluación. El riesgo está en permitir uso ilimitado.

## 5. Costo Por Estudiante Corregido

Supuesto por estudiante:

> análisis + corrección + feedback.

| Modelo | Costo estimado por estudiante |
| --- | ---: |
| Gemini 3.1 Flash-Lite | US$0.014 |
| Gemini 3 Flash Preview | US$0.029 |
| Gemini 3.5 Flash | US$0.086 |

Esto nos dice algo importante:

> La suscripción debe limitar cantidad de entregas corregidas, no solo cantidad de evaluaciones.

Un plan que diga "evaluaciones ilimitadas" es peligroso.

Lo correcto es vender créditos:

- evaluaciones;
- estudiantes;
- correcciones incluidas;
- sobreuso por entrega adicional.

## 6. Costos De Google Cloud Para El MVP

Para un MVP liviano, Google Cloud puede salir barato si usamos:

- Cloud Run;
- Firestore;
- Cloud Storage;
- Cloud Logging;
- límites controlados.

### Cloud Run

Cloud Run cobra por recursos usados y tiene free tier mensual. En la tabla oficial, el free tier considera los primeros **240.000 vCPU-seconds** y **450.000 GiB-seconds** mensuales, dependiendo de región y configuración.

Para la hackathon, con tráfico bajo/medio:

| Concepto | Estimación mensual |
| --- | ---: |
| Cloud Run API backend | US$0-15 |
| Cloud Run agent worker | US$0-20 |
| Egress bajo | US$0-5 |
| **Subtotal Cloud Run** | **US$0-40/mes** |

### Firestore

Firestore tiene free quota:

- 1 GiB almacenado;
- 50.000 lecturas/día;
- 20.000 escrituras/día;
- 20.000 deletes/día;
- 10 GiB/mes de salida.

Después, por ejemplo, cobra US$0.03 por 100.000 reads y US$0.09 por 100.000 writes en la tabla mostrada para regiones listadas.

Para MVP:

| Concepto | Estimación mensual |
| --- | ---: |
| Firestore lecturas/escrituras | US$0-10 |
| Firestore storage | US$0-5 |
| **Subtotal Firestore** | **US$0-15/mes** |

### Cloud Storage

Cloud Storage tiene uso gratuito hasta ciertos límites, por ejemplo **5 GB-months** en Always Free. Para operaciones, Google lista costos como **US$0.005 por 1.000 operaciones Class A** en Standard Storage regional.

Para MVP sin OCR pesado:

| Concepto | Estimación mensual |
| --- | ---: |
| Archivos subidos por estudiantes | US$0-5 |
| Descargas/reportes | US$0-5 |
| **Subtotal Storage** | **US$0-10/mes** |

### Cloud Logging

Cloud Logging permite los primeros **50 GiB/proyecto/mes** sin cobro para logging storage; luego cobra **US$0.50/GiB**.

Para MVP:

| Concepto | Estimación mensual |
| --- | ---: |
| Logs técnicos | US$0 |
| Logs de agentes estructurados | Mejor guardarlos también en Firestore/DB |
| **Subtotal Logging** | **US$0-5/mes** |

### Artifact Registry Y Builds

Artifact Registry ofrece hasta **0.5 GB gratis** y luego **US$0.10/GB/mes**. Firebase también muestra Cloud Build con no-cost hasta **120 min/día** y luego **US$0.003/min**.

Para MVP:

| Concepto | Estimación mensual |
| --- | ---: |
| Artifact Registry | US$0-2 |
| Cloud Build | US$0-5 |
| **Subtotal DevOps** | **US$0-7/mes** |

## 7. Costo Mensual Total De Operación

### Escenario A: MVP Hackathon Controlado

Supuesto:

- 20 docentes registrados;
- 10 docentes activos;
- 50 evaluaciones reales/mes;
- 1.500 estudiantes corregidos/mes;
- model routing barato: Flash-Lite + Flash;
- sin OCR;
- sin búsqueda web en cada corrección.

| Categoría | Estimación mensual |
| --- | ---: |
| Gemini API | US$25-80 |
| Cloud Run | US$0-40 |
| Firestore | US$0-15 |
| Cloud Storage | US$0-10 |
| Cloud Logging | US$0-5 |
| Build/Artifact Registry | US$0-7 |
| Email/transaccional | US$0-15 |
| Dominio amortizado | US$1-3 |
| **Total operación mensual** | **US$26-175** |

Para la hackathon, presupuestaría **US$150-250/mes** aunque esperemos gastar menos. Eso evita sorpresas.

### Escenario B: Piloto Más Serio

Supuesto:

- 50 docentes;
- 25 docentes activos;
- 200 evaluaciones/mes;
- 6.000 estudiantes corregidos/mes;
- algunos casos usan Gemini 3.5 Flash como fallback.

| Categoría | Estimación mensual |
| --- | ---: |
| Gemini API | US$120-450 |
| Cloud Run | US$20-100 |
| Firestore | US$10-40 |
| Storage | US$5-30 |
| Logging/Monitoring | US$0-25 |
| Email | US$10-50 |
| **Total operación mensual** | **US$165-695** |

### Escenario C: SaaS Inicial Real

Supuesto:

- 200 docentes;
- 100 docentes activos;
- 1.000 evaluaciones/mes;
- 30.000 estudiantes corregidos/mes.

| Categoría | Estimación mensual |
| --- | ---: |
| Gemini API | US$600-2.500 |
| Cloud Run | US$100-400 |
| Firestore/DB | US$40-200 |
| Storage | US$20-100 |
| Logging/Monitoring | US$25-100 |
| Email | US$50-200 |
| Soporte operativo | US$300-1.500 |
| **Total operación mensual** | **US$1.135-5.000** |

En ese punto ya no basta un precio barato. Ahí necesitas planes por volumen.

## 8. Costos De Desarrollo

Hay dos formas de mirarlo.

### Caja Real Durante Hackathon

Si tú desarrollas gran parte y usas herramientas gratuitas/créditos:

| Concepto | Bajo | Realista | Alto |
| --- | ---: | ---: | ---: |
| Dominio | US$10 | US$20 | US$60 |
| Google Cloud/Gemini testing | US$30 | US$150 | US$500 |
| Hosting/observabilidad | US$0 | US$50 | US$150 |
| Email transaccional | US$0 | US$20 | US$80 |
| Diseño/assets | US$0 | US$50 | US$200 |
| Marketing mínimo | US$0 | US$150 | US$500 |
| Payment fees | Variable | Variable | Variable |
| **Total caja 70 días** | **US$40** | **US$440** | **US$1.490** |

Presupuesto recomendado para no sufrir:

> US$500-1.000 de caja para toda la hackathon, sin contar horas propias.

### Costo Real Si Valorizamos Horas De Desarrollo

Supuesto para MVP serio:

| Rol / actividad | Horas estimadas |
| --- | ---: |
| Product discovery + negocio | 30-60 |
| UX/UI + landing | 40-80 |
| Frontend app | 100-180 |
| Backend/API | 120-220 |
| Agentes Gemini | 100-180 |
| Logs/dashboard/evidencia | 40-80 |
| Deploy/DevOps | 30-60 |
| QA/pilotos/ajustes | 60-120 |
| Video/demo/submission | 30-50 |
| **Total** | **550-1.030 h** |

| Valor hora | Costo equivalente |
| --- | ---: |
| US$20/h | US$11.000-20.600 |
| US$40/h | US$22.000-41.200 |

Esto no significa que tengas que cobrar eso de inmediato. Significa que el pricing debe construir un negocio capaz de recuperar desarrollo en el tiempo.

## 9. Modelo De Precios Recomendado

No partiría con planes demasiado baratos. Para ganar la hackathon necesitamos revenue real, pero también demostrar margen.

| Plan | Precio | Incluye | Costo variable estimado | Margen bruto aprox. |
| --- | ---: | --- | ---: | ---: |
| Free | US$0 | 1 evaluación, 30 entregas, marca de agua | US$0.50-1.50 | Negativo/controlado |
| Teacher Lite | US$12/mes | 3 evaluaciones, 90 entregas | US$2-5 | 55-75% |
| Teacher Pro | US$29/mes | 10 evaluaciones, 300 entregas | US$8-18 | 60-75% |
| Cohort Pro | US$79/mes | 30 evaluaciones, 1.000 entregas | US$25-60 | 55-70% |
| Pilot Pack | US$99 único | 3 evaluaciones reales, hasta 150 entregas, onboarding | US$5-15 + soporte | 50-85% |

El **Pilot Pack de US$99** es el más importante para la hackathon. Es fácil de entender, vendible y genera evidencia rápida:

> Te operamos tus próximas 3 evaluaciones prácticas con IA: diseño, rúbrica, corrección, feedback y reporte.

Para suscripciones, la unidad comercial debe ser:

> entregas corregidas por mes, no "uso ilimitado".

## 10. Costos De Pago

Si usamos Stripe, la tarifa online estándar publicada es **2.9% + US$0.30** por transacción para tarjetas domésticas en EE. UU.; pueden aplicarse recargos por tarjetas internacionales o conversión de moneda.

| Venta | Fee aproximado Stripe | Neto aproximado |
| --- | ---: | ---: |
| US$12 | US$0.65 | US$11.35 |
| US$29 | US$1.14 | US$27.86 |
| US$79 | US$2.59 | US$76.41 |
| US$99 | US$3.17 | US$95.83 |

Para Chile, habría que decidir si usar Stripe con cuenta habilitada, PayPal, MercadoPago, Flow o Transbank.

Para la hackathon, lo más importante es que la evidencia financiera sea clara y exportable en USD.

## 11. Regla De Oro Para No Perder Plata

No vender:

> Correcciones ilimitadas con IA.

Vender:

> Incluye hasta X entregas corregidas por mes. Entregas adicionales: US$0.05-0.15 cada una.

Precio recomendado por sobreuso:

| Tipo de uso extra | Precio sugerido |
| --- | ---: |
| Entrega adicional corregida | US$0.08 |
| Evaluación adicional sin correcciones | US$0.50 |
| Evaluación adicional con hasta 30 entregas | US$3.00 |
| Reporte avanzado adicional | US$1.00 |
| Revisión premium con modelo avanzado | US$0.20-0.50 por entrega |

Esto deja margen incluso si algunos casos escalan a Gemini 3.5 Flash.

## 12. Qué Deberíamos Mostrar En La Postulación

En la app necesitamos un dashboard interno de costos desde el día 1:

| Métrica | Por qué importa |
| --- | --- |
| Tokens input por agente | Control de costo. |
| Tokens output por agente | Control de costo. |
| Modelo usado | Justifica routing. |
| Costo estimado por ejecución | Unit economics. |
| Costo por evaluación | Pricing. |
| Costo por docente | Rentabilidad. |
| Costo por estudiante corregido | Margen. |
| Revenue por cliente | Business viability. |
| Marketing spend | Requisito Devpost. |
| Costos cubiertos por créditos | Transparencia. |
| Costos efectivamente pagados | Evidencia financiera. |

Devpost puede pedir documentación financiera adicional, revenue records, expense statements o prueba de relación con usuarios/clientes, así que esto no debería quedar para el final.

## Recomendación Final

Pricing inicial:

| Oferta | Precio |
| --- | ---: |
| Free | US$0 |
| Teacher Lite | US$12/mes |
| Teacher Pro | US$29/mes |
| Cohort Pro | US$79/mes |
| Pilot Pack hackathon | US$99 pago único |

Política técnica:

> Gemini 3.1 Flash-Lite para volumen, Gemini 3 Flash para generación/reportes, Gemini 3.5 Flash solo para fallback o tareas premium.

Con eso, el negocio puede tener buen margen, el costo de operación es defendible y la historia para la hackathon queda sólida:

> No solo generamos revenue. Sabemos cuánto cuesta cada evaluación, cada corrección y cada agente. GradeOps AI es un negocio medible, no una demo.

# 00 — Principios rectores

## 1. La arquitectura debe proteger el negocio

GradeOps AI no debe organizarse alrededor de Spring, JPA, Gemini, PostgreSQL o Firebase. Debe organizarse alrededor de sus capacidades de negocio:

- Evaluaciones.
- Rúbricas.
- Entregas de estudiantes.
- Corrección.
- Feedback.
- Brechas de aprendizaje.
- Actividades de recuperación.
- Reportes docentes.
- Créditos, consumo y billing.
- Auditoría de decisiones AI.

Las tecnologías son detalles reemplazables. Las reglas del negocio no.

## 2. El dominio no depende de frameworks

En `domain` está prohibido depender de:

- Spring (`@Service`, `@Component`, `@Autowired`, `ApplicationEventPublisher`).
- JPA/Hibernate (`@Entity`, `@Table`, `@Column`, `@ManyToOne`).
- Jackson (`@JsonProperty`, `@JsonFormat`).
- Bean Validation (`@NotNull`, `@Size`, `@Valid`).
- HTTP, REST, servlet, controller, request, response.
- SDKs externos: Gemini, OpenAI, Stripe, Firebase, Google Cloud, etc.

El dominio debe poder ejecutarse en tests unitarios puros sin levantar Spring.

## 3. La aplicación coordina; el dominio decide

Un caso de uso puede:

- Validar input a nivel de aplicación.
- Cargar aggregates desde puertos.
- Ejecutar métodos del dominio.
- Invocar políticas, servicios de dominio o steps.
- Persistir usando puertos.
- Publicar eventos usando puertos.
- Preparar resultados para la capa de entrada.

Un caso de uso no debe:

- Contener reglas de negocio profundas que deberían vivir en el aggregate.
- Manipular entidades JPA directamente.
- Retornar DTOs REST.
- Conocer detalles de HTTP.
- Formatear respuestas de API.

## 4. La infraestructura adapta; no decide el negocio

Un adapter puede:

- Convertir DTOs externos a comandos internos.
- Implementar puertos de persistencia, mensajería, IA, email o storage.
- Mapear entidades JPA hacia objetos de dominio.
- Implementar retries, timeouts y circuit breakers.
- Registrar logs técnicos.

Un adapter no debe:

- Decidir reglas pedagógicas.
- Calcular notas con lógica compleja embebida.
- Generar decisiones AI sin pasar por un caso de uso.
- Saltarse aggregates para modificar estado.

## 5. El código debe ser explícito antes que “mágico”

Preferir:

```java
Assessment assessment = assessmentFactory.createFrom(command);
assessment.approveRubric(rubricId, reviewerId);
assessmentRepository.save(assessment);
```

Evitar:

```java
mapper.updateEntityFromDto(dto, entity);
repository.save(entity);
```

La segunda opción puede parecer corta, pero suele ocultar invariantes, permisos, eventos y cambios de estado relevantes.

## 6. KISS primero, extensibilidad después

No crear abstracciones por especulación. Crear abstracciones cuando exista una presión real:

- Dos implementaciones reales.
- Una regla de negocio que cambia por tenant/plan/contexto.
- Un proveedor externo intercambiable.
- Un flujo con steps reutilizables.
- Una feature suficientemente grande para requerir orquestación.

## 7. DRY no significa “unificar todo”

No duplicar reglas de negocio, algoritmos ni contratos. Pero está permitido duplicar estructuras simples cuando evitar la duplicación produciría acoplamiento artificial.

Ejemplo aceptable:

- `CreateAssessmentRequest` y `CreateAssessmentCommand` pueden tener campos parecidos.
- Son objetos de capas distintas y con responsabilidades distintas.

Ejemplo no aceptable:

- Dos formas distintas de calcular el puntaje final de una entrega.

## 8. Cada cambio debe ser trazable

GradeOps AI compite y opera con evidencia: decisiones de IA, costos, eventos, uso y aprobación humana. Por eso una feature crítica debe dejar trazabilidad:

- Quién ejecutó la acción.
- Sobre qué recurso.
- Qué agente o caso de uso participó.
- Qué modelo/proveedor AI se usó si aplica.
- Qué input estructurado recibió.
- Qué output estructurado generó.
- Qué decisión tomó el humano.
- Qué costo estimado tuvo.

## 9. El humano conserva autoridad pedagógica

El software puede sugerir, automatizar y priorizar. En flujos de evaluación, el diseño debe permitir aprobación, corrección o rechazo humano cuando la decisión afecte evaluación académica, feedback o reporte relevante.

## 10. Una feature no está terminada sin pruebas

Toda feature debe incluir al menos:

- Tests unitarios de dominio si agrega reglas.
- Tests de aplicación si agrega caso de uso.
- Tests de adapter si integra infraestructura.
- Tests de controller si expone API.
- Tests de seguridad si toca permisos, tenant, autenticación o datos sensibles.

# 04 — SOLID, DRY y KISS

## SOLID aplicado a GradeOps AI

### S — Single Responsibility Principle

Cada clase debe tener una razón principal para cambiar.

Buenas separaciones:

- Controller cambia por contrato HTTP.
- Command cambia por input del caso de uso.
- UseCase cambia por flujo de aplicación.
- Aggregate cambia por reglas de negocio.
- PersistenceAdapter cambia por persistencia.
- GeminiAdapter cambia por proveedor AI.
- Mapper cambia por transformación de datos.

Señales de violación:

- Un controller calcula notas.
- Un adapter de Gemini consume créditos.
- Un mapper valida reglas de rúbrica.
- Un JPA entity decide estados del negocio.
- Un service hace HTTP, persistencia, scoring, logging y eventos.

### O — Open/Closed Principle

El código debe permitir extensión sin modificar reglas estables.

Usar cuando hay variación real:

- Estrategias de corrección por tipo de evaluación.
- Proveedores AI.
- Políticas de consumo de créditos por plan.
- Exportadores PDF/CSV.
- Reglas de scoring por rúbrica.

Ejemplo:

```java
public interface GradingStrategy {

  boolean supports(AssessmentType type);

  GradingResult grade(Submission submission, Rubric rubric);
}
```

No aplicar OCP creando interfaces para todo “por si acaso”.

### L — Liskov Substitution Principle

Una implementación debe cumplir el contrato sin sorpresas.

Si `CreditWalletPort.consume()` promete fallar con `InsufficientCreditsException`, ninguna implementación debe:

- Retornar `null`.
- Consumir parcialmente y no avisar.
- Ocultar errores críticos.
- Cambiar semántica según proveedor.

### I — Interface Segregation Principle

Preferir puertos pequeños y específicos.

Preferir:

```java
public interface AssessmentRepositoryPort {
  Assessment save(Assessment assessment);
  Optional<Assessment> findById(AssessmentId id);
}

public interface AssessmentSearchPort {
  Page<AssessmentSummary> search(AssessmentSearchCriteria criteria);
}
```

Evitar:

```java
public interface AssessmentPort {
  Assessment save(...);
  Optional<Assessment> findById(...);
  Page<AssessmentSummary> search(...);
  void sendEmail(...);
  void callGemini(...);
  void exportPdf(...);
}
```

### D — Dependency Inversion Principle

La aplicación depende de abstracciones, no de detalles.

Correcto:

```java
private final AssessmentRepositoryPort assessmentRepository;
private final AssessmentGenerationPort assessmentGenerationPort;
```

Incorrecto:

```java
private final AssessmentJpaRepository assessmentJpaRepository;
private final GeminiClient geminiClient;
```

## DRY práctico

### Duplicación prohibida

No duplicar:

- Cálculo de puntajes.
- Validación de créditos.
- Reglas de transición de estado.
- Parsing de respuesta estructurada AI.
- Autorización por tenant.
- Mapeo de errores HTTP comunes.
- Correlation ID y MDC.

### Duplicación tolerada

Se puede tolerar duplicación temporal o estructural en:

- DTOs vs Commands.
- Responses públicas vs Results internos.
- Builders de test.
- Mensajes de validación específicos por endpoint.

### Regla de abstracción

Antes de extraer código común, preguntar:

1. ¿La duplicación representa la misma regla de negocio?
2. ¿Cambiarán por las mismas razones?
3. ¿La abstracción mejora la lectura?
4. ¿La abstracción no acopla capas distintas?

Si la respuesta no es clara, mantener simple.

## KISS práctico

### Preferir simple cuando

- La feature es nueva y no validada.
- Solo existe una implementación.
- El cambio esperado es bajo.
- El código cabe en una clase pequeña y testeable.

### Introducir complejidad cuando

- Hay una segunda implementación real.
- El flujo ya tiene demasiadas ramas.
- Hay requerimientos de auditoría por paso.
- Hay reintentos, compensaciones o asincronía.
- Hay variación por plan, tenant o proveedor.

## Reglas de tamaño

| Elemento | Recomendado | Máximo tolerado |
|---|---:|---:|
| Método de dominio | 5–30 líneas | 60 líneas |
| Método de aplicación | 10–40 líneas | 80 líneas |
| Controller method | 5–25 líneas | 40 líneas |
| Clase de dominio | 50–250 líneas | 400 líneas |
| Handler simple | 20–80 líneas | 120 líneas |
| Orquestador | 30–150 líneas | 250 líneas |
| Parámetros por método | 0–4 | 8 |

Si se supera el máximo, justificar o refactorizar.

## Code smells frecuentes

- `*Service` con más de 500 líneas.
- Métodos con boolean flags: `process(input, true, false)`.
- `Map<String, Object>` como modelo de dominio.
- Uso de `String` para estados, IDs y dinero.
- `Optional` como parámetro o campo.
- `null` como flujo de negocio.
- `catch (Exception e)` sin acción clara.
- Logs con datos sensibles.
- DTOs retornados por repositories.
- Entidades JPA expuestas en API.

## Regla final

> Primero hacerlo claro. Luego hacerlo reusable. Luego hacerlo genérico, solo si el negocio lo exige.

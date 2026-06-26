# 15 — De KeyGo Server a GradeOps AI: prácticas conservadas y mejoras

## Prácticas que se conservan

GradeOps AI conserva estas decisiones inspiradas en KeyGo Server:

- Java moderno como baseline.
- Arquitectura hexagonal / ports and adapters.
- Separación fuerte entre dominio, aplicación, API, infraestructura y bootstrap.
- Dominio sin Spring, JPA, HTTP ni SDKs externos.
- Casos de uso explícitos con comandos, resultados y puertos.
- Controllers como adaptadores de entrada.
- Persistencia aislada detrás de adapters.
- Entidades JPA separadas del dominio.
- Migraciones versionadas para cambios de schema.
- Validación y errores consistentes.
- Seguridad y autorización tratadas como parte central, no accesorio.
- Logs con contexto y limpieza de MDC.
- Tests de regresión cuando se toca seguridad, filtros, tenants o contratos públicos.
- Documentación pensada también para desarrollo asistido por IA.

## Mejoras para GradeOps AI

### 1. DDD táctico más explícito

KeyGo ya separa capas. GradeOps AI agrega reglas más explícitas para:

- `AggregateRoot`.
- `ValueObject`.
- `DomainEvent`.
- `DomainService`.
- `Policy`.
- `Specification`.

Motivo: GradeOps AI tendrá reglas de evaluación, rúbricas, scoring, créditos, feedback y auditoría AI que necesitan modelado rico.

### 2. Use case pattern escalable

Se define una escala por complejidad:

| Tamaño | Patrón |
|---|---|
| Simple | `UseCase` + `Handler` + `Command` + `Result` |
| Medio | Handler + `Orchestrator` |
| Grande | Orchestrator + `Step` pipeline + Context |

Esto evita tanto services gigantes como arquitectura innecesaria.

### 3. Package por artifact y feature

Estándar:

```text
<base-package>.<artifact>.<feature>.<layer>
```

Ejemplo:

```text
ai.gradeops.api.assessment.application.usecase
ai.gradeops.agents.grading.infrastructure.adapter.out.gemini
```

Esto permite crecer desde monolito modular hacia servicios separados sin rehacer nombres.

### 4. Lombok más restrictivo

Reglas reforzadas:

- `@Data` prohibido en `@Entity`.
- `@Data` prohibido en aggregates y dominio rico.
- `@Getter` permitido en dominio para lectura.
- `@Setter` restringido a JPA/DTO/config.
- `@RequiredArgsConstructor` para inyección.
- `@Builder` para commands/results/contextos, con cuidado en dominio.
- `@UtilityClass` solo para utilidades puras, no reglas de negocio.
- `@SneakyThrows` prohibido.

### 5. AI Ops como primera clase

GradeOps AI requiere auditoría de agentes:

- Modelo usado.
- Prompt version.
- Input/output hash.
- Tokens.
- Créditos.
- Estado.
- Duración.
- Aprobación humana.

Esto se incorpora como estándar, no como feature posterior.

### 6. Seguridad específica para datos educacionales y AI

Se agregan reglas para:

- No loguear submissions completas.
- No loguear prompts crudos.
- Sanitizar/hachear payloads AI.
- Validar outputs estructurados.
- Manejar prompt injection.
- Mantener autoridad humana en evaluación y feedback.

### 7. ArchUnit obligatorio

Se recomienda proteger la arquitectura con tests automáticos:

- Domain sin Spring/JPA.
- Controllers sin repositories.
- API sin entidades JPA.
- Application sin adapters concretos.

### 8. Read models explícitos

Para dashboards y reportes, GradeOps AI puede usar read models optimizados sin contaminar aggregates.

Esto será importante para:

- Promedios.
- Distribuciones.
- Errores frecuentes.
- Estudiantes en riesgo.
- Costos y consumo.
- Métricas de negocio.

## Decisión final

KeyGo aporta una base sólida de arquitectura y disciplina. GradeOps AI debe mantener esa base, pero elevar el estándar en tres áreas:

1. Modelado de dominio.
2. Orquestación de casos de uso y agentes.
3. Auditoría, seguridad y operación AI-native.

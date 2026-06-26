# 13 — Guía para desarrollo asistido por IA

## Objetivo

Esta guía define cómo deben trabajar Copilot, Claude Code, Codex, Gemini u otros agentes al modificar GradeOps AI.

## Regla cero

Antes de escribir código, el agente debe identificar:

1. Artifact afectado.
2. Feature afectada.
3. Capa afectada.
4. Caso de uso o endpoint afectado.
5. Reglas de dominio involucradas.
6. Tests que deben agregarse o actualizarse.

## Orden de trabajo recomendado

1. Leer documentación de arquitectura.
2. Identificar feature package.
3. Revisar patrones existentes en la misma feature.
4. Diseñar command/result/use case.
5. Implementar dominio si hay reglas nuevas.
6. Implementar aplicación.
7. Implementar adapters.
8. Implementar API.
9. Agregar migración si cambia persistencia.
10. Agregar tests.
11. Ejecutar quality gates.
12. Actualizar documentación si cambia contrato o arquitectura.

## Prompt base para agentes

```text
Actúa como desarrollador backend senior Java/Spring Boot para GradeOps AI.
Respeta arquitectura hexagonal, DDD táctico y package structure:
<base-package>.<artifact>.<feature>.<layer>.
No pongas Spring/JPA/Jackson en domain.
No expongas entidades JPA en API.
Usa use cases explícitos con Command/Result.
Usa puertos para persistencia, AI, mail, storage y proveedores externos.
Usa Lombok de forma restringida: @Slf4j, @RequiredArgsConstructor, @Builder, @Getter/@Setter en JPA; nunca @Data en @Entity.
Agrega tests unitarios de dominio/aplicación y tests de adapter/API si corresponde.
No registres datos sensibles, prompts crudos ni respuestas completas de estudiantes en logs.
```

## Checklist para una nueva feature

El agente debe crear o modificar:

```text
feature/domain/model
feature/domain/event
feature/application/port/in
feature/application/port/out
feature/application/command
feature/application/result
feature/application/usecase
feature/infrastructure/adapter/in/web
feature/infrastructure/adapter/out/persistence
feature/infrastructure/adapter/out/<external-provider>
```

solo cuando cada carpeta sea necesaria.

## Restricciones fuertes

El agente no debe:

- Crear `service` genéricos tipo `AssessmentService` sin rol claro.
- Inyectar JPA repositories en controllers.
- Inyectar SDKs externos en use cases.
- Agregar `@Entity` en domain.
- Agregar `@Data` en entidades.
- Usar `System.out.println`.
- Crear endpoints sin tests.
- Modificar schema sin migración.
- Agregar lógica de negocio en mappers.
- Inventar carpetas o convenciones nuevas si ya existe una convención.
- Guardar secretos en archivos versionados.

## Cómo pedir cambios a la IA

Buen prompt:

```text
Implementa el caso de uso GenerateAssessment en grade-ops-ai-api.
Feature: assessment.
Capas esperadas: domain, application, infrastructure.adapter.in.web, infrastructure.adapter.out.ai.
Debe usar GenerateAssessmentCommand/Result, GenerateAssessmentUseCase y GenerateAssessmentHandler.
El proveedor AI debe entrar por AssessmentGenerationPort.
No uses Gemini SDK fuera del adapter.
Agrega tests de handler con fake port y test de controller para request válido e inválido.
```

Mal prompt:

```text
Haz el endpoint para generar evaluación con Gemini.
```

## Revisión de output generado por IA

Revisar siempre:

- Imports de capas prohibidas.
- Entidades JPA filtrándose a API.
- DTOs usados como dominio.
- Falta de tests.
- Falta de migración.
- Manejo de errores genérico.
- Logs inseguros.
- Uso excesivo de Lombok.
- Abstracciones innecesarias.
- Nombres fuera de estándar.

## Reglas para refactors con IA

Pedir refactors pequeños y verificables.

Orden recomendado:

1. Extraer value object.
2. Mover regla al aggregate.
3. Extraer port.
4. Crear adapter.
5. Crear tests de arquitectura.
6. Eliminar dependencia indebida.

Evitar prompts que pidan reescribir media aplicación de una sola vez.

## Output esperado de un agente en PR

Un PR generado con IA debe incluir descripción:

```text
## What changed
- Added GenerateAssessmentUseCase.
- Added Assessment aggregate factory for generated assessments.
- Added GeminiAssessmentGenerationAdapter behind AssessmentGenerationPort.
- Added controller endpoint POST /api/v1/assessments/generate.

## Architecture
- Domain remains free of Spring/JPA.
- AI provider is isolated in infrastructure adapter.
- Controller maps request to command.

## Tests
- GenerateAssessmentHandlerTest.
- AssessmentControllerTest.
- GeminiAssessmentGenerationAdapterTest with mocked client.

## Risks
- Provider timeout behavior requires production tuning.
```

## Regla final

La IA puede acelerar escritura, pero no debe decidir silenciosamente arquitectura, seguridad ni reglas del negocio.

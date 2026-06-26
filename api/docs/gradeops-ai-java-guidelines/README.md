# GradeOps AI — Reglas de codificación y buenas prácticas Java

Este paquete define el estándar base para construir servicios Java/Spring Boot de GradeOps AI con arquitectura hexagonal, DDD táctico y criterios estrictos de mantenibilidad.

La guía toma como punto de partida las prácticas usadas en KeyGo Server y las ajusta para GradeOps AI, especialmente en estos puntos:

- Organización por `package base + artifact + feature + capa`.
- Dominio más explícito con `AggregateRoot`, `ValueObject`, `DomainEvent` y reglas de invariantes.
- Casos de uso con tres niveles de complejidad: caso simple, orquestador y pipeline de pasos.
- Uso controlado de Lombok, evitando `@Data` en entidades JPA y en objetos de dominio ricos.
- Aplicación práctica de SOLID, DRY, KISS y patrones GoF.
- Reglas de testing, observabilidad, seguridad y desarrollo asistido por IA.

## Audiencia

Esta documentación está escrita para:

- Desarrolladores backend Java de GradeOps AI.
- Agentes AI de codificación usados en el proyecto.
- Revisores técnicos de Pull Requests.
- Futuras personas que se incorporen al equipo.

## Estructura de archivos

| Archivo | Propósito |
|---|---|
| `00-principios-rectores.md` | Principios generales de ingeniería para GradeOps AI. |
| `01-arquitectura-hexagonal-y-paquetes.md` | Arquitectura, dependencias, capas y estructura de paquetes. |
| `02-ddd-tactico.md` | Aggregates, Value Objects, Domain Events, Domain Services y repositorios. |
| `03-use-cases-orquestadores-y-pasos.md` | Patrón de casos de uso según tamaño de la feature. |
| `04-solid-dry-kiss.md` | Reglas prácticas para aplicar SOLID, DRY y KISS sin sobreingeniería. |
| `05-patrones-gof.md` | Cuándo usar y cuándo evitar patrones de diseño GoF. |
| `06-nomenclatura-java.md` | Convenciones de nombres para clases, métodos, variables, paquetes y tests. |
| `07-lombok.md` | Uso permitido, recomendado y prohibido de Lombok. |
| `08-persistencia-jpa.md` | Entidades, repositorios, mappers, migraciones y transacciones. |
| `09-api-rest-dtos-validacion.md` | Controladores, DTOs, validación, errores y OpenAPI. |
| `10-testing-calidad-y-automatizacion.md` | Pirámide de pruebas, ArchUnit, Testcontainers y quality gates. |
| `11-seguridad-observabilidad-y-auditoria.md` | Seguridad, logs, MDC, auditoría, datos sensibles y AI ops. |
| `12-excepciones-y-manejo-de-errores.md` | Jerarquía de excepciones por capa, GlobalExceptionHandler y anti-patterns. |
| `13-guia-ai-assisted-development.md` | Reglas para Copilot, Claude Code, Codex u otros agentes de desarrollo. |
| `14-checklists.md` | Checklist operativo para nuevas features y Pull Requests. |
| `15-keygo-a-gradeops-mejoras.md` | Qué se conserva de KeyGo y qué se mejora en GradeOps AI. |

## Regla principal

> El dominio no conoce frameworks. La aplicación orquesta. La infraestructura adapta. La API traduce. El `run` o bootstrap cablea.

## Baseline recomendado

- Java 21.
- Spring Boot 3.x/4.x según decisión del repositorio.
- Maven o Gradle con reglas de calidad reproducibles.
- Arquitectura hexagonal / ports and adapters.
- DDD táctico en features con reglas de negocio reales.
- Tests automatizados como requisito de merge.

## Glosario rápido

| Término | Definición |
|---|---|
| Artifact | Segmento técnico o repositorio ejecutable: `api`, `agents`, `worker`, `billing`, etc. |
| Feature | Capacidad funcional del negocio: `assessment`, `rubric`, `submission`, `grading`, `feedback`, `billing`, etc. |
| Domain | Núcleo de negocio sin Spring, JPA, HTTP ni proveedores externos. |
| Application | Capa de casos de uso, comandos, resultados y puertos. |
| Infrastructure | Adaptadores técnicos de entrada/salida: REST, JPA, Gemini, Mail, Rabbit, Storage, etc. |
| Adapter In | Entrada al sistema: controller REST, consumer de eventos, scheduler, CLI. |
| Adapter Out | Salida del sistema: base de datos, proveedor IA, correo, storage, API externa. |

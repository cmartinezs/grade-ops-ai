# GradeOps AI — Plan transversal de testing

## Propósito

Este paquete define la estrategia de pruebas para `web/`, `api/`, `agents/`, la ejecución local con Docker Compose y la validación de los ambientes `beta` y `demo`.

Está escrito como insumo para un agente de planificación. Debe convertir la estrategia en entregas incrementales, historias, tareas, dependencias, criterios de aceptación y cambios de infraestructura/CI, sin confundir el estado actual con la arquitectura propuesta.

## Baseline revisado

- Repositorio: `cmartinezs/grade-ops-ai`
- Rama: `develop`
- Commit: `aa2dc4e427b409d5f8f7de91c1809bcd66c36450`
- Fecha de revisión: 21 de julio de 2026
- Artefactos: `web/`, `api/`, `agents/`, `.github/` e `infra/`

## Índice

1. [Resumen ejecutivo](01-resumen-ejecutivo.md)
2. [Estado actual y brechas](02-estado-actual-y-brechas.md)
3. [Modelo objetivo y pirámide](03-modelo-objetivo-y-piramide.md)
4. [Unitarias, componentes y coverage](04-unitarias-componentes-y-coverage.md)
5. [Aceptación aislada por artefacto](05-aceptacion-aislada-por-artefacto.md)
6. [Contratos y simuladores](06-contratos-y-simuladores.md)
7. [Integración automatizada con Compose](07-integracion-automatizada-compose.md)
8. [Ejecución funcional local real](08-funcional-local-real.md)
9. [Estrategia para beta y demo](09-beta-y-demo.md)
10. [CI/CD y promoción](10-ci-cd-y-promocion.md)
11. [Artefactos, reportes y dashboard futuro](11-artefactos-reportes-dashboard.md)
12. [Datos, seguridad y costos](12-datos-seguridad-y-costos.md)
13. [Roadmap incremental](13-roadmap-incremental.md)
14. [Criterios de aceptación y decisiones](14-criterios-y-decisiones.md)
15. [SonarQube, JMeter y quality gates](15-sonarqube-jmeter-quality-gates.md)

## Instrucciones para el agente planificador

1. Mantener separados los niveles de prueba: unitario, aceptación aislada, contrato, integración, funcional local y post-deployment.
2. No permitir dependencias externas reales en suites herméticas.
3. Toda fase debe dejar comandos reproducibles, timeouts, healthchecks, limpieza y artefactos publicados.
4. Añadir tareas en `.github/` e `infra/` cuando cambien CI, secretos, ambientes o recursos.
5. No fijar umbrales globales arbitrarios: establecer baseline medido y elevarlo gradualmente, con mínimos estrictos para código nuevo.
6. Tratar los contratos API/Agents como artefactos versionados y verificables.
7. No usar credenciales de `beta`, `demo` ni producción en el Compose funcional local.
8. Cada release debe indicar qué gate bloquea PR, promoción a `beta` y promoción a `demo`.
9. SonarQube complementa, pero no sustituye, unitarias, coverage, SAST ni revisión humana.
10. Las pruebas de carga deben declarar presupuesto, duración, concurrencia, ambiente autorizado y criterio de abortado; nunca apuntar accidentalmente a `beta` o `demo`.

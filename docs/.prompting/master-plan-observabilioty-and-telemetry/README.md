# Plan transversal de observabilidad y telemetría — GradeOps AI

## Propósito

Este paquete entrega al agente planificador un baseline verificable y una arquitectura objetivo para instrumentar `web/`, `api/`, `agents/` e `infra/`. No es un backlog cerrado: debe convertirse en releases incrementales con dependencias, criterios de aceptación y pruebas.

## Baseline

| Campo | Valor |
|---|---|
| Repositorio | `cmartinezs/grade-ops-ai` |
| Rama | `develop` |
| Commit | `aa2dc4e` |
| Fecha de revisión | 2026-07-21 |
| Alcance | `web/`, `api/`, `agents/`, `infra/` y contratos transversales |

## Regla de interpretación

- **Estado actual**: comprobado directamente en código o configuración.
- **Objetivo**: arquitectura recomendada; todavía no implementada.
- **Decisión pendiente**: requiere ADR, medición o elección de producto.

## Índice

1. [Resumen ejecutivo](01-resumen-ejecutivo.md)
2. [Estado actual y evidencia](02-estado-actual-y-evidencia.md)
3. [Modelo objetivo](03-modelo-objetivo-observabilidad.md)
4. [Contratos y correlación](04-contratos-correlacion-contexto.md)
5. [Logging estructurado](05-logging-estructurado.md)
6. [Métricas, SLI, SLO y alertas](06-metricas-sli-slo-alertas.md)
7. [Trazas distribuidas](07-trazas-distribuidas.md)
8. [Telemetría de producto e IA](08-telemetria-producto-ia.md)
9. [Artefactos y dashboard futuro](09-artefactos-dashboard.md)
10. [Privacidad, seguridad, retención y costo](10-gobierno-telemetria.md)
11. [Pruebas y operación](11-pruebas-operacion.md)
12. [Estrategia incremental y decisiones](12-estrategia-incremental.md)
13. [Topología multiambiente y adaptadores](13-topologia-multiambiente.md)

## Instrucciones para el planificador

1. No convertir “observabilidad” en una sola tarea de logging.
2. Mantener separadas telemetría técnica, evidencia durable de negocio y artefactos de diagnóstico.
3. Toda fase debe contemplar explícitamente ambos destinos: `demo` en GCP y `beta` en Vercel + Render + Neon.
4. Mantener un contrato portable de instrumentación; los servicios concretos son adaptadores por ambiente.
5. No diseñar el dashboard antes de estabilizar eventos, dimensiones, calidad y retención.
6. Instrumentar primero los journeys críticos y la cadena `web → api → agents → proveedor`.
7. Aplicar redacción, cardinalidad y presupuesto desde la primera fase.

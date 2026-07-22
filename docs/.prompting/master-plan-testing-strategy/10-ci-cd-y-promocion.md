# CI/CD y promoción

## Pipeline de pull request

1. Detectar paths afectados.
2. Lint/type-check/build.
3. Unitarias/componentes por artefacto.
4. Coverage y reporte.
5. Análisis SonarQube/SonarCloud e importación de coverage.
6. Contract checks de consumidores/proveedores afectados.
7. Build de imágenes.
8. Aceptación aislada del artefacto.
9. Integración Compose necesaria según matriz de impacto.
10. Smoke de rendimiento breve cuando cambien rutas críticas; la carga completa queda fuera del PR.
11. Security/dependency/container scanning como gate complementario.
12. Publicar artefactos aun ante fallos.

Cambios de contrato o `testkit/` deben disparar las tres suites transversales. Cambios solo documentales no deben levantar contenedores.

## Nightly

- full-chain completo;
- escenarios de resiliencia/timeouts;
- matriz de versiones/proveedores simulados;
- detección de flakiness;
- JMeter sobre Compose con baseline controlado y comparación de p95/p99, throughput y errores;
- evaluación GenAI controlada solo si hay presupuesto y credenciales dedicadas.

## Post-deployment

- Beta: smoke automático y rollback/mark-failed si no pasa.
- Demo: promoción aprobada, smoke y verificación de digest.
- No desplegar desde workflows que solo ejecutan `test`; separar build, attest y promote.

## Gates mínimos

| Gate | PR | Beta | Demo |
|---|---:|---:|---:|
| Unit/component | Sí | Heredado | Heredado |
| Coverage/no regression | Sí | Heredado | Heredado |
| Sonar quality gate sobre código nuevo | Sí | Heredado | Heredado |
| Acceptance aislada | Sí | Heredado | Heredado |
| Contract | Sí | Heredado | Heredado |
| Integración según impacto | Sí | Sí | Heredado |
| Smoke desplegado | No | Sí | Sí |
| GenAI real smoke | No | Acotado | Acotado |
| JMeter smoke | Según impacto | Sí | Sí, muy acotado |
| JMeter carga/soak | No | Programado/autorizado | Excepcional y autorizado |
| Aprobación humana | No | Opcional | Sí |

## Fiabilidad

- Prohibir retries silenciosos de una suite completa.
- Un test flaky se etiqueta, registra dueño y fecha límite; no se ignora indefinidamente.
- Medir duración p50/p95, tasa de fallo y tasa de retry por test.
- Cachear dependencias, no resultados de pruebas.

# Criterios de aceptación y decisiones

## Criterios globales

1. `web/`, `api/` y `agents/` ejecutan unitarias y generan coverage en formatos humano/máquina.
2. Cada imagen pasa aceptación black-box con todos sus externos simulados.
3. Existen contratos versionados para Web–API, API–Agents y Agents–GenAI structured output.
4. Compose ejecuta automáticamente `api-agents`, `web-api` y `full-chain`.
5. Firebase y GenAI nunca se invocan desde las suites herméticas.
6. El Compose funcional real exige credenciales/proyectos exclusivos y rechaza los de beta/demo.
7. Beta y demo tienen smoke propio y ambientes completamente aislados.
8. La imagen promovida conserva el mismo digest validado.
9. Todo fallo publica diagnóstico suficiente antes del teardown.
10. Los artefactos respetan un schema versionado y pueden ingerirse en un dashboard futuro.
11. Ningún reporte contiene secretos, tokens o PII.
12. Los tests tienen timeout, owner y política explícita de flakiness.
13. Los tres artefactos publican análisis Sonar y el código nuevo cumple el quality gate acordado.
14. Los escenarios JMeter generan JTL, reporte HTML y resumen normalizado, sin credenciales ni PII.
15. Ninguna prueba de carga puede ejecutar contra Beta o Demo sin allowlist explícita del host y autorización del pipeline.

## Decisiones recomendadas

| Tema | Recomendación |
|---|---|
| E2E navegador | Playwright |
| HTTP stubs | WireMock o MockServer, uno transversal |
| DB en integración API | PostgreSQL Testcontainers/Compose + Flyway |
| Coverage Web | Jest LCOV/Cobertura |
| Coverage Java | JaCoCo XML/HTML |
| Unit vs integration Maven | Surefire/Failsafe |
| Contratos | OpenAPI + JSON Schema; Pact solo si aporta valor adicional |
| Compose | Archivos componibles + perfiles, no copias completas |
| GenAI | Mock determinista para gates; smoke/eval real separado |
| Dashboard | Ingerir summaries normalizados, no consultar artefactos crudos |
| Calidad estática | SonarQube local; backend compartido en CI por decidir entre SonarQube y SonarCloud |
| Rendimiento HTTP | JMeter non-GUI; Lighthouse/Playwright para experiencia Web |

## Decisiones pendientes que no deben bloquear el diseño

1. Herramienta exacta para acceptance HTTP Java: REST Assured, Karate o Newman.
2. WireMock versus MockServer.
3. Backend definitivo para almacenar tendencias del dashboard.
4. Umbrales iniciales después de medir coverage real.
5. Retención final según costo y exigencias de evidencia.
6. Estrategia exacta para previews de Vercel con backend aislado.
7. Frecuencia y presupuesto de `ai-eval` real.
8. SonarQube autogestionado versus SonarCloud para CI centralizado.
9. Baselines y umbrales JMeter por endpoint después de medir capacidad real.

## Riesgos

- Una única suite E2E gigante será lenta y frágil.
- Mocks no validados pueden divergir del proveedor.
- H2 puede dar falsos positivos respecto de PostgreSQL.
- Coverage alto puede coexistir con mala calidad de aserciones.
- LLM real como gate exacto introduce flakiness y costo.
- Compartir Firebase/GenAI entre ambientes destruye el aislamiento.
- Guardar artefactos sin schema impide construir tendencias confiables.
- Convertir cobertura o Sonar en una meta numérica aislada incentiva tests y refactors de poco valor.
- Ejecutar carga contra servicios serverless sin límites puede disparar autoscaling, consumo GenAI y costos.

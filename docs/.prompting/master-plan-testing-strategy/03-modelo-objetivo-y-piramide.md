# Modelo objetivo y pirámide

## Principios

- **Hermeticidad:** un test automático debe declarar y controlar todas sus dependencias.
- **Determinismo:** misma entrada, mismo resultado; los modelos reales no sirven como oráculo exacto.
- **Frontera real:** aceptación prueba el artefacto por HTTP/navegador, no mediante llamadas internas.
- **Fidelidad selectiva:** PostgreSQL real donde importa SQL/Flyway; mocks donde importa aislamiento.
- **Velocidad:** PR rápido; suites amplias por paths afectados, integración transversal y nightly.
- **Trazabilidad:** cada fallo debe identificar commit, suite, escenario, servicio e imagen.

## Distribución recomendada

La mayor cantidad de pruebas debe residir en unitarias/componentes. Aceptación y contratos cubren todas las capacidades públicas críticas. Integración cubre pocos journeys de alto valor. Las llamadas reales a GenAI deben limitarse a smoke/evaluaciones controladas, nunca intentar reemplazar las pruebas deterministas.

## Matriz de responsabilidad

| Defecto | Capa primaria |
|---|---|
| Regla de dominio incorrecta | Unitario API |
| Render/validación UI | Componente Web |
| Prompt o parser inválido | Unitario/fixture Agents |
| Endpoint no cumple OpenAPI | Aceptación/contrato API |
| API invoca mal Agents | Contrato + integración `api-agents` |
| Web consume DTO obsoleto | Contrato + integración `web-api` |
| Configuración entre tres servicios | Integración full-chain |
| Credencial o CORS del ambiente | Smoke post-deployment |
| Calidad semántica de GenAI | Evaluación separada con dataset dorado |

## Taxonomía obligatoria

Los nombres y tags deben distinguir como mínimo:

```text
unit | component | integration | acceptance | contract | e2e | smoke | ai-eval | manual
```

No etiquetar como “unitario” un test que inicia Spring, abre puertos, usa Docker o depende de red.

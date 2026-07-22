# Artefactos, reportes y dashboard futuro

## Artefactos por ejecución

| Artefacto | Formato canónico | Productor |
|---|---|---|
| Resultados test | JUnit XML | Jest/Playwright/Maven adapters |
| Cobertura Web | LCOV + Cobertura XML + HTML | Jest |
| Cobertura API/Agents | JaCoCo XML + HTML | Maven |
| Calidad estática | Sonar task report + quality gate JSON | Sonar scanner/API |
| Rendimiento HTTP | JTL + dashboard HTML + summary JSON | JMeter |
| E2E/aceptación Web | Playwright HTML + trace.zip | Playwright |
| Contratos | JSON/XML + diff | OpenAPI/schema checker |
| Logs | JSONL/text por servicio | Docker Compose |
| Estado contenedores | JSON/text | Docker inspect/compose ps |
| Evidencia | screenshots/video solo al fallar | Playwright |
| Metadata | `run-manifest.json` | Orquestador |
| Resumen | `summary.json` + Markdown | Agregador |

## Manifiesto mínimo

```json
{
  "schemaVersion": "1.0",
  "runId": "...",
  "commitSha": "...",
  "branch": "...",
  "environment": "ci",
  "suite": "integration-full-chain",
  "startedAt": "...",
  "finishedAt": "...",
  "status": "passed",
  "imageDigests": {},
  "contractVersions": {},
  "artifactIndex": []
}
```

Nunca incluir secretos, tokens, prompts completos ni PII.

## Retención sugerida

- PR exitoso: 14–30 días.
- PR fallido: 60–90 días.
- Beta/demo release: 6–12 meses.
- Tendencias agregadas: largo plazo.
- Traces/videos pesados: solo en fallos o sampling.

## Dashboard futuro

El dashboard no debe leer HTML ni recorrer buckets en tiempo real. Diseñar un proceso de ingestión que convierta `summary.json`, JUnit y coverage en un modelo consultable:

```text
test_run → test_suite → test_case_result
         → coverage_snapshot
         → quality_gate_snapshot
         → performance_run → performance_metric
         → artifact_reference
         → deployment_validation
```

Métricas útiles:

- pass rate y duración por artefacto/suite;
- cobertura total y diff coverage;
- estado del quality gate, bugs, vulnerabilities, security hotspots, smells y duplicación;
- p50/p95/p99, throughput, error rate y bytes por escenario de rendimiento;
- flaky rate y top fallos;
- tiempo hasta detección y reparación;
- contract breaks;
- calidad/costo/latencia de `ai-eval` separados del testing determinista;
- estado de promoción por commit/digest.

Para comenzar, GitHub Actions artifacts + summaries son suficientes. El contrato de artefactos evita quedar amarrado al dashboard o backend definitivo.

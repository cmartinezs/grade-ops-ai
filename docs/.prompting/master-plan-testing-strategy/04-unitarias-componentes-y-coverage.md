# Unitarias, componentes y coverage

## Web

Implementar scripts separados:

```text
test:unit
test:unit:coverage
test:component
test:ci
```

- Mantener Jest/Testing Library para lógica, hooks, componentes y páginas.
- No probar detalles internos; validar comportamiento accesible.
- Añadir `collectCoverageFrom`, exclusiones justificadas y reporters `text`, `html`, `lcov`, `cobertura`.
- Publicar `coverage/lcov.info`, `coverage/cobertura-coverage.xml` y HTML.
- Complementar con type-check y lint; no contarlos como coverage.

## API

- Mantener JUnit/Mockito para dominio, application y adapters.
- Usar slices web/seguridad cuando no sea necesario iniciar todo el contexto.
- Mantener ArchUnit como gate arquitectónico.
- Preferir PostgreSQL Testcontainers + Flyway para repositorios y migraciones; reducir H2 a casos donde su semántica no importe.
- Configurar JaCoCo con reporte XML/HTML y `check` en `verify`.
- Separar Maven Surefire (unitarias) y Failsafe (integración, `*IT`).

## Agents

- Cubrir validación de comandos, selección de proveedor/modelo, templates, envelopes, parsers, schema validation, retry/fallback y sanitización de errores.
- Usar fixtures/golden files versionados para respuestas válidas, inválidas y adversariales.
- Verificar que prompts no expongan secretos ni permitan que contenido no confiable cambie instrucciones de sistema.
- Configurar JaCoCo, Surefire/Failsafe y tags equivalentes a API.
- Separar completamente `ai-eval` de unit/integration.

## Política de cobertura

Coverage es una señal, no el objetivo. Estrategia recomendada:

1. Medir baseline por artefacto.
2. Impedir que disminuya sin excepción documentada.
3. Exigir cobertura más alta sobre líneas nuevas/modificadas.
4. Aplicar mínimos por capa crítica, no únicamente un porcentaje global.
5. Excluir configuración/DTOs solo con justificación.

Meta inicial sugerida, sujeta al baseline: 80% de líneas nuevas y 70% de branches nuevas; dominio y seguridad deben aspirar a valores superiores. No bloquear el primer PR con un umbral histórico imposible.

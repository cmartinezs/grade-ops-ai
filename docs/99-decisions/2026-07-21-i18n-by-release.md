# ADR - i18n por release funcional

## Estado

Aceptada.

## Contexto

GradeOps AI mezcla actualmente superficies en ingles y espanol. Hasta ahora esa deuda podia verse como editorial o limitada a labels de UI, pero el alcance real es mayor: afecta contratos Web-API, catalogos, errores seguros, preferencias de usuario, salidas GenAI, contenido para docentes/estudiantes, emails, reports, exports y pruebas.

Al mismo tiempo, el sistema necesita mantener estabilidad tecnica: el codigo fuente, DTO fields, database columns, enum codes, error codes, logs, metricas, traces y eventos operativos deben permanecer en ingles para evitar drift y facilitar mantenimiento.

## Decision

i18n se implementa dentro de cada release funcional, no como una release tecnica separada.

- El codigo fuente se mantiene en ingles.
- Los contratos tecnicos usan field names y codes en ingles.
- Los valores visibles para usuario final pueden estar localizados o representarse como `code` + `label` localizado.
- `web/` no hardcodea texto user-facing en componentes, hooks o schemas.
- `api/` resuelve locale, valida allowlist, aplica fallback y separa `errorCode` tecnico de `safeMessage` user-facing.
- `agents/` recibe `outputLocale`/`contentLocale` cuando genera texto visible para docentes o estudiantes.
- El locale efectivo se captura en operaciones asincronas y artefactos generados cuando el idioma del resultado importa.
- Observabilidad, telemetria y logs permanecen en ingles; locale solo aparece como atributo normalizado y de baja cardinalidad.
- Las pruebas deben cubrir translation keys, fallback, contratos de locale, salida GenAI simulada en idioma esperado y que logs/traces/metrics no se localicen.

## Consecuencias

- R01 debe tratar i18n como fundacion minima: locale efectivo, UI strings de dashboard/intake/draft, catalog labels, errores seguros y `outputLocale` para generation/regeneration.
- Las futuras salidas student-facing o teacher-facing no deben depender de idioma implicito del prompt ni del navegador.
- `language` de programacion debe separarse de locale natural; si el contrato actual usa `language`, las tareas deben desambiguar cuando aplique.
- La documentacion puede seguir el idioma de cada archivo hasta una normalizacion editorial posterior, pero runtime/product i18n queda gobernado por esta decision.
- Logs, metricas, traces, audit event names y error codes no se traducen aunque la UI este en otro idioma.

## Fuentes

- `docs/master-plan/analysis/i18n-strategy.md`
- `docs/source-docs-refresh/audit-report.md`
- `docs/source-docs-refresh/validation-report.md`
- `docs/09-developer-guide/07-web-development.md`
- `docs/10-best-practices/02-arquitectura-y-diseno.md`

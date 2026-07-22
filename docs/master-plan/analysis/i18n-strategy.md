# Estrategia transversal de i18n - GradeOps AI

> Incorporacion de i18n al Master Plan.
> Este documento no crea una release tecnica independiente: define el contrato de idioma/locale que cada release funcional debe implementar cuando expone contenido a usuarios finales.

## Principio rector

El codigo fuente de GradeOps AI se mantiene en ingles. Esto incluye nombres de clases, paquetes, modulos, funciones, variables, DTO fields, database columns, enum codes, error codes, event names, metric names, span names, log fields, prompt template filenames y test identifiers.

Todo lo que enfrenta a un usuario final queda sujeto a i18n: labels, botones, menus, placeholders, tooltips, validaciones, empty/loading/error states, emails, exports, reports, dashboards, textos generados por IA, preguntas, feedback, instrucciones para estudiantes y mensajes seguros de error.

Observabilidad, telemetria y logs siguen en ingles. Un log puede registrar `requested_locale`, `effective_locale` o `content_locale` como atributos normalizados, pero no debe cambiar `event_name`, `error_code`, `span.name`, `metric.name` ni mensajes tecnicos segun el idioma del usuario.

## Alcance de i18n

| Superficie | Regla |
|---|---|
| Source code | Siempre en ingles. No traducir identificadores, DTO fields, enum codes, database columns ni nombres de tests. |
| Web UI | Todo texto visible usa claves i18n; no hardcodear labels, botones, mensajes, placeholders, aria-labels, tooltips ni empty states en componentes. |
| API contracts | Field names y codes en ingles; valores user-facing pueden venir localizados o con `code` + `label` localizado. |
| Catalogos | `api/` entrega IDs/codes estables y labels localizados segun locale efectivo. |
| Errores | `errorCode` tecnico en ingles; `safeMessage` puede ser localizado o traducido por `web` desde code+params. |
| Contenido docente/estudiante | Se almacena con `contentLocale`/`outputLocale` cuando su idioma importa para generacion, revision, entrega o export. |
| GenAI | Los comandos a `agents/` incluyen locale objetivo cuando el output sera visible a usuario final. |
| Logs/traces/metrics | Ingles estable; solo atributos de locale normalizados, baja cardinalidad y sin PII. |
| Screenshots/videos de test | Pueden estar en locale de prueba, pero artefactos no deben filtrar PII/secrets. |
| Documentacion tecnica interna | Puede seguir el idioma del archivo vigente; las convenciones de runtime i18n se documentan aqui. |

## Locale y preferencias

GradeOps debe usar tags BCP 47 normalizados, por ejemplo `es-CL`, `es` y `en`. El locale default del producto para teacher-facing UI puede ser `es-CL` mientras no exista decision comercial distinta, pero el sistema no debe quedar cableado a un unico idioma.

Orden recomendado de resolucion:

1. Override explicito de la sesion/UI cuando el usuario selecciona idioma.
2. `preferredLocale` persistido en el perfil del usuario.
3. `Accept-Language` del navegador o cliente.
4. Default del tenant/organizacion si existe.
5. Default del producto.

El locale efectivo debe quedar disponible para `web`, `api` y operaciones asincronas. Si una accion genera contenido durable visible para usuarios, el locale de generacion debe capturarse en el comando y persistirse; no basta con confiar en el header actual porque el usuario puede cambiar idioma antes de que termine la operacion.

## Contrato Web-API

Los contratos entre `web/` y `api/` mantienen nombres de campos en ingles. El idioma aparece como dato de contexto, no como traduccion de la forma tecnica del contrato.

Reglas minimas:

- `web` debe enviar locale efectivo en cada request que pueda devolver texto visible o iniciar generacion visible; usar `Accept-Language` como base y un header/claim explicito solo si la arquitectura lo define.
- Para writes que generan contenido, incluir `outputLocale` o `contentLocale` en el request body cuando el idioma del resultado sea parte del dominio.
- Los valores canonicos se almacenan como IDs/codes estables, no como labels localizados.
- Los catalogos devuelven `code`, `label`, `locale` y, cuando aplique, `fallbackLocale`.
- Los errores devuelven `errorCode` estable en ingles, params seguros y `safeMessage` localizado solo si `api/` asume esa responsabilidad para la superficie.
- Los status de workflow, operation states y capability names viajan como codes en ingles; `web` los renderiza localizados.
- No usar strings localizados como claves de negocio, permisos, estados, filtros persistidos o comparaciones de dominio.

Ejemplo conceptual:

```json
{
  "data": {
    "assessmentId": "uuid",
    "status": "draft_ready",
    "statusLabel": "Borrador listo",
    "locale": "es-CL"
  },
  "meta": {
    "requestId": "req_123",
    "effectiveLocale": "es-CL",
    "fallbackLocale": null
  }
}
```

`status` es el valor de dominio. `statusLabel` es representacion user-facing y puede omitirse si `web` traduce el code.

## Contrato API-Agents

Los comandos y resultados de agentes mantienen nombres tecnicos en ingles. Cuando el output sera visto por docentes o estudiantes, el comando debe incluir el idioma objetivo:

- `outputLocale` para idioma del artefacto generado.
- `uiLocale` solo si el agente necesita adaptar copy auxiliar visible, no para logs.
- `contentLocale` cuando el input docente/estudiante ya trae un idioma propio.
- `programmingLanguage` separado de locale natural; no confundir `language` de programacion con `outputLocale`.

`api/` decide el locale efectivo antes de llamar a `agents/` y lo persiste en `AiOperation`, `AgentRun`, artefactos generados y eventos relevantes. `agents/` no infiere idioma desde el texto si `api/` ya lo definio, salvo para emitir warnings como `locale_mismatch` o `unsupported_locale`.

Los prompts pueden estar escritos y versionados como archivos en ingles, pero deben recibir variables de locale y reglas explicitas para producir la salida en el idioma solicitado. La validacion de salida debe detectar al menos:

- idioma incorrecto para el output;
- mezcla no intencional de idiomas;
- tono o registro no compatible con la audiencia;
- traduccion de terminos tecnicos que debian permanecer como nombres de lenguaje, API, libreria o codigo.

## Web

Cada tarea `web/` que cree o modifique UI debe incluir un gate i18n:

- no hardcodear texto user-facing en TSX/hook/schema;
- definir translation keys en ingles estable;
- cubrir labels, botones, placeholders, tooltips, aria-labels, mensajes de error, toasts, empty/loading states y copy de confirmacion;
- usar pluralizacion, interpolacion segura y formateo locale-aware para fechas, numeros, duracion, porcentajes y moneda;
- setear `lang`/metadata de pagina segun locale efectivo;
- permitir selector de idioma o respetar `preferredLocale` cuando el producto lo habilite;
- no traducir nombres de campos tecnicos, IDs, codes, stack traces, correlation IDs ni support references;
- mantener layout resistente a textos mas largos en otros idiomas.

## API

Cada tarea `api/` que exponga texto visible o genere contenido debe incluir:

- resolver locale temprano en la request, antes de construir respuestas user-facing;
- validar locale contra allowlist y aplicar fallback seguro;
- persistir `preferredLocale` cuando corresponda al perfil;
- exponer catalogos/read models con labels localizados o codes suficientes para que `web` traduzca;
- separar `errorCode` tecnico de `safeMessage` localizado;
- guardar `contentLocale`/`outputLocale` en artefactos generados o user-authored cuando sea relevante;
- capturar locale en operaciones asincronas para que el resultado no cambie por cambios posteriores de UI;
- no localizar logs, metrics, traces, audit events tecnicos ni enum codes persistidos.

## Agents

Cada tarea `agents/` que produzca texto visible debe incluir:

- command fields en ingles y `outputLocale` explicito;
- prompt template versionado que instruya el idioma de salida sin traducir el contrato tecnico;
- validators o checks de idioma/tone cuando el output sea teacher-facing o student-facing;
- warnings estructurados en ingles para `locale_mismatch`, `unsupported_locale`, `mixed_language_output` o `untranslated_user_facing_key`;
- logs/traces en ingles, sin prompts/respuestas completas salvo evidencia redacted autorizada;
- no persistir dominio ni preferencias de usuario directamente.

## Observabilidad y evidencia

i18n no cambia el idioma operacional del sistema.

| Elemento | Idioma |
|---|---|
| `event_name`, `span.name`, `metric.name` | Ingles |
| `error_code`, `warning_code`, `status_code` | Ingles/codes estables |
| Log message tecnico | Ingles |
| Dashboard operativo interno | Puede localizar labels si es user-facing; queries/eventos siguen ingles |
| Product event payload | Fields/codes en ingles; locale como atributo |
| Evidence export para usuario | Localizable si se entrega a usuario final |
| Evidence export tecnico/auditoria | Fields/codes en ingles; user-facing sections pueden localizarse |

Dimensiones permitidas de baja cardinalidad: `requested_locale`, `effective_locale`, `content_locale`, `output_locale`, `fallback_locale`, `locale_source`. No usar texto traducido como label de metrica.

## Seguridad

- Locale debe validarse por allowlist; no aceptar valores libres en prompts, paths o template names.
- No usar locale para decidir permisos, ownership o tenant.
- No filtrar existencia de recursos a traves de mensajes localizados distintos.
- Fallbacks deben ser consistentes y no exponer detalles tecnicos.
- Traducciones no deben contener HTML no sanitizado ni interpolaciones inseguras.
- Prompt instructions de idioma no deben permitir prompt injection via labels, catalogos o locale strings.

## Testing

Cada release debe probar i18n segun el riesgo:

- unit/component tests verifican ausencia de texto user-facing hardcodeado en superficies modificadas;
- tests de translation keys cubren claves requeridas, interpolaciones, pluralizacion y fallback;
- Web-API contract tests cubren `Accept-Language`/locale efectivo, catalog labels y error safe messages o error code+params;
- API-Agents tests cubren `outputLocale` en comandos y outputs GenAI simulados en el idioma solicitado;
- acceptance/e2e cubre al menos el locale default del producto y un segundo locale cuando la release declara soporte;
- tests negativos cubren locale no soportado, fallback y no filtracion de detalles tecnicos;
- observability tests o review verifican que logs/traces/metrics siguen en ingles y solo incluyen locale como atributo controlado.

## Aplicacion por release

| Release | Incremento i18n requerido | No incluir aun |
|---|---|---|
| R01 | Fundacion: `preferredLocale`/locale efectivo, Web i18n scaffold para dashboard/intake/draft, API locale resolver, catalog labels para intake, `outputLocale` para generation/regeneration, prompt guardrails y logs en ingles | Traduccion completa de todo el producto historico |
| R02 | Rubric/submission/grading/feedback con UI localizada, safe errors localizados, feedback student-facing en `outputLocale`, y contract tests Web-API/API-Agents | Tone marketplace o traduccion automatica de submissions |
| R03 | Gaps/recovery/report con idioma de reporte, estimaciones y export user-facing localizados; mantener evidence/logs tecnicos en ingles | BI multilingue completo |
| R04 | Question bank/closed authoring con question text en `outputLocale`, catalogos curriculares localizados y validators de idioma/ambiguedad | Scoring dependiente de IA o traducciones no revisadas |
| R05 | Student link flow, attempts, result publication y item analytics con locale de estudiante/teacher; emails y public screens localizados | Student accounts o preferencias complejas por familia/curso |
| R06 | Operator/evidence dashboard distingue UI localizable de telemetria tecnica en ingles; exports publicables localizados y audit exports tecnicos estables | Data warehouse multilingue o localizacion de logs crudos |

## Criterio de salida

Una release con texto visible o contenido generado no queda lista si:

- contiene copy user-facing hardcodeado en componentes o handlers;
- no declara locale efectivo ni fallback;
- usa labels localizados como valores canonicos;
- mezcla programming language con natural language/locale;
- permite que un cambio de idioma posterior altere el resultado de una operacion async ya iniciada;
- genera contenido IA en idioma distinto al solicitado sin warning;
- localiza logs, metric names, span names, event names o error codes tecnicos;
- no prueba al menos el locale default y los fallbacks requeridos por la release.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Creacion inicial | Incorporar i18n como gate transversal por release funcional sin traducir codigo, contratos tecnicos ni telemetria | Master Plan, R01-R06, web/api/agents/testing/observabilidad | D-I18N-01..D-I18N-10 |

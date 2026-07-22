# Estrategia UI Design System y semantica de datos - GradeOps AI

> Incorporacion de reglas de diseno previo, Design System y semantica de datos para toda implementacion de UI.
> Este documento no crea una release tecnica independiente: define gates obligatorios que cada release funcional debe aplicar cuando toca `web/`, rutas, formularios, tablas, dashboards o flujos de escritura.

## Principio rector

Wireframes y mockups no reemplazan el diseno de producto desde el Design System. Antes de crear wireframes, maquetas funcionales o componentes, cada pantalla debe declarar:

- objetivo de usuario y accion visible de entrada;
- patron DS seleccionado: shell, page, section, form, table, tabs, dialog, card, empty state, loading, error, etc.;
- componentes DS previstos para cada control: `Input`, `Textarea`, `Select`, `Checkbox`, `Radio`, `Switch`, `Tag`, `Tabs`, `Button`, `IconButton`, etc.;
- naturaleza de cada dato mostrado o escrito;
- fuente de verdad y endpoint/tabla/catalogo que respalda cada dato;
- contrato `api/` que entrega o recibe cada dato de pantalla;
- modo de comunicacion por accion/dato: sincrono o asincrono;
- mecanismo de finalizacion/progreso cuando sea asincrono: polling de operacion, SSE, WebSocket, webhook server-to-server, push/notification u otro mecanismo definido;
- contrato i18n: locale efectivo, labels/mensajes/claves de traduccion, catalog labels, safe errors y `outputLocale` cuando la pantalla dispara contenido generado visible;
- restricciones de longitud, caracteres, formato, valores permitidos, cardinalidad y obligatoriedad.

Una UI que usa `input text` para todo no cumple el gate si el dominio tiene valores fijos, enums, datos maestros, relaciones, numeros, fechas, booleanos o selecciones multiples.

## Fuentes incorporadas

| Area | Fuente |
|---|---|
| ADR Design System | `docs/99-decisions/2026-06-21-web-design-system.md` |
| ADR UI/data semantics | `docs/99-decisions/2026-07-21-ui-design-data-semantics.md` |
| Design System local | `web/design-system/` |
| UX teacher workspace | `docs/06-ux/teacher-workspace-ux.md` |
| Data model | `docs/04-architecture/data-model.md` |
| API-Agent Orchestration | `docs/master-plan/analysis/api-agent-orchestration-strategy.md` |
| Testing strategy | `docs/master-plan/analysis/testing-strategy.md` |

## Clasificacion obligatoria de campos

Toda historia/tarea UI con lectura o escritura de datos debe clasificar cada campo:

| Clase | Control UI esperado | Fuente de verdad |
|---|---|---|
| Texto libre | `Textarea` o `Input` segun longitud | Campo `text`/`string` con max length y sanitizacion |
| Texto restringido | `Input` con mascara/validacion | Regla de dominio/API: charset, regex, largo minimo/maximo |
| Enum fijo | `Select`, `Radio`, `Tabs` o `Badge` de estado | Enum versionado en API/dominio |
| Catalogo maestro single | `Select`, searchable select o picker | Tabla/catalogo API (`Subject`, `Course`, `ProgrammingLanguage`, etc.) |
| Catalogo maestro multiple | multi-select, `Tag` removible, checkbox group | Relacion N:N o lista de IDs validada por API |
| Numero | numeric input, stepper o slider | Campo numerico con min/max/unidad |
| Booleano | `Switch`/checkbox | Campo booleano o capability |
| Fecha/hora | date/time picker | Campo temporal con timezone/regla |
| Read-only/provenance | texto, badge, table cell, tooltip | API/DB/evento, nunca editable en UI |
| Generated editable | editor estructurado con estado dirty/provenance | Output IA persistido mas edicion docente auditada |

Si la fuente de verdad todavia no existe, la tarea no debe inventar texto libre como reemplazo silencioso. Debe registrar una de estas salidas:

- implementar o enriquecer el contrato `api/` requerido para lectura/escritura antes de cerrar la UI;
- crear o enriquecer catalogo/API/tabla maestra;
- aceptar temporalmente un campo custom controlado con validacion explicita;
- bloquear la UI hasta que exista la fuente;
- registrar residual tecnico/producto con impacto visible.

## Contrato API I/O y sync/async

Toda pantalla debe declarar sus datos de entrada/salida contra `api/`, no contra fixtures, DTOs inventados ni llamadas directas a `agents/`.

| Aspecto | Regla |
|---|---|
| Lectura de datos | Cada tabla, dashboard, selector, estado, badge o detalle debe tener endpoint/read model/API contract asociado. |
| Escritura de datos | Cada submit, save, approve, reject, regenerate, upload, publish o export debe tener mutation/command endpoint asociado. |
| API ausente | Si `api/` no expone el dato necesario, la release debe agregar tarea `api/`/DB/infra o residual explicito antes de cerrar la UI. |
| Sync | Usar solo cuando el resultado esperado cabe en una respuesta HTTP corta, deterministica y con timeout razonable. |
| Async | Usar cuando hay IA, batch, upload pesado, scoring masivo, reportes, exports, analytics o latencia incierta. |
| Finalizacion async | Debe existir mecanismo acordado: `GET /operations/{id}` polling, SSE, WebSocket, webhook server-to-server, push/notification u otro. |
| Estado UI async | La pantalla debe mostrar queued/running/succeeded/failed/cancelled/timeout segun contrato, no spinners indefinidos. |
| Idempotencia | Todo comando mutante con costo, IA o efectos no triviales debe definir `Idempotency-Key` o equivalente. |

Los webhooks son apropiados para integraciones server-to-server. Para el navegador, el default debe ser polling de una operacion consultable, SSE o WebSocket segun necesidad de tiempo real. La decision debe quedar en la tarea; no se infiere despues en implementacion.

## Datos maestros candidatos

El Master Plan debe detectar datos que no deberian repetirse como strings libres:

| Dominio | Ejemplos | Uso UI |
|---|---|---|
| Curriculum | subject/asignatura, materia, unidad, learning outcome, topic/tema | Select o picker, con opcion custom solo si el dominio lo permite |
| Course/cohort | curso, seccion, periodo, nivel/programa | Select single/multiple, scoping por teacher/organization |
| Assessment | level/difficulty, status, mode, duration presets, programming language | Enum/select/radio/stepper segun caso |
| Question bank | question type, difficulty, tags, outcomes, scoring policy | Select/multi-select/tags |
| Workflow | approval state, generation status, invitation status, result status | Badges/tabs, no inputs libres |
| AI/provider | provider, model, policy, prompt/schema version | Read-only o selector autorizado por `api`, nunca texto libre en `web` |

## Gate UI por tarea

Toda tarea que cree o modifique pantallas, formularios, tablas, dashboards, filtros, rutas o acciones de `web/` debe incluir un checkpoint UI Design/Data Semantics.

El checkpoint debe verificar:

- diseno previo referenciado desde `web/design-system/` antes de wireframe/mockup;
- componente DS correcto para cada campo/control;
- matriz de campos con clase de dato, lectura/escritura, fuente de verdad, restricciones, cardinalidad y control UI;
- DTO/API contract no contradice la semantica real del dominio;
- todos los datos de pantalla, tanto lectura como escritura, estan alineados con endpoints/contratos `api/`;
- si falta `api/`, existe tarea/residual para implementarlo antes de cerrar la UI;
- cada accion declara si usa comunicacion sync o async;
- cada accion async declara mecanismo de completion/progress, estados UI, timeout, retry/cancel e idempotencia;
- los datos maestros requeridos tienen endpoint/API/tabla o residual explicitado;
- inputs libres solo se usan para datos realmente libres;
- enums/statuses no se editan como texto;
- valores custom tienen validacion, auditabilidad y regla de negocio;
- fake data/mockups representan catálogos, enums, errores y estados reales;
- copy user-facing y labels de catalogos usan claves/labels i18n; no se hardcodean textos en componentes o schemas;
- si la pantalla genera contenido visible, el campo `outputLocale`/`contentLocale` queda definido y alineado con `api/`/agents;
- unit tests cubren mapeo de campos/control, valores fijos, validacion y errores;
- acceptance/e2e cubre seleccion de valores maestros y rechazo de valores invalidos.

## Aplicacion R01 - Assessment Creation

R01 debe revisar `/assessments/new` antes de implementarlo como formulario final:

| Campo | Semantica esperada | Implicancia |
|---|---|---|
| `learningGoal` | texto libre largo | `Textarea`, con max length y ayuda contextual |
| `topic` | tema/curriculum: candidato a dato maestro o tag controlado | No asumir `Input` libre; detectar `Subject`/`Topic`/custom allowed |
| `level` | enum/difficulty | `Select` o `Radio`, no texto libre |
| `duration` | numero/minutos o preset | numeric input/stepper/select con min/max/unidad, no string libre |
| `language` | enum/catalogo de lenguaje | `Select` o combo controlado; custom solo si API lo permite |
| constraints/resources | lista de restricciones/recursos | tags/checkbox/multi-select cuando se incorporen al contrato |

Si el API actual solo acepta strings para `topic`, `level`, `duration` o `language`, R01 debe registrar el gap: o se ajusta contrato/modelo/API antes de cerrar UI, o se deja un residual explicito y no se presenta como UX definitiva.

R01 tambien debe acordar el contrato de comunicacion del intake:

- crear brief puede mantenerse sincrono si `api/` persiste y responde rapido con `assessmentId`;
- generation/regeneration debe declararse sincrona compatible u operation-backed asincrona;
- si pasa a asincrona, `api/` debe exponer una operacion consultable y `web/` debe saber cuando termina por polling/SSE/WebSocket u otro mecanismo elegido;
- la UI no debe simular finalizacion con timers locales ni asumir que el draft esta listo sin estado de `api/`.

## Criterio de salida

Una UI no queda lista solo porque renderiza y envia datos. Debe demostrar:

- acceso desde accion visible;
- diseno previo DS aprobado o referenciado;
- controles coherentes con la semantica de los datos;
- fuente de verdad declarada para lectura/escritura;
- contrato `api/` disponible para todos los datos I/O de pantalla o residual/API task explicito;
- modo sync/async y mecanismo de finalizacion async documentado y probado cuando aplique;
- datos maestros/enums/catalogos tratados como tales;
- validaciones client-side alineadas con API/dominio, sin sustituir validacion server-side;
- pruebas unitarias, acceptance y e2e que ejercen valores validos/invalidos reales.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Incorporacion de i18n en UI/data | Asegurar que labels, mensajes, catalogos y outputs visibles respeten locale sin traducir contratos tecnicos | UI strategy, R01, web tasks | D-I18N-01..D-I18N-10 |
| 2026-07-21 | Incorporacion de API I/O y sync/async contract | Alinear datos de pantalla con `api/` y exigir mecanismo de finalizacion para flujos asincronos | UI strategy, R01, web/api tasks | D-UI-01..D-UI-08, D-API-01..D-API-10 |
| 2026-07-21 | Creacion inicial | Evitar UI con inputs libres para datos restringidos y exigir diseno DS previo a wireframes/mockups | Master Plan, R01, web planning, tasks UI | D-UI-01..D-UI-08 |

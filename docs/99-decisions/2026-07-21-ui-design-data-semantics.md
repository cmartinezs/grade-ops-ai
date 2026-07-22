# ADR - UI Design System y semantica de datos por release funcional

## Estado

Aceptada.

## Contexto

Las pantallas de GradeOps AI se planifican con wireframes, jerarquias y mockups funcionales, pero ese proceso no basta si la pantalla no parte desde el Design System ni respeta la naturaleza real de los datos que lee o escribe.

El caso visible en R01 es `/assessments/new`: el contrato actual de intake puede verse como `{learningGoal, topic, level, duration, language}` en strings, pero el dominio ya distingue datos libres, enums, numeros y posibles datos maestros. Si la UI convierte todo en `input text`, se introducen valores ambiguos, validaciones pobres, drift con `api/` y deuda de datos para agentes y reportes.

## Decision

Toda implementacion de UI debe incorporar un gate de UI Design/Data Semantics dentro de la release funcional correspondiente, no como release tecnica independiente.

- Antes de wireframe/mockup/componente, la tarea debe declarar el patron del Design System y los componentes DS esperados por control.
- Cada dato mostrado o escrito debe clasificarse como texto libre, texto restringido, enum fijo, catalogo maestro single/multiple, numero, booleano, fecha/hora, read-only/provenance o generated editable.
- Cada campo debe declarar fuente de verdad, restricciones de longitud/formato/caracteres, cardinalidad, obligatoriedad y control UI.
- Cada dato requerido en pantalla, tanto lectura como escritura, debe estar respaldado por `api/`: endpoint, read model, catalogo, defaults, capabilities, mutation o estado de operacion.
- Si `api/` no existe para el dato requerido, la release debe implementar el contrato/API/DB/infra necesario o registrar residual bloqueante; la UI no puede resolverlo con mocks permanentes ni DTOs locales.
- Cada accion debe declarar si su comunicacion es sincrona o asincrona. Si es asincrona, debe acordar mecanismo de finalizacion/progreso: polling de operacion, SSE, WebSocket, webhook server-to-server, push/notification u otro mecanismo explicito.
- Los datos maestros o valores restringidos no se degradan silenciosamente a texto libre. Si falta tabla/API/catalogo, la tarea debe crear el scope correspondiente o registrar residual explicito.
- Las pruebas unitarias, acceptance y e2e deben cubrir controles, valores validos/invalidos, catalogos/enums y la accion visible de entrada al flujo.

## Consecuencias

- R01 debe revisar `learningGoal`, `topic`, `level`, `duration` y `language` antes de cerrar `/assessments/new` como UX definitiva.
- `learningGoal` es texto libre largo; `level`, `duration` y `language` no deben implementarse como inputs libres si el dominio define enum, numero, preset o catalogo.
- `topic` debe tratarse como candidato a dato maestro/curricular o tag controlado, con opcion custom solo si `api/` lo permite explicitamente.
- Futuras pantallas de rubricas, submissions, question bank, student links, dashboards y exports heredan el mismo gate cuando tengan lectura/escritura de datos.
- `web/` no puede inventar tipos independientes ni transformar restricciones de dominio en copy local; `api/` sigue siendo la fuente de verdad.
- Flujos IA, batch, exports, analytics o cualquier operacion de latencia incierta deben acordar contractualmente si siguen siendo sync o pasan a async, y como la UI detecta completion, failure, timeout y retry/cancel.

## Fuentes

- `docs/master-plan/analysis/ui-design-data-strategy.md`
- `docs/99-decisions/2026-06-21-web-design-system.md`
- `docs/04-architecture/data-model.md`
- `docs/06-ux/teacher-workspace-ux.md`
- `web/design-system/`

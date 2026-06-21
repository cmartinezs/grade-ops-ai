# Checklists operativos

## Checklist general antes de implementar

- Identifique modulo(s) impactado(s).
- Revise clases analogas.
- Revise tests analogos.
- Confirme si existe planning/RFC requerido.
- Defina si el cambio afecta API, seguridad, persistencia, dominio, configuracion o docs.
- Verifique que no hay una implementacion reutilizable.
- Verifique que no se rompe direccion de dependencias.
- Verifique que no se introducen secretos.

## Checklist de arquitectura

- `domain` sigue sin Spring/JPA/HTTP.
- `app` depende de puertos, no adaptadores.
- `api` no usa JPA repository directo.
- `persistence` no filtra entities a app/api.
- `run` o composicion solo ensambla/configura.
- Nuevos bounded contexts siguen estructura existente.
- No hay imports desde modulos aguas abajo.

## Checklist de dominio

- La regla vive en value object/entidad si es invariante.
- Hay factory/builder validado.
- Hay excepcion especifica si aplica.
- Estados son enums.
- `toString()` no expone secretos.
- Tests cubren casos validos e invalidos.

## Checklist de caso de uso

- Nombre `FooUseCase`.
- Entrada `FooCommand`.
- Salida `FooResult` si no devuelve dominio.
- Dependencias por constructor.
- Dependencias externas como `FooPort`.
- No usa Spring/JPA/HTTP.
- Verifica que no persiste si falla una regla.
- Tests con Mockito/AssertJ.

## Checklist de API

- Controller delega a use case.
- Request DTO separado de command si corresponde.
- Response DTO separado de dominio si corresponde.
- Usa envelope si el proyecto lo exige.
- Usa response code si el proyecto tiene catalogo.
- Define HTTP status correcto.
- Tiene validacion de input.
- Tiene `@PreAuthorize` o policy si es protegido.
- Si es RFC/contrato externo nativo, documenta por que no usa envelope.
- OpenAPI actualizado.
- Coleccion de API o SDK actualizado si UI/cliente lo consume.

## Checklist de seguridad

- No hay secretos hardcodeados.
- No se loguean tokens/passwords/private keys.
- Endpoint protegido tiene autorizacion.
- Endpoint publico esta declarado y testeado.
- Cambio de path usa `getServletPath()` si hay contexto servlet.
- JWT requiere roles/scopes cuando corresponde.
- Roles/scopes se normalizan correctamente.
- `SecurityContextHolder` se limpia en `finally`.
- MDC se limpia en `finally`.
- ThreadLocal se limpia en `finally`.
- Error 401/403 usa respuesta estructurada.
- Detalles tecnicos solo local/dev.
- Actuator no queda expuesto en prod sin control.

## Checklist de OAuth2/OIDC

- Authorization code es one-time use.
- Expiracion validada.
- Redirect URI exacta.
- PKCE validado.
- Token firmado con clave activa.
- Header JWT incluye `kid`.
- Claims obligatorios presentes.
- JWKS publica solo material publico.
- No se expone private material.
- Tests cubren errores principales.

## Checklist de persistencia

- Puerto definido en `app`.
- Adapter en `persistence`.
- Entity JPA separada de dominio.
- Mapper domain/entity.
- Repository Spring Data encapsulado.
- ID generado por JPA en nuevas entities cuando corresponde.
- `@Data` evitado en entities si implica riesgos.
- JSONB usa tipo/column definition correcto si aplica.
- Migration Flyway/Liquibase versionada.
- `ddl-auto=validate`.
- `open-in-view=false`.
- Queries nuevas testeadas.

## Checklist de configuracion

- Variables sensibles desde entorno.
- Defaults seguros.
- Properties bajo prefijo coherente.
- Profiles separados.
- No se cambia config prod para resolver problema local.
- CORS revisado.
- Actuator revisado.
- Timezone coherente.

## Checklist de testing

- Unit tests de dominio.
- Unit tests de use case.
- Tests de controller si cambia API.
- Tests de adapter si cambia persistencia.
- Tests de filtro si cambia seguridad/path.
- Test de regresion para bug corregido.
- Tests de error, no solo happy path.
- Verifica que no se llama al puerto si falla validacion.
- Verifica limpieza de contextos.
- Ejecuta modulo impactado.
- Ejecuta `clean verify` antes de PR si el cambio es amplio.

## Checklist de documentacion

- README si cambia uso local o URLs.
- `CHANGELOG.md` si es cambio notable.
- `CONTRIBUTING.md` si cambia flujo.
- `SECURITY.md` si cambia politica.
- OpenAPI si cambia contrato HTTP.
- Coleccion de API o SDK si cambia endpoint consumible.
- Guia de agentes AI si cambia regla para asistentes.
- No crear docs en rutas que la guia del repo prohibe.
- Verificar que las rutas referenciadas existen.

## Checklist de PR

- Descripcion clara.
- Modulos impactados.
- Como se probo.
- Riesgos/tradeoffs.
- Cambios de API documentados.
- Migraciones mencionadas.
- Sin secretos en diff.
- Tests pasan.
- Cobertura revisada.
- Changelog actualizado si corresponde.

## Checklist para revisar codigo generado por AI

- Respeta arquitectura hexagonal o por capas?
- Respeta nombres del repo?
- Invento clases innecesarias?
- Duplico logica existente?
- Cambio archivos no relacionados?
- Agrego dependencias al modulo incorrecto?
- Genero tests reales o triviales?
- Maneja errores con codigos del proyecto?
- Usa logs seguros?
- Deja TODOs vagos?
- Cita documentos inexistentes?
- Declara limitaciones de verificacion?

# Seguridad

Todo backend Spring Boot debe tratar la seguridad como parte del diseno, no como un accesorio. Esto aplica tanto a sistemas publicos como internos: autenticacion, autorizacion, secretos, auditoria, CORS, datos sensibles, errores seguros y operacion deben estar cubiertos.

## Politica de seguridad

Un `SECURITY.md` util define:

- Versiones soportadas.
- Canal privado para reportar vulnerabilidades.
- Datos esperados del reporte: descripcion, reproduccion, version afectada, impacto y sugerencias.
- Tiempos esperados de respuesta, actualizacion y resolucion segun severidad.
- Practicas declaradas: dependencias actualizadas, analisis automatizado, code reviews, tests de seguridad en CI/CD, minimo privilegio y cifrado de datos sensibles.

Buenas practicas para AI:

- Si detecta una vulnerabilidad, no debe escribirla como issue publico ni documentarla con exploits operativos.
- Debe proponer mitigacion y pruebas.
- Debe evitar generar secretos o credenciales reales.

## Secretos y variables de entorno

Practicas recomendadas:

- `.gitignore` excluye `.env`, `.env-local`, `.env-dev`, `.env-prod`, `.env.*` y backups.
- Permite `!.env.example` solo con placeholders.
- Archivos `application-*.yml` leen datasource, API keys, JWT secrets, SMTP credentials y claves privadas desde entorno o secret manager.
- Dockerfile y compose no bakean secretos en la imagen.
- La guia de contribucion exige no incluir secretos en diff.

Buenas practicas:

- Nunca commitear tokens, passwords, JWTs reales, private keys ni `.env`.
- Usar placeholders seguros.
- En docs, preferir `DB_PASSWORD=<set-by-secret-manager>` a valores reales.
- En tests, generar claves efimeras cuando sea necesario.
- Revisar diffs con busqueda de `password`, `secret`, `token`, `key`, `private`, `Bearer`.

## Autenticacion

Un backend stateless suele autenticar con Bearer JWT, resource server OAuth2 o un filtro propio cuando el caso lo exige.

Un filtro de autenticacion robusto:

- Lee `Authorization: Bearer <jwt>`.
- Rechaza header ausente, mal prefijado o token blank cuando la ruta es protegida.
- Verifica firma, algoritmo, expiracion, issuer, audience y claims obligatorios.
- Extrae roles/scopes.
- Normaliza authorities como `ROLE_<ROL>` o `SCOPE_<scope>` segun convencion.
- Inserta `Authentication` en `SecurityContextHolder`.
- Enriquece MDC con `userId` o sujeto.
- Limpia `SecurityContextHolder` y MDC en `finally`.

Buenas practicas:

- No aceptar tokens sin roles/scopes cuando el endpoint requiere autorizacion.
- No continuar cadena cuando falla autenticacion en path protegido.
- No exponer detalle tecnico salvo perfiles `local/dev`.
- Probar roles como lista y como CSV si ambos estan soportados.
- Preferir soporte nativo de Spring Security cuando cubre el caso.

## Autorizacion

Spring Security permite combinar reglas HTTP con method security.

Practica clave:

- Autenticacion en filtro/resource server.
- Autorizacion fina en metodos/controladores con `@PreAuthorize`, policies o evaluadores.

Ventajas:

- Permite rutas publicas complejas como login, OAuth2/OIDC, webhooks o discovery.
- Mantiene decision de permisos cerca de endpoint/caso.
- Evita reglas HTTP gigantes para cada rol.

Riesgos:

- Si un endpoint protegido olvida `@PreAuthorize` o policy equivalente, puede quedar expuesto.
- Por eso los tests y checklists deben revisar cada endpoint nuevo.

Regla para AI:

- Todo endpoint bajo rutas protegidas debe declarar autorizacion o estar cubierto por una regla global verificable.
- Si se agrega ruta publica, debe documentarse en properties/configuracion y agregarse test de regresion.

## Public paths

Centralice prefijos/sufijos publicos en properties o configuracion versionada:

- Actuator permitido.
- Service info.
- Swagger UI.
- API docs.
- `.well-known`.
- Login/registro/recuperacion si aplica.
- OAuth2 authorize/token/revoke/userinfo si aplica.
- Webhooks publicos con firma.
- Archivos publicos o discovery.

Buenas practicas:

- No hardcodear rutas publicas dispersas.
- Usar properties.
- Distinguir `prefix`, `suffix` y `segment`.
- Testear cada categoria.
- Validar `context-path` usando `getServletPath()`.

## Multi-tenancy o scoping organizacional

Si el sistema usa tenant, organizacion, cuenta, workspace o proyecto como scope, resuelvalo una vez por request.

Un filtro de scoping:

- Lee header, subdominio, path variable o claim.
- Busca el scope.
- Falla 404 si no existe o no debe revelarse.
- Falla 403 si existe pero el usuario no puede acceder.
- Guarda el scope en un context holder.
- Inserta el identificador en MDC.
- Limpia `ThreadLocal` y MDC en `finally`.

Buenas practicas:

- Nunca dejar `ThreadLocal` sin limpiar.
- No asumir scope si el dato falta; endpoints scoped deben validarlo.
- Agregar tests para scope inexistente, suspendido/inactivo, sin header y sin permisos.
- Evitar confiar solo en filtros si las queries necesitan enforcement por tenant.

## OAuth2/OIDC

Si el sistema implementa OAuth2/OIDC:

- Authorization Code debe modelarse con estado.
- PKCE debe validarse cuando aplica.
- Redirect URI debe coincidir exactamente.
- Codigo se usa una sola vez.
- Codigo expira.
- Estados explicitos: pending, used, revoked, expired.
- JWKS y OIDC metadata son endpoints nativos RFC y pueden ser excepcion al envelope del proyecto.
- JWT se firma con clave activa y `kid`.
- Solo claves publicables aparecen en JWKS.

Buenas practicas:

- No relajar comparacion exacta de redirect URI.
- No reutilizar authorization codes.
- No aceptar roles/scopes vacios cuando son obligatorios.
- No firmar tokens con claves hardcodeadas.
- No exponer private material.
- Tests deben verificar header `kid`, algoritmo, claims y expiracion.

## Passwords y credenciales

Practicas recomendadas:

- `CredentialEncoderPort` o servicio equivalente abstrae hashing.
- Implementacion concreta usa BCrypt, Argon2 o algoritmo aprobado.
- Casos de uso dependen de puerto, no del encoder concreto.
- Password reset/recovery tienen endpoints publicos controlados.

Buenas practicas:

- Nunca guardar passwords en claro.
- No loguear password ni temporary password.
- Abstraer hashing para pruebas y cambios futuros.
- Reset/recovery deben tener expiracion, invalidacion y rate limit.

## CORS y CSRF

Configure:

- CORS desde properties.
- `allowCredentials` solo si es necesario.
- Sesiones stateless si usa Bearer tokens.
- CSRF deshabilitado solo cuando no aplica por arquitectura.
- Frame options restrictivas salvo herramientas locales como H2 console.

Buenas practicas:

- CORS permitido solo a origenes configurados.
- En produccion, no usar wildcard con credentials.
- Si hay cookies/sesion de browser, revisar CSRF cuidadosamente.
- Mantener perfiles locales diferenciados.

## Actuator

Actuator es util pero sensible.

Buenas practicas:

- En produccion exponer solo `health`, `info` y endpoints necesarios.
- Proteger endpoints sensibles.
- Separar actuator por puerto/red cuando sea posible.
- No exponer `env`, `configprops`, `heapdump`, `threaddump` o metrics sensibles sin control.
- Revisar `management.endpoints.web.exposure.include` por ambiente.

## Errores seguros

Practicas recomendadas:

- `AccessDeniedHandler` escribe respuesta estructurada en 403.
- Filtros de autenticacion escriben JSON estructurado en 401.
- Detalles tecnicos solo para `local/dev`.
- Locale/i18n se resuelve antes de posibles rechazos si el producto lo requiere.

Buenas practicas:

- Cliente recibe mensaje util, no stack trace.
- Logs contienen contexto para diagnostico.
- Error temprano en filtro debe usar el mismo envelope que el resto.
- No revelar si un usuario, tenant, cuenta o recurso existe cuando eso facilite enumeracion.

## Docker y runtime

Un Dockerfile seguro aplica:

- Multi-stage build.
- Builder con JDK.
- Runtime con JRE o imagen minima.
- Usuario no-root.
- Capas de Spring Boot extraidas si se busca cache eficiente.
- Healthcheck.
- JVM container-aware.
- Credenciales por env vars.

Buenas practicas:

- No ejecutar como root.
- No copiar archivos innecesarios.
- Usar healthcheck.
- No bakear secretos en imagen.
- Definir defaults seguros o vacios para valores sensibles.

## Checklist de seguridad para cambios

- El cambio toca endpoint protegido?
- Tiene `@PreAuthorize` o equivalente?
- Cambia rutas publicas?
- Se agrego test para el filtro o regla de seguridad?
- Se agrego test con `context-path` si aplica?
- Se mantiene `SecurityContextHolder.clearContext()`?
- Se mantiene limpieza de MDC/ThreadLocal?
- Se introdujo nuevo secreto?
- El secreto esta en env var/secret manager?
- Se evita loguear token/password/private key?
- Cambia CORS/Actuator?
- Cambia OAuth/OIDC/JWT?
- Hay prueba de regresion?

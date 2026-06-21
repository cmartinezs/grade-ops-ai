# Resumen ejecutivo

Un backend Spring Boot mantenible debe separar decisiones de negocio, casos de uso, infraestructura y exposicion HTTP. Esa separacion permite probar reglas de forma rapida, evolucionar integraciones sin contaminar el nucleo y pedir ayuda a agentes AI sin que mezclen responsabilidades.

La guia asume un proyecto Java moderno con Spring Boot, Maven o Gradle, arquitectura modular, seguridad con Spring Security, persistencia relacional con JPA/Flyway, configuracion externalizada, OpenAPI, CI, Docker y documentacion de contribucion. Las mismas reglas aplican aunque el sistema resuelva identidad, pagos, inventario, educacion, salud, operaciones internas o cualquier otro problema.

## Principios rectores

1. Separacion estricta de modulos o paquetes.
   Mantenga un nucleo de dominio puro, una capa de aplicacion con casos de uso y puertos, adaptadores tecnicos, controllers HTTP, persistencia y un modulo de ensamblaje Spring Boot. Los nombres pueden variar, pero la direccion de dependencia debe ser explicita.

2. Dominio primero.
   Las reglas de negocio viven en entidades, value objects, agregados, politicas, excepciones y estados explicitos. Ejemplos transferibles: `EmailAddress`, `Money`, `OrderStatus`, `AccountId`, `AuthorizationCode`, `BusinessRuleViolation`.

3. Puertos y adaptadores.
   Los casos de uso dependen de interfaces como `CustomerRepositoryPort`, `ClockPort`, `TokenSignerPort`, `NotificationPort` o `PaymentGatewayPort`, no de detalles concretos como JPA, HTTP clients, JWT libraries o reloj del sistema.

4. Contratos explicitos.
   Los endpoints deben usar DTOs, codigos de respuesta de negocio estables y un formato de error consistente. Si el proyecto usa un envelope como `BaseResponse<T>`, apliquelo de forma uniforme salvo endpoints que deban cumplir un RFC o contrato externo nativo.

5. Seguridad por capas.
   Combine filtros o resource server JWT, `@EnableMethodSecurity`, `@PreAuthorize`, validacion de roles/scopes, scoping por tenant/organizacion cuando aplique, CORS por properties, respuestas 401/403 estructuradas y limpieza de contexto de seguridad.

6. Observabilidad desde el inicio.
   Use un filtro de trazabilidad que reciba o genere `X-Trace-ID`, lo propague en respuesta y lo inserte en MDC junto con metodo, path, usuario y tenant/organizacion si aplica. Configure logs locales legibles y logs estructurados en ambientes reales.

7. Configuracion externalizada.
   Secrets y parametros sensibles deben leerse desde variables de entorno o secret managers. `.env` debe estar ignorado. `.env.example` puede existir solo con placeholders seguros. No deje defaults reales para credenciales.

8. Testing por riesgo y capa.
   Agregue tests unitarios de value objects, entidades, casos de uso, filtros, adaptadores y componentes de seguridad. Valide casos felices, errores, regresiones de context-path, limpieza de `MDC`, `ThreadLocal` y `SecurityContextHolder`.

9. Calidad automatizada.
   Use Maven Enforcer o herramientas equivalentes para version de Java, version de build tool y encoding. Use JaCoCo u otro medidor de cobertura, Checkstyle/Spotless/PMD/Error Prone segun el estandar del equipo, y haga que CI publique reportes de falla.

10. Flujo colaborativo.
   Defina ramas, Conventional Commits, checklist de PR, build/test antes de merge, documentacion actualizada y ausencia de secretos en diff. Mantenga `CHANGELOG.md` con SemVer y Keep a Changelog si el proyecto publica versiones.

## Practicas especialmente utiles para AI

- Antes de crear codigo, buscar equivalentes existentes y reutilizar.
- Respetar modulos, paquetes y direcciones de dependencia.
- No introducir Spring, JPA ni HTTP en dominio puro.
- No importar desde capas externas hacia capas internas.
- Crear `Command`, `Result`, `Port`, `Adapter`, `Controller` siguiendo convenciones existentes.
- Tratar campos nullable como `Optional<T>` cuando aplique en dominio/app; no aplicarlo mecanicamente en JPA o serializacion.
- Actualizar OpenAPI, colecciones de API o SDKs si cambia un contrato HTTP consumible.
- Agregar migraciones Flyway/Liquibase cuando cambia el schema.
- Validar paths con `getServletPath()` cuando hay `server.servlet.context-path`.
- Agregar pruebas de regresion cuando se toca seguridad, filtros, multi-tenancy, autorizacion, OAuth/OIDC o persistencia.

## Fortalezas tecnicas esperadas

- Arquitectura hexagonal o por capas claramente marcada.
- Dominio con value objects y validaciones tempranas.
- Casos de uso testeables por puertos.
- Seguridad stateless y method-level cuando corresponde.
- Observabilidad con trace ID simetrico.
- Logging diferenciado por ambiente.
- Configuracion sensible externalizada.
- Docker multi-stage con runtime no-root.
- CI con build, test, cobertura y artefactos de falla.
- Versionamiento documentado con SemVer, Keep a Changelog y Conventional Commits.

## Alertas comunes de mejora

- README o guias internas referencian rutas que no existen.
- La cobertura esperada en documentacion no coincide con la configuracion real de JaCoCo.
- Actuator expone demasiados endpoints en produccion.
- Filtros de rutas publicas crecen sin tests parametrizados.
- Controllers usan repositorios JPA directamente.
- Entidades JPA se filtran hacia API o dominio.
- Se agregan endpoints sin autorizacion ni tests de rechazo.
- Se agregan migraciones sin verificar orden, inmutabilidad y compatibilidad.

## Como usar este documento con AI

Uselo como contexto base antes de pedir implementaciones. La instruccion clave para el agente deberia ser:

```text
Lee esta guia y respeta las practicas del proyecto Spring Boot. Antes de implementar, identifica modulo, capa, patron existente, contratos impactados, tests necesarios, migraciones/documentacion afectadas y riesgos de seguridad. No inventes rutas ni documentos: verifica que existan.
```

# Arquitectura y diseno

## Vision general

Una arquitectura Spring Boot robusta puede organizarse con arquitectura hexagonal, tambien conocida como ports and adapters. La idea es que el dominio y los casos de uso no conozcan detalles externos como HTTP, Spring MVC, JPA, PostgreSQL, JWT, correo, mensajeria, storage o Docker. Esos detalles viven en adaptadores.

El flujo conceptual es:

```text
HTTP/event/job -> filtros/security -> controller/handler -> use case -> port -> adapter -> recurso externo
```

La dependencia principal va desde lo estable hacia lo variable:

```text
<service>-domain <- <service>-app <- <service>-infra
                                  <- <service>-api
                                  <- <service>-persistence
                                                   <- <service>-run
```

Si el proyecto es monolito de un solo modulo, replique esta separacion con paquetes y reglas de imports.

## Modulos

### `<service>-domain`

Responsabilidad:

- Entidades de dominio.
- Agregados.
- Value objects.
- Enums de estado.
- Excepciones de dominio.
- Reglas invariantes del negocio.

Reglas:

- No usar Spring.
- No usar JPA.
- No depender de HTTP.
- No depender de infraestructura.
- Validar invariantes en constructores/factories.
- Exponer comportamiento de dominio como metodos, no como setters anemicos.

Ejemplos transferibles:

- `Order` valida estado, items, totales y permite transiciones `confirm()`, `cancel()` o `ship()`.
- `Slug`, `EmailAddress`, `PhoneNumber`, `Money` o `DocumentNumber` encapsulan formato, longitud y normalizacion.
- `AuthorizationCode` modela OAuth2 Authorization Code con estados, expiracion, uso unico y revocacion cuando el sistema implementa OAuth2.
- `AccountStatus`, `OrderStatus` o `SubscriptionStatus` hacen explicitos los estados permitidos.

Buenas practicas:

- Crear value objects para conceptos con reglas propias.
- Evitar `String` plano cuando el valor tiene significado de negocio.
- Usar enums para estados del dominio.
- Usar excepciones especificas en vez de errores genericos.
- Mantener `toString()` sin secretos ni datos sensibles.

### `<service>-app`

Responsabilidad:

- Casos de uso.
- Commands.
- Results.
- Puertos de salida.
- Orquestacion de reglas entre agregados.
- Abstracciones testeables.

Convenciones:

- Entrada de caso de uso: `FooCommand`.
- Salida de caso de uso: `FooResult`.
- Puerto de salida: `FooPort`.
- Caso de uso: `FooUseCase`.

Ejemplos transferibles:

- `CreateOrderUseCase` valida duplicados o disponibilidad por `OrderRepositoryPort`, crea un agregado valido y persiste por puerto.
- `RegisterCustomerUseCase` valida email unico por `CustomerRepositoryPort`, codifica credenciales por `CredentialEncoderPort` y envia notificacion por `NotificationPort`.
- `ExchangeAuthorizationCodeUseCase` valida codigo, expiracion, cliente, redirect URI y PKCE si el proyecto implementa OAuth2.
- `ClockPort` abstrae el tiempo para testabilidad.
- `TokenSignerPort` abstrae firma JWT.
- `RequestContextHolder` o `ScopeContextHolder` puede usar `ThreadLocal` sin Spring para almacenar contexto de request, siempre con limpieza garantizada.

Buenas practicas:

- Inyectar dependencias por constructor.
- Depender de interfaces, no adaptadores concretos.
- No meter JPA ni HTTP en casos de uso.
- No resolver configuracion Spring dentro del caso de uso salvo que sea pasada como parametro, policy o puerto.
- Los casos de uso deben ser pequenos, orientados a una operacion clara.
- Las reglas tecnicas repetibles deben ir a puertos o servicios de dominio/aplicacion.

### `<service>-infra`

Responsabilidad:

- Implementaciones tecnicas transversales no necesariamente ligadas a JPA.
- JWT signing/verifying.
- JWKS.
- PKCE.
- Email.
- Storage.
- Mensajeria.
- Clientes HTTP.
- Plantillas.

Ejemplos transferibles:

- `RsaJwtTokenSigner`.
- `StandardTokenClaimsFactory`.
- `JwkSetBuilder`.
- `SmtpNotificationAdapter`.
- `S3ObjectStorageAdapter`.
- `RestPaymentGatewayAdapter`.

Buenas practicas:

- Mantener algoritmos y protocolos fuera de controladores.
- Probar criptografia, tokenizacion y clientes externos con tests especificos.
- Usar librerias especializadas para JOSE/JWT, OAuth2, HTTP retry o mensajeria.
- Encapsular detalles de claims, headers, timeouts y serializacion.

### `<service>-api`

Responsabilidad:

- Controllers REST.
- DTOs HTTP.
- OpenAPI.
- Envelope de respuesta si el proyecto lo usa.
- Codigos de respuesta de negocio.
- Manejo de errores.
- Serializacion orientada a contrato.

Reglas recomendadas:

- Endpoints devuelven un formato consistente salvo endpoints nativos de un estandar externo, por ejemplo JWKS, token OAuth2, metadata OIDC, webhooks con contrato de proveedor o health checks.
- Los codigos de respuesta de negocio no deben confundirse con HTTP status codes.
- El envelope, si existe, puede incluir `date`, `success`, `failure`, `data`, `debug` y `throwable`, omitiendo nulos.
- JSON debe tener una estrategia consistente de naming, timezone y manejo de nulos.

Buenas practicas:

- Controllers delegan en casos de uso.
- Controllers no contienen reglas de persistencia.
- Usar DTOs para requests/responses.
- Mantener codigo de respuesta de negocio centralizado.
- Evitar respuestas ad hoc por endpoint.
- Si cambia contrato consumible por UI o clientes, actualizar OpenAPI, colecciones de API, SDKs o documentacion contractual.

### `<service>-persistence`

Responsabilidad:

- Entidades JPA.
- Repositorios Spring Data.
- Adaptadores de persistencia.
- Mappers persistencia-dominio.
- Migraciones Flyway o Liquibase.
- Configuracion de base de datos.

Ejemplos transferibles:

- `FooEntity` usa `@Entity`, `@Table`, indices, `@GeneratedValue`, `@CreationTimestamp`, `@UpdateTimestamp`.
- `FooRepositoryAdapter` implementa `FooRepositoryPort`, usa `FooJpaRepository` y mapper.
- `FooJpaRepository` extiende `JpaRepository` y opcionalmente `JpaSpecificationExecutor`.
- `application-db.yml` usa `ddl-auto=validate`, `open-in-view=false`, `jdbc.time_zone=UTC`, migraciones habilitadas y credenciales por env vars.

Buenas practicas:

- Adaptador implementa puerto, no al reves.
- Mapper separa JPA de dominio.
- Repositorio JPA no debe filtrarse hacia app/domain.
- Usar `Optional` en busquedas.
- Usar `Specification`, Querydsl o queries nombradas para filtros dinamicos.
- Mantener migraciones versionadas e inmutables.
- No usar `ddl-auto=update` en ambientes reales.

### `<service>-run`

Responsabilidad:

- Main app.
- Wiring Spring.
- Configuracion de seguridad.
- Filtros servlet.
- Beans de use cases.
- Properties.
- Configuracion JSON/i18n/logging.

Ejemplos transferibles:

- `ApplicationConfig` crea beans para use cases y adaptadores concretos.
- `SecurityConfig` configura CORS, stateless session, method security, handlers y filtros.
- `RequestTracingFilter`, `LocaleContextFilter`, `ScopeResolutionFilter`, `BearerAuthenticationFilter`.
- `application.yml`, `application-local.yml`, `logback-spring.xml`.

Buenas practicas:

- Ensamblar dependencias en un modulo de composicion.
- Mantener `domain` y `app` libres de Spring cuando se busca hexagonal estricta.
- Registrar filtros con orden explicito cuando importa precedencia.
- Centralizar configuracion con `@ConfigurationProperties`.
- Usar perfiles por ambiente.

## Patrones de diseno recomendados

### Hexagonal Architecture

Separa nucleo de negocio de detalles externos. Es especialmente util para AI porque reduce el espacio de decision: si el cambio es negocio, va a `domain`/`app`; si es persistencia, va a `persistence`; si es HTTP, va a `api`; si es wiring, va a `run`.

### DDD tactico

Use tacticas de DDD cuando el dominio lo justifique:

- Entidades: `Order`, `Customer`, `Account`.
- Value objects: `EmailAddress`, `Money`, `Slug`, IDs.
- Agregados: `Order`, `AuthorizationCode`, `Subscription`.
- Estados: `OrderStatus`, `AccountStatus`.
- Excepciones de dominio.

### Command/Result

Los casos de uso reciben comandos y devuelven resultados. Esto evita firmas largas, ordena contratos internos y facilita pruebas.

### Port/Adapter

Los casos de uso dependen de puertos. Los adaptadores implementan esos puertos con JPA, JWT, email, HTTP, storage, mensajeria u otro detalle tecnico.

### Factory Method

Use factories para crear instancias validas en estado inicial correcto, por ejemplo `Order.created(...)`, `AuthorizationCode.issued(...)` o `Subscription.started(...)`.

### Builder controlado

Lombok `@Builder` puede ser util, pero proteja invariantes con constructores privados, factories o validaciones. Evite builders que permitan objetos imposibles.

### Specification

Use JPA Criteria/Specification, Querydsl o patrones equivalentes para filtros dinamicos sin contaminar casos de uso con detalles SQL/JPA.

### Filter Chain

Los filtros separan preocupaciones cross-cutting: trazabilidad, locale, tenant/organizacion, autenticacion, auditoria y rate limiting.

### Response Envelope

Un envelope de respuesta estandariza exito, falla, debug y data. Si lo usa, defina excepciones claras para endpoints regidos por RFCs o contratos de terceros.

## Reglas de dependencia para agentes AI

1. Si necesitas regla de negocio:
   Modifica `domain` o `app`.

2. Si necesitas un endpoint:
   Modifica `api` y conecta un caso de uso existente o nuevo.

3. Si necesitas persistir algo:
   Crea/ajusta puerto en `app`, adaptador en `persistence`, entity/repository/mapper y migracion.

4. Si necesitas un algoritmo tecnico:
   Usa `infra`.

5. Si necesitas conectar dependencias:
   Usa `run` o el modulo de composicion equivalente.

6. Nunca importes desde `api` hacia `app` o `domain` de forma que el nucleo dependa de HTTP.

7. Nunca hagas que `domain` conozca JPA. La dependencia aceptable es el adaptador dependiendo de dominio y app.

## Antipatrones a evitar

- Controller que usa directamente `JpaRepository`.
- Use case que importa `Entity`.
- Dominio anotado con `@Entity`, `@Service`, `@Component` o `@Transactional`.
- DTO HTTP usado como command de aplicacion sin transformacion clara.
- Validacion de negocio solo en controller.
- Strings sueltos para IDs, slugs, email, scopes o estados con reglas.
- Nuevo endpoint sin codigo de respuesta de negocio cuando el proyecto usa catalogo.
- Nueva migracion sin actualizar documentacion operativa si el repo lo exige.
- Nueva regla de seguridad sin test de regresion.

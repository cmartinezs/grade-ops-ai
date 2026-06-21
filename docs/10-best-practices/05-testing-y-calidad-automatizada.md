# Testing y calidad automatizada

## Estrategia recomendada

Un proyecto Spring Boot debe usar JUnit 5, AssertJ, Mockito, Spring Boot Test y Testcontainers cuando corresponde. La estrategia no se limita a cobertura: prueba reglas de negocio, contratos internos, regresiones de seguridad, limpieza de contexto, adaptadores y migraciones.

## Tipos de tests

### Tests de dominio

Ejemplos:

- `EmailAddressTest`.
- `OrderTest`.
- `MoneyTest`.
- `AuthorizationCodeTest`.

Que validan:

- Creacion con datos validos.
- Rechazo de nulos/blanks.
- Rechazo por longitud.
- Rechazo por formato.
- Cambios de estado.
- Excepciones de dominio.
- Casos permitidos como `id` nulo antes de persistir.

Buenas practicas:

- Testear invariantes del value object.
- Testear comportamiento del agregado.
- Evitar mocks en dominio.
- Mantener tests rapidos.

### Tests de casos de uso

Ejemplo:

- `CreateOrderUseCaseTest`.
- `RegisterCustomerUseCaseTest`.
- `ExchangeAuthorizationCodeUseCaseTest`.

Que validan:

- Uso correcto del puerto.
- Derivaciones o normalizaciones del input.
- No persistir cuando hay duplicado o regla incumplida.
- Excepciones cuando la regla falla.
- Interaccion con repositorios o gateways mockeados.

Buenas practicas:

- Mockear puertos, no detalles JPA.
- Verificar que `save` o acciones externas no se llaman cuando falla validacion.
- Probar happy path y failure paths.
- Usar nombres behavior-driven.

### Tests de API/contrato

Ejemplos:

- `ResponseCodeTest`.
- `FooControllerTest`.
- `GlobalExceptionHandlerTest`.

Que validan:

- Codigos no nulos.
- Mensajes no vacios.
- Codigos unicos.
- Codigos esperados para operaciones clave.
- Validacion de input.
- Status HTTP correcto.
- Estructura JSON estable.

Buenas practicas:

- Probar catalogos de contrato.
- Evitar duplicados de response codes.
- Si se agrega endpoint, agregar response code especifico cuando el proyecto lo usa.
- Si cambia contrato HTTP, agregar test de controller o documentacion OpenAPI/coleccion de API.

### Tests de filtros y seguridad

Ejemplos:

- `BearerAuthenticationFilterTest`.
- `RequestTracingFilterTest`.
- `ScopeResolutionFilterTest`.
- `SecurityConfigTest`.

Que validan:

- Rutas publicas sin autenticacion.
- Rutas protegidas rechazan header ausente.
- Bearer JWT valido.
- Rechazo de JWT sin roles/scopes.
- Roles/scopes como lista y como CSV si ambos estan soportados.
- Rechazo si verifier no existe.
- Rechazo si verificacion falla.
- Error amigable vs tecnico.
- Regresion de `context-path`.
- `X-Trace-ID` generado o reutilizado.
- MDC limpio incluso con excepciones.
- `SecurityContextHolder` limpio en `finally`.
- Metodo/path disponibles durante filter chain.

Buenas practicas:

- Todo filtro con `ThreadLocal`, MDC o SecurityContext debe probar limpieza en `finally`.
- Toda ruta publica nueva debe tener test parametrizado.
- Toda correccion de seguridad debe quedar como regression test.
- Probar errores antes de MVC.

### Tests de adaptadores

Ejemplo:

- `FooRepositoryAdapterTest`.
- `SmtpNotificationAdapterTest`.
- `PaymentGatewayAdapterTest`.

Que validan:

- Adapter delega a repository/client concreto.
- Mapper devuelve dominio.
- `Optional.empty()` cuando no encuentra.
- Errores externos se traducen a excepciones propias.
- Entities o DTOs externos no se filtran hacia app/domain.

Buenas practicas:

- Mockear JPA repository en unit tests de adapter.
- Usar Testcontainers para integracion real con PostgreSQL u otra base cuando se prueba SQL/migraciones.
- Usar fixtures seguros para clientes externos.

### Tests de infraestructura

Ejemplo:

- `RsaJwtTokenSignerTest`.
- `JwkSetBuilderTest`.
- `ObjectStorageAdapterTest`.

Que validan:

- JWT tiene tres partes.
- Header contiene `kid`.
- Header contiene algoritmo esperado.
- Claves generadas dinamicamente para test.
- Claims y expiracion correctos.

Buenas practicas:

- No usar claves reales.
- No hardcodear material sensible.
- Probar estructura del protocolo.
- Probar claims y errores de algoritmo cuando corresponda.

## Herramientas

### JUnit 5

Base de pruebas unitarias.

### AssertJ

Usado para aserciones expresivas:

```java
assertThat(result).isPresent();
assertThatThrownBy(() -> useCase.execute(command))
    .isInstanceOf(DuplicateResourceException.class);
```

### Mockito

Usado para puertos y dependencias:

```java
when(repositoryPort.existsBySlug(any())).thenReturn(true);
verify(repositoryPort, never()).save(any());
```

### Spring Boot Test

Disponible para API/run/persistence cuando se necesita contexto Spring.

### Testcontainers

Recomendado para pruebas reales con PostgreSQL, MySQL, Redis, Kafka u otros servicios externos cuando el comportamiento depende de integracion, SQL, migraciones o configuracion.

## Cobertura

JaCoCo suele configurar:

- `prepare-agent`.
- `report`.
- `check`.
- `report-aggregate` si hay multi-modulo.
- Exclusiones para clases sin logica, como `*Application`, `*Config`, `*Properties`, `*Entity`, `*Exception`, `*Model`, `*Result`, `*Command`, `*Vo`, `*Id`, `*Status`, `*Filter`, segun criterio del equipo.

Alerta:

Puede existir drift entre expectativa documental y configuracion real. Para AI, la regla debe ser: no asumir el umbral, verificar POM/Gradle actual y documentacion vigente antes de cambiar cobertura.

## CI

Un flujo de CI tipico ejecuta:

- Checkout.
- Setup Java con la version del proyecto.
- Cache Maven/Gradle.
- `./mvnw verify --no-transfer-progress` o comando Gradle equivalente.
- Package final.
- Upload de Surefire/Failsafe reports si falla.
- Upload de JaCoCo reports siempre.

Buenas practicas:

- `verify` antes de package final.
- Reportes de falla disponibles como artefactos.
- Cobertura publicada como artefacto.
- Branches y PR targets definidos explicitamente.
- Version Java en CI coincide con Enforcer/toolchain.

## Convenciones de nombres de tests

Estilos recomendados:

- `shouldCreateValidOrder`.
- `shouldRejectDuplicateSlug`.
- `doFilterInternal_shouldAllowPublicPathsWithoutAuth`.
- `givenValidClaims_whenSignJwt_thenProducesThreePartToken`.

Recomendacion:

- Mantener formato legible.
- Usar `@DisplayName` para claridad en reportes.
- Usar comentarios `Given / When / Then` cuando el test tiene varias etapas.

## Matriz minima de tests por tipo de cambio

| Cambio | Tests minimos |
|---|---|
| Nuevo value object | validos, null/blank, limites, formato |
| Nueva entidad de dominio | creacion, invariantes, transiciones de estado, errores |
| Nuevo use case | happy path, cada regla de negocio, puerto no llamado ante error |
| Nuevo endpoint | controller/API contract, response code, validacion input, auth |
| Nueva ruta publica | test en filtro/configuracion de seguridad |
| Cambio JWT/OIDC | firma/verificacion, claims, expiracion, errores |
| Nueva migracion | test de schema o al menos arranque con Flyway/Testcontainers |
| Nuevo adapter JPA | mapping domain/entity, Optional, errores, queries |
| Nuevo filtro | orden, efecto, limpieza en finally, excepciones |
| Cambio de logging/MDC | propagacion y limpieza |

## Reglas para agentes AI

- Nunca agregar funcionalidad sin test cuando toca seguridad, dominio, autorizacion, OAuth/OIDC, dinero, datos sensibles o persistencia.
- Si no se puede correr toda la suite, correr el modulo impactado.
- Si no se pueden correr tests por restricciones externas, declararlo.
- Preferir tests enfocados a snapshots grandes.
- Mantener regresiones para bugs historicos, especialmente `context-path`.
- No perseguir cobertura superficial; cubrir reglas de negocio y caminos de error.

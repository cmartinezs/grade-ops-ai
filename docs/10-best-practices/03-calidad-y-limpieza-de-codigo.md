# Calidad y limpieza de codigo

## Convenciones base

Un proyecto Spring Boot debe definir una base consistente con `.editorconfig`, Checkstyle, Spotless u otra herramienta equivalente:

- UTF-8.
- LF.
- newline final.
- trim de espacios finales.
- Java con indentacion consistente.
- Longitud maxima de linea definida por el equipo.
- XML/YAML/JSON/properties/shell con indentacion consistente.
- Markdown con reglas explicitas para trailing whitespace y longitud.

Estas reglas importan mucho con AI: si el agente no las respeta, genera ruido en diffs y dificulta review.

## Checkstyle o linter equivalente

Una configuracion madura suele validar:

- No tabs si el estandar usa espacios.
- Newline al final.
- Longitud de archivo maxima.
- Longitud de linea.
- No `System.out.println`; usar logger.
- No imports con wildcard.
- No imports redundantes o no usados.
- Convenciones de nombres.
- Metodo con longitud maxima razonable.
- Maximo de parametros.
- Orden de modificadores.
- Braces obligatorios en `if`, `else`, `for`, `while`, `do`.
- Una clase top-level por archivo.
- Checks como `EqualsHashCode`, `SimplifyBooleanExpression`, `StringLiteralEquality`.

Si las violaciones son warnings, el agente AI no debe relajarse: aunque el build no falle, debe escribir codigo que pase las reglas.

## Maven Enforcer o Gradle equivalente

El build debe exigir:

- Version minima de Java.
- Version minima de Maven/Gradle.
- Encoding UTF-8.
- No duplicar versiones de dependencias si el parent/BOM ya las gestiona.

Buenas practicas:

- No hardcodear versiones si Spring Boot parent o BOM ya las gestiona.
- Mantener dependencias en el modulo que realmente las usa.
- Evitar meter dependencias tecnicas en `domain`.
- Evitar dependencias transitivas innecesarias en modulos internos.

## Limpieza por capas

### Dominio

El dominio debe ser expresivo y minimo:

- Value objects para reglas simples pero importantes.
- Constructores/factories con validacion.
- Metodos de comportamiento.
- Excepciones especificas.
- Sin frameworks.
- Sin setters publicos cuando se puede proteger invariantes.

Ejemplo: un `EmailAddress` o `Slug` que valida formato y normalizacion evita que cada controller o use case repita validaciones.

### Aplicacion

La capa app debe ser clara y testeable:

- Un caso de uso por intencion.
- Nombres tipo verbo + objeto: `CreateOrderUseCase`, `RegisterCustomerUseCase`, `ExchangeAuthorizationCodeUseCase`.
- Puertos para dependencias externas.
- Commands/Results para contratos internos.
- Constructor injection.
- `Optional` en busquedas que pueden fallar.
- No side effects ocultos fuera de puertos.

### API

La API debe transformar, no decidir todo:

- Validar input HTTP.
- Convertir request DTO a command.
- Llamar use case.
- Convertir result/domain a response DTO.
- Usar envelope de respuesta si el proyecto lo define.
- Usar codigos de respuesta de negocio si existen.
- Delegar errores a handler/factory.

### Persistencia

La persistencia debe traducir:

- Entity JPA separada del dominio.
- Mapper dedicado.
- Repository adapter implementa puerto.
- JPA repository queda encapsulado.
- `ddl-auto=validate`.
- Flyway/Liquibase como fuente de cambios de schema.

## Nombres

Convenciones recomendadas:

| Concepto | Convencion |
|---|---|
| Entrada de caso de uso | `FooCommand` |
| Salida de caso de uso | `FooResult` |
| Puerto de salida | `FooPort` |
| Implementacion de puerto | `FooAdapter` |
| Adapter de repositorio | `FooRepositoryAdapter` |
| REST controller | `FooController` |

Buenas practicas adicionales:

- Usar nombres de dominio, no nombres tecnicos genericos.
- Evitar `Manager`, `Processor`, `Helper` salvo que tengan una responsabilidad clara.
- Preferir `CredentialEncoderPort` a `PasswordUtils` si la abstraccion es parte del caso de uso.
- Usar `Id`, `Slug`, `Status`, `Type`, `Policy` como value objects/enums cuando tienen reglas.

## Comentarios y JavaDoc

La practica buena no es comentar todo, sino comentar:

- Invariantes.
- Contratos de headers.
- Razones de orden de filtros.
- Reglas de seguridad.
- Diferencias de ambiente.
- Decisiones que evitarian regresiones.
- Semantica de valores nulos permitidos.

Ejemplos:

- Un filtro de trazabilidad documenta el contrato simetrico `X-Trace-ID`.
- Un filtro de locale explica por que debe ejecutarse antes de errores 401/403.
- Un filtro de autenticacion explica uso de `getServletPath()` cuando existe `context-path`.
- Una entity documenta cuando `parentId` o `ownerId` nulo representa un recurso del sistema.

## Manejo de null

Regla recomendada:

- Campos nullable deben declararse como `Optional<T>` cuando aplique en dominio/app.
- Inicializar con `Optional.ofNullable(...)`.
- Convertir a raw value solo en bordes: JPA, HTTP serialization u otra API que lo requiera.

Matiz importante:

- JPA, Jackson y algunos frameworks trabajan mejor con `null` deliberado.
- Un ID puede ser nulo antes de persistir.
- Un campo opcional puede ser nulo por semantica de negocio, por ejemplo `deletedAt`, `parentId` u `ownerId`.

Buena practica para AI:

- No aplicar `Optional` mecanicamente en entidades JPA.
- Mantener `Optional` en contratos de busqueda y dominio/app cuando mejora claridad.
- Documentar semanticamente los `null` permitidos.

## Errores y excepciones

Practicas recomendadas:

- Excepciones especificas de dominio y aplicacion.
- Codigo de respuesta de negocio separado de status HTTP.
- Respuestas de error estructuradas.
- Detalles tecnicos solo en perfiles locales/dev.

Buenas practicas:

- No lanzar `RuntimeException` generico para reglas de negocio.
- No exponer stack traces en produccion.
- Asociar errores a codigos de negocio estables.
- Mantener mensajes utiles para cliente y logs.
- En filtros, escribir JSON consistente si el error ocurre antes de MVC.

## Logging

Practicas recomendadas:

- Usar SLF4J.
- Prohibir `System.out.println`.
- MDC con `traceId`, `scopeId` o identificador organizacional, `userId`, `method`, `path`.
- Logs locales legibles y con DEBUG para codigo propio.
- Logs de staging/prod con niveles restrictivos y salida estructurada.
- Rotacion diaria, compresion, retencion y cap total.

Buenas practicas:

- No loguear tokens, passwords, secrets ni material privado.
- Loguear IDs tecnicos suficientes para correlacion.
- Loguear errores de seguridad sin revelar detalles sensibles al cliente.
- Limpiar MDC en `finally`.

## JSON y serializacion

Configure Jackson de forma centralizada:

- Naming strategy consistente, por ejemplo `snake_case` o `camelCase`.
- `FAIL_ON_UNKNOWN_PROPERTIES` segun tolerancia del contrato.
- Case-insensitive properties solo si el contrato lo permite.
- Omision de nulos cuando aplica.
- Timezone UTC.

Buenas practicas:

- Mantener coherencia entre ambientes.
- No definir naming distinto por controller salvo necesidad fuerte.
- Usar `@JsonProperty` cuando el contrato externo exige nombre exacto.
- En Spring Boot 4/Jackson 3, revisar cambios de paquetes y compatibilidad de annotations.

## Senales de codigo limpio

- Modulos pequenos por responsabilidad.
- Use cases con constructor injection.
- Puertos abstraen infraestructura.
- Value objects validan temprano.
- Tests con nombres behavior-driven.
- Configuracion externalizada.
- Docker no-root.
- CI sube reportes de tests/cobertura.

## Senales a vigilar

- Clases de configuracion muy grandes pueden crecer mucho; dividalas por bounded context o tecnologia.
- Filtros con muchas reglas de path publico requieren tests parametrizados y documentacion.
- Drift entre docs y repo puede inducir a agentes AI a crear archivos en rutas equivocadas.
- Umbrales de cobertura declarados y configurados deben alinearse.

# Automatizaciones, operacion y versionamiento

## Build

Use wrapper del build tool y comandos reproducibles.

Comandos Maven frecuentes:

```bash
./mvnw clean package -DskipTests
./mvnw clean install
./mvnw test
./mvnw -pl <service>-api test
./mvnw test -Dtest=FooControllerTest
./mvnw test -Dtest=FooControllerTest#methodName
./mvnw clean verify
./mvnw checkstyle:check
./mvnw spring-boot:run -pl <service>-run
```

Buenas practicas:

- Usar Maven wrapper o Gradle wrapper, no instalacion global.
- Para cambios acotados, correr modulo afectado.
- Para cierre de PR, correr `clean verify` o equivalente.
- Usar `--no-transfer-progress` en CI Maven.

## GitHub Actions o CI equivalente

Pipeline recomendado:

1. Checkout.
2. Setup JDK con version del proyecto.
3. Cache Maven/Gradle.
4. Build/test completo.
5. Package final.
6. Upload de reportes de tests si falla.
7. Upload de reportes de cobertura siempre.

Buenas practicas:

- CI valida tests y cobertura antes de package final.
- Artefactos ayudan a depurar sin reproducir localmente.
- Java version en CI coincide con Enforcer/toolchain.
- Branches y PR targets estan declarados.

Recomendaciones de mejora:

- Agregar dependency vulnerability scanning.
- Agregar secret scanning/pre-commit si el repo lo permite.
- Alinear cobertura real con cobertura esperada.
- Considerar build de Docker image en PR o release.

## Docker

Un Dockerfile Spring Boot solido aplica:

- Multi-stage build.
- Imagen JDK para builder.
- Imagen JRE o runtime minimo.
- Dependencias Maven/Gradle descargadas antes de copiar source para aprovechar cache.
- JAR layers extraidas si se usa layered jar.
- Labels OCI.
- Usuario no-root.
- Healthcheck.
- Env vars para datasource y servicios externos.
- JVM container-aware.

Buenas practicas:

- Separar build y runtime.
- No ejecutar como root.
- No incluir credenciales en imagen.
- Usar healthcheck.
- Optimizar capas.
- Definir timezone/JVM options de forma consciente.
- Verificar que healthcheck, README y compose usen el mismo `context-path`.

## Configuracion por ambiente

### `application.yml`

Suele contener:

- CORS.
- Info de servicio.
- Public paths.
- Tracing.
- Integraciones externas.
- Mail.
- UI paths.
- Templates.
- SpringDoc/OpenAPI.
- Spring application/profile.
- i18n.
- Actuator.
- Server port/context-path.

Buenas practicas:

- Centralizar bajo un prefijo coherente del producto, por ejemplo `<service>.*`.
- Usar placeholders de env vars.
- Mantener rutas publicas declarativas.
- Separar public paths por prefijo/sufijo/segmento.

### `application-local.yml`

Suele contener:

- Dependencias locales.
- Cache de templates deshabilitado para desarrollo.
- Docker Compose lifecycle para servicios locales.
- Mail local sin auth/starttls.
- Logging mas verboso.

Buenas practicas:

- Desarrollo local rapido.
- Evitar cache cuando se editan templates.
- Levantar dependencias locales automaticamente cuando el stack lo permita.
- No resolver problemas locales cambiando configuracion productiva.

### `application-db.yml` o perfil de persistencia

Suele contener:

- Datasource por env vars.
- Pool configurable.
- Ajustes para proxy/pool externo si aplica.
- `ddl-auto=validate`.
- `open-in-view=false`.
- UTC para JDBC.
- Flyway/Liquibase enabled.
- Limpieza destructiva deshabilitada por defecto.

Buenas practicas:

- No defaults para credenciales.
- Pool configurable.
- SQL validation por migraciones/JPA.
- Evitar Open Session in View.
- Timezone consistente.

## Logging operacional

`logback-spring.xml` o configuracion equivalente debe definir:

- Perfil local/default: consola legible, MDC visible, codigo propio en DEBUG.
- Perfiles staging/prod: consola restrictiva y JSON file rotativo o salida estructurada para plataforma.
- Campos MDC: `traceId`, `scopeId` o identificador organizacional, `userId`, `method`, `path`.
- Rotacion diaria y compresion si se escriben archivos.
- Retencion y cap total.

Buenas practicas:

- Logs estructurados en ambientes reales.
- Correlacion por request.
- Separar ruido local de produccion.
- Configurar `LOG_PATH` o salida stdout segun plataforma.

## Versionamiento

### SemVer

Use Semantic Versioning si el proyecto publica versiones:

- `MAJOR`: cambios incompatibles de API, migraciones destructivas o contratos incompatibles.
- `MINOR`: features compatibles.
- `PATCH`: fixes y seguridad compatibles.

### Keep a Changelog

`CHANGELOG.md` puede usar secciones:

- Added.
- Changed.
- Fixed.
- Security.
- Deprecated.
- Removed.
- Unreleased.

Practicas recomendadas:

- Fecha y fase.
- Modulos afectados.
- Tests agregados.
- Endpoints agregados.
- Migraciones mencionadas.
- Contratos o colecciones de API actualizados.

Buenas practicas:

- Registrar cambios por modulo.
- Mencionar endpoints y response codes.
- Mencionar migraciones Flyway/Liquibase.
- Mencionar numero de tests si aporta contexto.
- Mantener `Unreleased` actualizado.

### Conventional Commits

Tipos frecuentes:

- `feat`: nueva funcionalidad.
- `fix`: bug fix.
- `docs`: documentacion.
- `refactor`: refactorizacion.
- `test`: tests.
- `chore`: mantenimiento.
- `security`: mitigacion o hardening si el equipo lo permite.

Buenas practicas:

- Mensaje corto, imperativo y especifico.
- No mezclar cambios no relacionados.
- Commits pequenos y revisables.

Ejemplos:

```text
feat: add account profile endpoints
fix: validate context-path in authentication filter
docs: update security practices for public paths
test: cover bearer role extraction
refactor: split payment use case wiring
chore: align jacoco threshold documentation
```

## Pull Requests

Checklist recomendado:

- Build pasa.
- Tests pasan.
- Sin secretos en diff.
- Documentacion actualizada si cambiaron APIs o configuracion.
- Descripcion incluye que cambio, como se probo, riesgos/tradeoffs.

Buenas practicas para AI:

- Generar resumen de cambios por modulo.
- Declarar pruebas ejecutadas.
- Declarar riesgos.
- Declarar si no se pudo correr algo.
- Incluir impacto en OpenAPI, colecciones de API, migraciones y configuracion.

## Documentacion

Documentos recomendados:

- README raiz orienta rapidamente.
- CONTRIBUTING define flujo de colaboracion.
- SECURITY define reporte y tratamiento de vulnerabilidades.
- CODE_OF_CONDUCT si aplica al proyecto.
- CHANGELOG registra cambios publicables.
- Guia para agentes AI si el equipo usa asistentes.
- Instrucciones de Copilot/Codex/Claude si existen.

Regla para AI:

- Verificar documentos antes de usarlos como fuente de verdad.
- Si una referencia falta, reportar drift y no inventar contenido.
- No crear documentacion tecnica en rutas prohibidas por las reglas del repo.

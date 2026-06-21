# Guia para desarrollo asistido por AI

Esta guia convierte las practicas recomendadas en instrucciones operativas para usar Codex, Claude Code, Copilot Agent u otro asistente en proyectos Spring Boot.

## Principio general

La AI debe actuar como un desarrollador que respeta el sistema existente. Antes de escribir codigo debe entender modulo, capa, patron, contrato, tests y riesgos.

## Workflow obligatorio recomendado

### 1. Orientacion

Antes de implementar:

- Leer `README.md`.
- Leer guias para agentes si existen, por ejemplo `AGENTS.md`, `CLAUDE.md`, `.github/copilot-instructions.md` o equivalentes.
- Revisar POM/Gradle raiz y build del modulo impactado.
- Buscar una implementacion equivalente.
- Buscar tests equivalentes.
- Identificar bounded context reales del proyecto, por ejemplo `auth`, `customer`, `order`, `payment`, `inventory`, `notification`, `reporting`.

Prompt sugerido:

```text
Antes de implementar, revisa el codigo existente y dime:
1. modulo(s) impactado(s),
2. patron existente que vas a seguir,
3. archivos analogos encontrados,
4. tests existentes que usaras como referencia,
5. riesgos de seguridad/contrato/migracion.
No escribas codigo hasta terminar este analisis.
```

### 2. Planificacion

Si el repositorio exige planning o RFC para trabajo no trivial, respetelo antes de implementar.

Buenas practicas:

- Crear plan con problema, solucion, modulos, patrones, pasos, tests y estado.
- Para cambios grandes, crear RFC.
- Esperar aprobacion explicita cuando el flujo del equipo lo exige.

Prompt sugerido:

```text
Crea un plan de implementacion para esta tarea en formato:
- Problema
- Alcance
- Modulos impactados
- Diseno propuesto
- Patrones existentes a reutilizar
- Cambios por archivo
- Tests requeridos
- Documentacion/contratos/migraciones
- Riesgos
No implementes todavia.
```

### 3. Implementacion

Reglas:

- No Spring en dominio puro.
- No JPA/HTTP en casos de uso.
- No dependencias hacia atras.
- Seguir Command/Result/Port/Adapter si el proyecto usa ese patron.
- Reutilizar helpers existentes.
- No crear abstracciones nuevas si no reducen complejidad.
- No introducir secretos.
- No modificar docs generados.
- No inventar rutas/documentos.

### 4. Verificacion

Segun cambio:

- `./mvnw -pl <modulo> test`
- `./mvnw test`
- `./mvnw clean verify`
- `./mvnw checkstyle:check`
- Comandos Gradle equivalentes si el proyecto usa Gradle.

Si toca seguridad:

- Ejecutar tests de filtros/security.
- Agregar regression test.

Si toca persistencia:

- Ejecutar modulo de persistencia.
- Revisar Flyway/Liquibase.
- Considerar Testcontainers.

Si toca API:

- Ejecutar tests del modulo API.
- Actualizar OpenAPI, colecciones de API o SDKs si corresponde.

### 5. Cierre

Respuesta final del agente:

- Que cambio.
- Donde cambio.
- Que tests corrio.
- Que riesgos quedan.
- Que documentacion/contrato/migracion actualizo.

## Mapa de decision por tipo de solicitud

### Nueva regla de negocio

Ubicacion probable:

- `domain` si es invariante de entidad/value object.
- `app` si es flujo de caso de uso.

Preguntas:

- Es regla intrinseca del concepto?
- Debe validarse siempre?
- Necesita persistencia externa?
- Ya existe value object similar?

Tests:

- Dominio unitario.
- Use case unitario.

### Nuevo endpoint

Ubicacion:

- `api`: controller, request/response DTO.
- `app`: command/result/use case si no existe.
- `run`: bean wiring.
- `persistence`: adapter si necesita datos.

Checklist:

- Codigo de respuesta nuevo o existente si el proyecto usa catalogo.
- Envelope salvo endpoint RFC/contrato externo.
- Validaciones.
- `@PreAuthorize` o equivalente.
- OpenAPI/coleccion de API/SDK.
- Tests.

### Nueva persistencia

Ubicacion:

- `app`: puerto.
- `persistence`: entity, repository, mapper, adapter, migration.
- `run`: wiring si aplica.

Checklist:

- No exponer entity fuera de persistence.
- Evitar `@Data` en entities si genera equals/hashCode/toString riesgosos.
- ID generado por JPA cuando corresponde.
- `ddl-auto=validate`.
- Migracion `V<N>__description.sql` o formato Liquibase equivalente.
- Actualizar documentacion operativa si la regla vigente lo exige.

### Nueva integracion tecnica

Ubicacion:

- `infra` para adapter tecnico.
- `app` para puerto.
- `run` para bean/configuracion.

Checklist:

- El caso de uso depende del puerto.
- Adapter no filtra detalles al dominio.
- Secrets por env vars.
- Timeouts/retries definidos.
- Tests con mocks o fixtures seguros.

### Cambio de seguridad

Ubicacion:

- Configuracion de Spring Security.
- Filtros.
- Controllers si cambia method security.
- App/domain si cambia regla de autorizacion de negocio.

Checklist:

- Test de happy path.
- Test de rechazo.
- Test de context-path.
- Test de limpieza.
- No exponer detalles tecnicos.
- No debilitar public paths.

## Reglas de prompts para agentes

### Prompt base recomendado

```text
Trabaja en este proyecto Spring Boot respetando estas reglas:
- Arquitectura hexagonal o por capas segun el repo.
- No Spring/JPA/HTTP en dominio puro.
- La capa app contiene use cases, commands, results y ports cuando el patron existe.
- La capa API contiene controllers, DTOs, envelope y response codes si el proyecto los usa.
- La capa persistence contiene JPA, mappers, adapters y migraciones.
- La capa run/composition contiene wiring, config, security y filters.
- Reutiliza patrones existentes antes de crear nuevos.
- Si cambia endpoint, revisa OpenAPI/colecciones de API.
- Si cambia schema, agrega migracion y actualiza documentacion relacionada.
- Si cambia seguridad, agrega tests de regresion.
- No inventes documentos/rutas: verifica existencia.
```

### Prompt para revisar una propuesta de AI

```text
Revisa esta propuesta como tech lead de un proyecto Spring Boot.
Busca:
- violaciones de capas,
- imports en direccion incorrecta,
- Spring en domain,
- entities filtradas hacia app/api,
- falta de response code si el proyecto usa catalogo,
- falta de envelope si el proyecto lo exige,
- falta de @PreAuthorize o policy,
- secretos hardcodeados,
- falta de tests,
- migraciones/documentacion faltantes,
- drift con guias internas.
Devuelve hallazgos ordenados por severidad.
```

### Prompt para generar tests

```text
Genera tests siguiendo el estilo existente del proyecto.
Usa JUnit 5, AssertJ y Mockito cuando corresponda.
Cubre:
- happy path,
- validaciones,
- errores de negocio,
- que no se llame al puerto cuando falla,
- regresion si corrige bug,
- limpieza de MDC/SecurityContext/ThreadLocal si aplica.
```

### Prompt para cambios de seguridad

```text
Este cambio toca seguridad. Antes de implementar:
1. identifica rutas publicas y protegidas,
2. revisa filtros y SecurityConfig,
3. revisa @PreAuthorize o policy del endpoint,
4. agrega tests de rechazo y exito,
5. valida context-path con getServletPath(),
6. asegura limpieza de SecurityContextHolder y MDC.
```

## Reglas anti-alucinacion

1. Si un documento referenciado no existe, reportarlo.
2. Si no se puede listar el repo, trabajar con archivos verificados y decir que el analisis se basa en ellos.
3. No crear rutas de documentacion prohibidas por guias internas.
4. No asumir umbrales de cobertura: leer POM/Gradle.
5. No asumir migracion siguiente: revisar migraciones existentes.
6. No asumir endpoint publico: revisar properties/configuracion y tests.
7. No asumir rama principal: verificar metadata del repo.

## Senales de respuesta buena de AI

Una respuesta buena:

- Nombra modulos impactados.
- Explica por que el cambio va en esa capa.
- Reutiliza nombres existentes.
- Incluye tests especificos.
- Menciona documentacion/contratos.
- Declara riesgos.
- No sobreexplica lo obvio.

Una respuesta riesgosa:

- Crea un servicio Spring en `domain`.
- Usa JPA repository desde controller.
- Devuelve JSON ad hoc.
- Agrega endpoint sin codigo de respuesta en un proyecto que exige catalogo.
- Agrega public path sin test.
- Mete password en `application.yml`.
- Ignora `context-path`.
- Cambia muchas cosas no relacionadas.

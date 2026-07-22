# Estado actual y brechas

## Evidencia comprobada

### `web/`

- Next.js 15, React 19 y TypeScript.
- Jest 29, `jsdom`, Testing Library y mocks locales de `firebase/app` y `firebase/auth`.
- Existen pruebas de páginas de autenticación, dashboard, componentes y `apiClient`.
- `npm test` solo ejecuta `jest --passWithNoTests`.
- No se observó configuración de coverage, Playwright, pruebas de aceptación black-box, contratos ni workflow propio de CI para `web/`.

### `api/`

- Spring Boot/Java 21 con JUnit, Mockito, Spring Security Test, H2, Testcontainers PostgreSQL y ArchUnit.
- Existen pruebas unitarias, web/controller, seguridad, persistencia e integración de flujos de assessment.
- `application-test.yml` usa H2 y desactiva Flyway; algunas pruebas de persistencia usan PostgreSQL mediante Testcontainers.
- El `pom.xml` no configura JaCoCo ni gates de cobertura.
- `api/compose.yml` levanta únicamente PostgreSQL 16.
- El workflow actual ejecuta `./mvnw test`, pero no publica resultados ni cobertura.

### `agents/`

- Spring Boot/Java 21, JUnit/Mockito y pruebas de comandos, handlers, orquestador, selección de proveedor, templates y adaptadores Gemini/Groq.
- El perfil de test desactiva autoconfiguraciones GenAI para mantener `contextLoads` hermético.
- Existe una verificación manual para Groq; no debe formar parte del gate automatizado.
- No se observó JaCoCo, aceptación HTTP black-box, contratos, Testcontainers ni Compose propio.
- El workflow actual ejecuta `./mvnw test`, pero no publica artefactos.

### Transversal

- No existe Compose raíz que levante `web + api + agents`.
- No existen simuladores compartidos/versionados para Firebase, Agents API, GradeOps API y GenAI.
- No hay pruebas automáticas de los journeys solicitados.
- No existe nomenclatura ni esquema común de artefactos de prueba.
- Los workflows de `api/` y `agents/` están orientados a `master`, mientras el baseline revisado está en `develop`; la estrategia de ramas debe resolverse explícitamente.

## Brechas principales

1. Las pruebas internas no validan que la imagen desplegable arranque y respete su contrato HTTP.
2. H2 puede ocultar diferencias de PostgreSQL/Flyway.
3. No hay detección automática de incompatibilidades entre DTOs de Web, API y Agents.
4. Falta aislamiento fuerte frente a Firebase/GenAI en suites automáticas.
5. Falta evidencia histórica comparable de calidad, duración, flakiness y coverage.
6. Falta una política de promoción distinta para `beta` y `demo`.

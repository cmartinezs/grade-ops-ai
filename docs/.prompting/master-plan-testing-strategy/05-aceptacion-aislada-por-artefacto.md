# Aceptación aislada por artefacto

## Definición

Una prueba de aceptación levanta la misma imagen que se desplegará, accede únicamente por su interfaz pública y reemplaza todos sus externos por simuladores. Debe validar health/readiness, happy paths, errores, seguridad, timeouts y compatibilidad del contrato.

## `web/` acceptance

Topología:

```text
Playwright → Web real → API mock
                    → Firebase mock/emulator controlado
```

Alcance mínimo:

- login/registro/recuperación sin servicios reales;
- navegación protegida y expiración de sesión;
- listado/creación de assessment contra respuestas API deterministas;
- estados loading, empty, error, retry y unauthorized;
- no exposición de secretos ni mezcla de URL de ambiente;
- accesibilidad básica y capturas solo al fallar.

El guard de cliente se valida por UX; la autorización real sigue siendo responsabilidad de API.

## `api/` acceptance

Topología:

```text
Runner HTTP → API real → PostgreSQL real y migrado
                       → Firebase verifier mock
                       → Agents mock
                       → email/object storage mocks
```

El mock de Firebase debe ser un adapter de prueba activado solo por perfil, capaz de emitir identidades/roles deterministas. No se recomienda emular criptografía del Admin SDK dentro de cada escenario. Debe existir un fail-fast que impida activar ese adapter fuera de test.

Validar como mínimo: autenticación/autorización, ownership, idempotencia, persistencia, errores de Agents, timeout/retry y auditoría.

## `agents/` acceptance

Topología:

```text
Runner HTTP → Agents real → GenAI mock
```

Validar:

- autenticación interna y capabilities;
- selección permitida de proveedor/modelo;
- render de prompt y contrato de salida;
- respuesta GenAI inválida, timeout, rate limit y error;
- retry/fallback acotado;
- correlación, uso y error seguro;
- prompt injection fixture sin ejecutar herramientas/capacidades no permitidas.

## Herramienta de ejecución

- Playwright para Web y journeys con navegador.
- REST Assured/Newman/Karate para fronteras HTTP Java; elegir una sola convención transversal.
- WireMock o MockServer para APIs HTTP simuladas; evitar mocks ad hoc embebidos en scripts.

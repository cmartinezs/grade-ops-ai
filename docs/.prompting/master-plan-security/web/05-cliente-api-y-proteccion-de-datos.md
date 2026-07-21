# Cliente API y protección de datos

## Cliente recomendado

Centralizar:

- Base URL permitida por ambiente.
- Inyección del token solo hacia el origen API esperado.
- `AbortController` y timeout por operación.
- Parseo uniforme de `application/problem+json` o contrato equivalente.
- `X-Correlation-ID` para soporte.
- `Idempotency-Key` para comandos GenAI/mutantes.
- Sin retries automáticos para mutaciones salvo idempotencia confirmada.

## Semántica de errores

| Estado | Comportamiento UI |
|---|---|
| `401` | Reautenticar/cerrar sesión sin bucle de redirects |
| `403` | Ocultar acción y mostrar acceso insuficiente |
| `404` | Mensaje neutro; no revelar ownership ajeno |
| `409/422` | Refrescar estado y explicar regla funcional |
| `429` | Respetar backoff y cuota |

## Datos sensibles

- No enviar prompts, respuestas, tokens o PII a analytics, error trackers o `console`.
- Cachear datos por UID/organización y vaciar al cambiar sesión.
- Evitar `dangerouslySetInnerHTML`; si se incorpora Markdown generado por IA, sanitizar con allowlist y bloquear HTML activo, URLs peligrosas y handlers.
- Descargas deben usar URLs firmadas cortas y nombres seguros; no confiar en MIME declarado por el cliente.
- Formularios validan UX con Zod, pero la validación real permanece en API.

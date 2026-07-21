# Headers y amenazas del navegador

## Headers objetivo

- CSP con `default-src 'self'`, allowlists explícitas para Firebase/Google y política gradual sin `unsafe-eval` en producción.
- `Strict-Transport-Security` en producción HTTPS.
- `X-Content-Type-Options: nosniff`.
- `Referrer-Policy: strict-origin-when-cross-origin` o más restrictiva para accesos Student.
- `Permissions-Policy` deshabilitando capacidades no usadas.
- `frame-ancestors 'none'` dentro de CSP, salvo necesidad documentada.
- Mantener COOP compatible con popup Google y probar el flujo.

## Amenazas prioritarias

| Amenaza | Control principal |
|---|---|
| XSS roba capacidad de sesión | CSP, render seguro, dependencias, no HTML sin sanitizar |
| IDOR manipulando IDs | Ownership en API; UI no es control |
| Token Student filtrado | Canje, referrer restrictivo, no analytics/logs |
| Clickjacking en aprobación | `frame-ancestors`, confirmación y API |
| Doble ejecución GenAI | Idempotency key y estado de operación |
| Dependencia comprometida | lockfile, auditoría, actualizaciones y CI |

Las variables `NEXT_PUBLIC_*` son públicas por diseño. Ningún secreto de agentes, service account o API interna puede usar ese prefijo.

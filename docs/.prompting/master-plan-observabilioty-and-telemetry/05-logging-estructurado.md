# Logging estructurado

## Objetivo

Los logs deben permitir diagnóstico, no reconstruir todo el dominio ni almacenar contenido sensible.

## Configuración por entorno

| Entorno | Salida |
|---|---|
| Local | Consola legible; archivo JSON opcional |
| Test | Captura controlada y silenciosa salvo fallo |
| Demo GCP | JSON estructurado a `stdout/stderr`, ingerido por Cloud Logging |
| Beta Render | JSON estructurado a `stdout/stderr`, ingerido o drenado desde Render |
| Beta Vercel | Logs estructurados compatibles con el runtime; captura de errores frontend/server-side sin secretos |

En Cloud Run y Render, eliminar la dependencia operativa de `RollingFileAppender`. Los límites de retención de las plataformas no convierten sus logs en evidencia durable.

## Eventos técnicos mínimos

- Request iniciado/terminado solo cuando entregue valor adicional a la telemetría automática.
- Autenticación rechazada y autorización denegada con razón normalizada.
- Dependencia externa lenta/no disponible.
- Operación/run/attempt cambia de estado.
- Reintento, timeout, cancelación, presupuesto excedido.
- Output de IA inválido o rechazado por políticas.
- Error no controlado con stack trace server-side y referencia pública segura.

## Prohibiciones

No registrar:

- Firebase ID tokens, API keys, secretos o cookies.
- Prompts completos, submissions, feedback o respuestas del estudiante.
- Bodies HTTP por defecto.
- Correos, nombres o identificadores estudiantiles en claro.
- URLs firmadas completas.
- Stack traces entregados al navegador.

## Eventos de seguridad

Los eventos de auditoría sensibles deben persistirse como registros inmutables o append-only en la API; el log técnico es una copia de diagnóstico, no la fuente canónica.

## Normalización

- `event_name` estable.
- `error.code` controlado por enum/taxonomía.
- Mensaje humano separado de campos consultables.
- Stack trace únicamente para errores inesperados.
- Sampling para INFO de alto volumen; nunca muestrear errores críticos ni auditoría durable.

# Flujos por actor

## Teacher

Login → verificación Firebase → `/api/v1/me` → capabilities → workspace. Cada lectura y comando recibe token; la API filtra por Teacher UID. Aprobar, publicar, regenerar y calificar requieren confirmación visible, idempotencia y manejo de estado obsoleto.

## Operator

Login → sesión Operator → `/operator/**`. Provisionamiento, flags y evidencia usan el actor autenticado; la UI nunca envía un `setBy` autoritativo. Acciones sensibles deben mostrar impacto, pedir confirmación y reflejar audit ID.

## Student

Abrir enlace → validar/canjear token → sesión acotada → cargar solo evaluación publicada → enviar respuesta con prevención de duplicado → recibo. No debe existir navegación al portal docente ni reutilización del token para listar recursos.

## Matriz mínima

| Acción | Teacher | Operator | Student |
|---|---:|---:|---:|
| Gestionar assessment propio | Sí | No | No |
| Revisar sugerencia de nota | Sí | No por defecto | No |
| Provisionar Teacher | No | Sí | No |
| Responder invitación | No | No | Solo token scoped |
| Ver resultado | Teacher propio | Agregado si procede | Solo publicado y scoped |

Esta matriz guía navegación y tests; la matriz canónica de autorización vive en `api/`.

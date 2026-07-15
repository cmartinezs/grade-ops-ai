# 12 — Observabilidad, errores y telemetría

## 1. Objetivo

La UI debe ayudar a entender:

- Qué falló.
- En qué pantalla.
- Qué acción intentó el usuario.
- Si el error fue recuperable.
- Si existe trace/correlation ID del backend.
- Qué impacto tuvo en el flujo.

No se trata de llenar consola; se trata de diagnosticar sin exponer datos sensibles.

## 2. Errores visibles

Cada error visible debe tener:

- Título o mensaje breve.
- Descripción accionable.
- Retry si aplica.
- Ruta alternativa si aplica.
- Código o trace ID solo si ayuda a soporte.

Ejemplo:

```text
No pudimos cargar las evaluaciones.
Intenta nuevamente. Si el problema continúa, contacta soporte con el código GO-1234.
```

## 3. Errores técnicos

Los errores técnicos deben normalizarse antes de mostrarse.

No mostrar:

- Stack traces.
- JSON crudo.
- Mensajes de Firebase sin traducción.
- Errores SQL/backend internos.
- Payloads sensibles.

## 4. Boundaries

Agregar error boundaries cuando:

- Una zona compleja puede fallar sin romper toda la app.
- Hay visualización de datos compleja.
- Hay render de contenido externo.
- Hay componentes de IA con output variable.

Un error boundary debe ofrecer recuperación o redirección.

## 5. Logging cliente

Si se agrega logging cliente, registrar:

- Nombre de pantalla.
- Acción.
- Tipo de error.
- Status HTTP.
- Código backend.
- Trace ID.
- Build/version si está disponible.

No registrar:

- Token.
- Payload completo.
- Datos de estudiantes.
- Feedback generado.
- Emails sin necesidad.

## 6. Métricas UX

Eventos útiles:

- Inicio de creación de evaluación.
- Evaluación creada.
- Feedback aprobado.
- Feedback editado.
- Error en generación AI.
- Retry después de error.
- Sesión expirada.

Cada evento debe tener propósito de producto o soporte. No instrumentar todo por defecto.

## 7. Correlation ID

Si el backend expone correlation/trace ID:

- Capturarlo en `apiClient`.
- Asociarlo al error normalizado.
- Mostrarlo en errores persistentes si ayuda a soporte.
- No inventar IDs locales como si fueran backend trace.

## 8. Performance

Observar:

- Tiempo hasta primera UI útil.
- Tiempo de carga de listas.
- Latencia de submit.
- Bloqueos por bundle grande.
- Re-render innecesario en listas grandes.

Optimizar después de medir o cuando el problema sea evidente.

## 9. Console

No dejar `console.log` en código de producción.

Se permite temporalmente durante desarrollo, pero debe removerse antes de merge. Para errores esperados, usar mecanismo de logging si existe.

## 10. Fallbacks

Una pantalla con datos remotos debe tener:

- Fallback de loading.
- Fallback de error.
- Fallback de empty.
- Fallback de permiso.

Si el fallback no existe, el usuario queda sin contexto.

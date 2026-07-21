# Pruebas, observabilidad y aceptación

## Pruebas unitarias/integración

- AuthGuard: anónimo, email no verificado, Google, cuenta API deshabilitada.
- Menús: capacidades Teacher/Operator sin asumir que ocultar autoriza.
- apiClient: token, expiración, `403/404/409/429`, timeout e idempotencia.
- Sanitización de Markdown/feedback malicioso.
- Cambio de usuario sin fuga de caché.

## E2E negativos

- Navegar manualmente a ruta Operator como Teacher.
- Invocar endpoint ajeno desde DevTools.
- Alterar role/capabilities en memoria.
- Reusar invitación expirada/revocada.
- Reenviar dos veces un comando.
- Abrir dos pestañas y cerrar sesión en una.

## Observabilidad segura

Registrar route template, status, duración, correlation ID y categoría de error. No registrar URL completa con tokens, Authorization, formularios, respuestas estudiantiles ni prompts.

## Criterios de aceptación transversales

1. Manipular la UI nunca concede una operación denegada por API.
2. Ningún token aparece en URL de analytics, logs o error tracking.
3. Toda ruta sensible tiene pruebas por actor permitido y denegado.
4. Headers se verifican sobre el deployment real, no solo en configuración local.
5. El cambio de sesión elimina datos y cachés del actor anterior.

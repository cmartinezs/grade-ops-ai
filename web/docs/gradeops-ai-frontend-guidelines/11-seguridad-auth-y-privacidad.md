# 11 — Seguridad, auth y privacidad

## 1. Regla principal

El frontend no es frontera de seguridad. Es una experiencia autenticada que debe comportarse correctamente, pero la autorización real vive en el backend.

## 2. Firebase Auth

La UI puede:

- Iniciar sesión.
- Registrar usuario.
- Leer usuario actual.
- Obtener ID token.
- Redirigir si no hay sesión.
- Mostrar estado de email no verificado.
- Cerrar sesión.

La UI no debe:

- Confiar en claims para autorizar recursos sin backend.
- Guardar tokens manualmente en storage.
- Exponer configuración privada.
- Saltarse validación server-side.

## 3. Tokens

Reglas:

- El ID token se envía como `Authorization: Bearer <token>`.
- El `apiClient` centraliza esta lógica.
- No loguear tokens.
- No guardar tokens en logs, errores o analytics.
- No pasar tokens como query params.

## 4. Variables públicas

Variables `NEXT_PUBLIC_*` pueden llegar al browser. Solo deben contener configuración pública:

- Firebase API key pública.
- Auth domain.
- Project ID.
- App ID.
- Sender ID.
- API base URL pública.

Nunca poner secrets reales con prefijo `NEXT_PUBLIC_`.

## 5. Datos sensibles

Tratar como sensibles:

- Datos de estudiantes.
- Entregas.
- Calificaciones.
- Feedback.
- Rúbricas privadas.
- Prompts internos.
- Outputs AI no aprobados.
- Tokens.
- Emails.
- IDs externos si permiten correlación.

No imprimir datos sensibles en consola ni errores visibles.

## 6. Autorización visual

La UI puede ocultar o deshabilitar acciones para mejorar UX, pero siempre debe asumir que el backend puede rechazar.

Ejemplo:

- Si `canApprove=false`, ocultar botón o mostrarlo disabled con explicación.
- Si usuario fuerza request, backend responde 403.
- La UI muestra "No tienes permisos para aprobar esta evaluación."

## 7. Sesión expirada

Cuando API responde 401:

- Si es email no verificado, redirigir a verificación.
- Si es sesión expirada, cerrar sesión y volver a login.
- Mantener mensaje humano.

El usuario no debe quedar en pantalla rota.

## 8. Acciones sensibles

Requieren confirmación o revisión:

- Publicar feedback a estudiantes.
- Aprobar calificación.
- Eliminar evaluación.
- Consumir créditos en lote.
- Ejecutar generación AI masiva.
- Exportar datos sensibles.

## 9. Archivos

Si se agregan uploads:

- Validar tipo y tamaño en UI.
- Validar nuevamente en backend.
- Mostrar progreso.
- Manejar error.
- No confiar en extensión.
- Evitar preview inseguro de HTML.

## 10. XSS y contenido generado

No renderizar HTML recibido del backend o IA con `dangerouslySetInnerHTML` salvo revisión explícita y sanitización robusta.

Preferir renderizar texto plano, Markdown sanitizado o componentes estructurados.

## 11. Links externos

Links externos deben usar:

```tsx
target="_blank"
rel="noopener noreferrer"
```

No abrir links generados por usuario sin validación si pueden ser maliciosos.

## 12. Privacidad en analytics

Si se agrega telemetría:

- No enviar nombres de estudiantes.
- No enviar texto de entregas.
- No enviar feedback completo.
- Usar IDs internos no reversibles cuando sea posible.
- Documentar eventos.

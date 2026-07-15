# 07 — Formularios, validación y feedback

## 1. Stack recomendado

Para formularios usar:

- React Hook Form para estado del formulario.
- Zod para schema y validación de formato.
- Componentes DS para inputs, labels y botones.
- API client para submit.

## 2. Responsabilidad de la validación frontend

La UI valida:

- Campos requeridos.
- Formato de email.
- Longitud mínima/máxima.
- Rangos obvios.
- Tipos de archivo.
- Tamaño de archivo si aplica.
- Consistencia local entre campos.

La UI no reemplaza:

- Reglas de dominio.
- Permisos.
- Estado actual del recurso.
- Validación multi-tenant.
- Consumo de créditos.

El backend debe validar todo nuevamente.

## 3. Schema Zod

Mantener schemas cerca del formulario si son específicos:

```ts
const createAssessmentSchema = z.object({
  title: z.string().min(1, "Ingresa un título.").max(120, "Usa máximo 120 caracteres."),
  courseId: z.string().min(1, "Selecciona un curso."),
});
```

Extraer a archivo propio si:

- Se comparte entre varios formularios.
- Es largo.
- Tiene transforms/refinements importantes.
- Necesita tests unitarios.

## 4. Mensajes de error

Los mensajes deben:

- Decir qué corregir.
- Estar junto al campo.
- Ser específicos.
- Estar en español.
- No exponer detalles técnicos.

Preferir:

- "Ingresa una dirección de correo válida."
- "Selecciona una fecha de cierre."
- "El archivo debe pesar menos de 10 MB."

Evitar:

- "Invalid input"
- "Required"
- "Bad request"
- "ZodError"

## 5. Submit

Todo submit debe:

- Deshabilitar botón principal mientras procesa.
- Mostrar estado loading.
- Evitar doble submit.
- Manejar error.
- Llevar al siguiente estado en éxito.
- Preservar input del usuario si falla.

Ejemplo:

```tsx
<Button type="submit" loading={form.formState.isSubmitting} block>
  {form.formState.isSubmitting ? "Guardando..." : "Guardar evaluación"}
</Button>
```

## 6. Errores de servidor

Los errores de servidor pueden ser:

- Globales del formulario.
- Asociados a campo.
- Asociados a conflicto de estado.
- Bloqueantes de permisos.

Si backend devuelve errores por campo, mapearlos al form state. Si devuelve error general, mostrar `role="alert"` sobre el submit o en una zona clara.

## 7. Formularios multi-step

Un multi-step debe definir:

- Pasos.
- Validación por paso.
- Qué datos se preservan.
- Navegación atrás/siguiente.
- Guardado parcial si aplica.
- Estado de progreso.
- Confirmación final.

No esconder errores de pasos anteriores sin indicación.

## 8. Drafts

Para flujos largos como evaluaciones o rúbricas:

- Considerar guardado como borrador.
- Mostrar última actualización.
- Evitar pérdida de datos al navegar.
- Confirmar salida con cambios sin guardar.
- Manejar conflictos si otro usuario actualizó.

## 9. Campos complejos

Para campos como rúbricas, criterios o feedback:

- Usar componentes especializados.
- Validar cada ítem.
- Permitir agregar/quitar con controles claros.
- Mantener foco después de agregar.
- Evitar formularios gigantes sin agrupación.

## 10. Feedback de éxito

El éxito debe ser proporcional:

- Redirect para creación completada.
- Toast o inline success para guardado menor.
- Estado persistente para aprobación.
- Confirmación explícita para publicación.

No mostrar toasts para todo si la pantalla ya refleja el cambio claramente.

## 11. Accesibilidad en formularios

Cada campo debe tener:

- `label` asociado con `htmlFor`.
- Error anunciado con `role="alert"` o `aria-describedby`.
- Focus visible.
- Tab order lógico.
- Botón submit accesible.

No usar placeholder como único label.

## 12. Confirmaciones

Pedir confirmación cuando:

- Se elimina información.
- Se publica feedback a estudiantes.
- Se aprueba resultado AI sensible.
- Se consume crédito de forma importante.
- No hay undo.

No pedir confirmación para acciones triviales y reversibles.

# 15 — Contratos Backend/Frontend

## 1. Regla principal

El backend define reglas de negocio y contratos de datos. El frontend define experiencia, presentación e interacción.

La frontera debe ser explícita:

- DTOs.
- Estados.
- Códigos de error.
- Permisos.
- Paginación.
- Ordenamiento.
- Validaciones.
- Side effects.

## 2. DTOs compartidos por contrato

Cuando el backend expone:

```json
{
  "id": "asm_123",
  "title": "Evaluación 1",
  "status": "GRADING"
}
```

El frontend debe reflejar:

```ts
export interface AssessmentSummaryDto {
  id: string;
  title: string;
  status: AssessmentStatus;
}
```

No renombrar campos en DTO si representan contrato real. Si la UI necesita otro nombre, usar view model.

## 3. Estados

Los estados backend deben mapearse explícitamente:

```ts
const STATUS_LABELS: Record<AssessmentStatus, string> = {
  DRAFT: "Borrador",
  OPEN: "Abierta",
  GRADING: "En corrección",
  CLOSED: "Cerrada",
};
```

No mostrar enums crudos al usuario salvo en herramientas internas de debug.

## 4. Errores

El backend debe idealmente responder errores estructurados:

```json
{
  "code": "ASSESSMENT_ALREADY_CLOSED",
  "message": "Assessment is already closed",
  "traceId": "..."
}
```

El frontend debe traducir:

```text
Esta evaluación ya está cerrada. No puedes modificar sus entregas.
```

El código técnico puede usarse para soporte, no como microcopy principal.

## 5. Validación

Validación frontend:

- Rápida.
- Cercana al campo.
- Formato y consistencia local.

Validación backend:

- Autoritativa.
- Dominio.
- Permisos.
- Estado del recurso.
- Tenant.
- Reglas críticas.

Si ambas existen, deben estar alineadas. No permitir que frontend acepte algo que backend rechaza por regla conocida y estable.

## 6. Permisos

Si backend entrega permisos/capabilities:

```json
{
  "canEdit": true,
  "canApprove": false,
  "canPublish": false
}
```

La UI puede:

- Mostrar acciones permitidas.
- Deshabilitar acciones no permitidas con explicación.
- Ocultar acciones irrelevantes.

Pero backend debe seguir validando cada comando.

## 7. Paginación y filtros

Para listas grandes, acordar:

- Page/size o cursor.
- Orden default.
- Filtros permitidos.
- Búsqueda.
- Total count si es necesario.
- Estado de filtros inválidos.

El frontend no debe descargar todo para filtrar si la lista puede crecer mucho.

## 8. Fechas y zona horaria

Contratos deben definir:

- Formato ISO.
- Zona horaria.
- Si la fecha representa instante o fecha local.
- Cómo mostrar vencimientos.

La UI debe formatear para el usuario, no mostrar ISO crudo salvo debug.

## 9. Números, porcentajes y moneda

Definir:

- Unidad.
- Precisión.
- Redondeo.
- Locale.
- Moneda si aplica.

No calcular de forma distinta en frontend y backend para métricas críticas.

## 10. AI outputs

Para salidas AI, el contrato debe ser estructurado:

- Texto generado.
- Versión de prompt si aplica.
- Modelo/proveedor si se muestra o audita.
- Estado de revisión humana.
- Fecha de generación.
- Ediciones humanas.
- Estado de publicación.

El frontend debe diferenciar visualmente:

- Generado por IA.
- Editado por docente.
- Aprobado.
- Publicado.

## 11. Evolución de contrato

Cuando backend cambia contrato:

- Actualizar tipos frontend.
- Actualizar mappers/view models.
- Actualizar tests.
- Actualizar documentación si cambia comportamiento.
- Coordinar despliegue si hay breaking change.

Cuando frontend necesita contrato nuevo:

- Documentar campos necesarios.
- Indicar estados de UI.
- Explicar por qué el dato es necesario.
- Proponer shape pero no asumirlo definitivo sin backend.

## 12. Anti-patterns

Evitar:

- DTO duplicado con campos renombrados sin mapper.
- UI que depende de strings de error en inglés.
- Estados backend mostrados como enums crudos.
- Frontend calculando permisos reales.
- Backend devolviendo blobs ambiguos para flujos críticos.
- Mocks que se vuelven contrato accidental.

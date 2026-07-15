# 10 — Testing, calidad y automatización

## 1. Objetivo

Las pruebas frontend deben demostrar que la UI permite completar tareas reales:

- Renderiza información correcta.
- Responde a interacción.
- Valida input.
- Maneja loading/error/empty.
- Invoca servicios esperados.
- Protege contra regresiones.

No deben centrarse en detalles internos de implementación.

## 2. Stack

Baseline del repo:

- Jest.
- Testing Library.
- `@testing-library/user-event`.
- Mocks de Firebase.
- TypeScript.

## 3. Pirámide frontend

Prioridad:

1. Tests unitarios de utilidades puras.
2. Tests de componentes con Testing Library.
3. Tests de páginas con servicios mockeados.
4. Tests de integración de flujos críticos.
5. E2E solo para flujos de alto valor cuando exista infraestructura estable.

## 4. Qué testear en componentes

Testear:

- Texto visible.
- Roles accesibles.
- Estados de carga.
- Errores.
- Clicks.
- Inputs.
- Disabled/loading.
- Navegación o callback.

Evitar testear:

- Nombres de clases internos.
- Estructura exacta de divs.
- Estado privado.
- Implementación del hook si el comportamiento ya está cubierto.

## 5. Queries

Preferir queries accesibles:

```ts
screen.getByRole("button", { name: /crear evaluación/i });
screen.getByLabelText(/correo electrónico/i);
screen.getByText(/no hay evaluaciones/i);
```

Evitar `getByTestId` salvo que no exista alternativa semántica razonable.

## 6. Formularios

Tests mínimos:

- Render inicial.
- Validación requerida.
- Error de formato.
- Submit exitoso.
- Error de servidor.
- Botón loading/deshabilitado.

## 7. API mocks

Mockear funciones de `lib/api`, no `fetch` directo en cada test, cuando la feature usa servicios.

```ts
jest.mock("@/lib/api/assessments", () => ({
  getAssessments: jest.fn(),
}));
```

Esto mantiene los tests cerca del contrato de la app.

## 8. Firebase mocks

La autenticación debe mockearse desde `src/test/__mocks__/firebase`. No inicializar Firebase real en tests unitarios.

Tests de auth deben cubrir:

- Usuario autenticado.
- Usuario no autenticado.
- Email no verificado.
- Sign out.
- Error de proveedor.

## 9. Loading, empty y error

Toda página que fetch debe tener tests para:

- Loading inicial.
- Datos renderizados.
- Empty state.
- Error state.

Si falta un estado, el test debe hacerlo visible.

## 10. Accessibility smoke

En tests de componentes:

- Usar roles y labels.
- Verificar `aria-label` en icon buttons.
- Verificar alerts para errores.
- No depender solo de snapshots.

Snapshots grandes tienden a ocultar problemas. Usarlos con moderación.

## 11. Quality gates

Antes de merge:

```bash
npm run lint
npm run test
npm run build
```

Si un comando no puede ejecutarse localmente por entorno, documentar la razón en el PR.

## 12. Regresiones

Todo bug corregido debe agregar test si:

- El bug era reproducible.
- El flujo es crítico.
- La corrección toca lógica.
- El error puede reaparecer sin señal visual obvia.

## 13. Datos de prueba

Los fixtures deben cubrir:

- Datos normales.
- Datos largos.
- Datos vacíos.
- Estados distintos.
- Fechas y cantidades.
- Errores de permisos.

No usar solo un fixture feliz para todo.

## 14. Tests y diseño

Una UI fácil de testear suele tener:

- Componentes pequeños.
- Hooks claros.
- Servicios aislados.
- Roles accesibles.
- Estados explícitos.

Si una pantalla es difícil de testear, probablemente está demasiado acoplada.

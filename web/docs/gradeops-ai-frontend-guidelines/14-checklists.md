# 14 — Checklists

## Checklist de nueva feature frontend

### Diseño

- [ ] La feature tiene objetivo de usuario claro.
- [ ] Se identificó ruta o zona de UI.
- [ ] Se definió flujo principal.
- [ ] Se identificaron estados loading/empty/error/sin permisos.
- [ ] Existe wireframe textual o visual.
- [ ] Existe maqueta funcional si el flujo es nuevo o complejo.
- [ ] Se definió acción primaria.
- [ ] Se validó responsive esperado.

### Arquitectura

- [ ] La `Page` compone y no concentra toda la lógica.
- [ ] Páginas, layouts, componentes y tests React usan TSX.
- [ ] No se agregan componentes `.jsx` nuevos al código productivo.
- [ ] Código JSX importado desde maquetas/UI kits se migró a TSX antes de usarse en producción.
- [ ] Existen `Section`/`SubSection` cuando la pantalla lo requiere.
- [ ] Componentes no triviales tienen hook propio.
- [ ] La lógica de API está en `lib/api`.
- [ ] DTOs viven en `types` o feature.
- [ ] View models separan contrato backend de presentación.
- [ ] No se agregó estado global innecesario.

### Design System

- [ ] Usa componentes DS existentes cuando aplica.
- [ ] Usa tokens semánticos.
- [ ] No introduce colores arbitrarios.
- [ ] Estados hover/focus/disabled/loading están cubiertos.
- [ ] No hay cards anidadas sin necesidad.
- [ ] La UI mantiene densidad adecuada para producto operativo.
- [ ] Icon buttons tienen `aria-label`.

### UX

- [ ] La acción primaria es clara.
- [ ] Acciones destructivas tienen confirmación.
- [ ] Errores son humanos y accionables.
- [ ] Empty state explica siguiente paso.
- [ ] Loading no deja pantalla en blanco.
- [ ] Textos largos no rompen layout.
- [ ] Datos fake cubren casos reales si hay maqueta.

### Formularios

- [ ] Usa React Hook Form si el formulario no es trivial.
- [ ] Usa Zod para validación.
- [ ] Cada campo tiene label.
- [ ] Errores aparecen junto al campo.
- [ ] Submit deshabilita doble envío.
- [ ] Error de servidor se muestra sin perder datos.
- [ ] Validación frontend no reemplaza backend.

### API y datos

- [ ] No hay `fetch` directo en componentes.
- [ ] Pantallas con dos o más fuentes remotas usan `Page Data Loader` o `Screen Data Facade`.
- [ ] La página recibe view models ya compuestos, no una colección de DTOs crudos.
- [ ] Llamadas API independientes se orquestan en paralelo cuando corresponde.
- [ ] 401/403/404/409/422 tienen tratamiento razonable.
- [ ] Mutaciones tienen loading, success y error.
- [ ] Después de mutación crítica se sincroniza con backend.
- [ ] No hay optimistic UI en acciones sensibles sin rollback.
- [ ] Mocks temporales están aislados y nombrados.

### Accesibilidad

- [ ] Se usan elementos semánticos.
- [ ] La pantalla funciona con teclado.
- [ ] Focus visible.
- [ ] Contraste suficiente.
- [ ] Formularios tienen labels.
- [ ] Errores usan `role="alert"` cuando corresponde.
- [ ] No se usa color como única señal.

### Seguridad y privacidad

- [ ] No se loguean tokens ni datos sensibles.
- [ ] No se guardan tokens manualmente.
- [ ] No hay secrets en `NEXT_PUBLIC_*`.
- [ ] No se renderiza HTML externo sin sanitizar.
- [ ] Autorización real queda en backend.
- [ ] Acciones sensibles tienen confirmación/revisión.

### Testing

- [ ] Tests de render principal.
- [ ] Tests de interacción crítica.
- [ ] Tests de loading.
- [ ] Tests de empty.
- [ ] Tests de error.
- [ ] Tests de validación si hay formulario.
- [ ] Tests de permisos visuales si aplica.
- [ ] Mocks de API/Firebase correctos.

## Checklist de componente

- [ ] Tiene nombre claro.
- [ ] Props expresan negocio.
- [ ] No recibe props ambiguas o contradictorias.
- [ ] Usa hook propio si tiene lógica no trivial.
- [ ] No hace fetch directo.
- [ ] No conoce detalles de rutas si no es navegación.
- [ ] Tiene estados visuales completos.
- [ ] Es accesible.
- [ ] Tiene test si su comportamiento es relevante.

## Checklist de Pull Request

- [ ] El PR tiene descripción clara.
- [ ] El cambio corresponde al scope declarado.
- [ ] No mezcla refactor grande con feature grande sin necesidad.
- [ ] Usa patrones existentes del repo.
- [ ] No agrega dependencias nuevas sin justificar.
- [ ] No rompe Design System.
- [ ] No deja mocks hardcoded en flujo real.
- [ ] No deja `console.log`.
- [ ] No deja TODO crítico sin issue.
- [ ] Incluye actualización de docs si cambia patrón o contrato.
- [ ] `npm run lint` ejecutado o razón documentada.
- [ ] `npm run test` ejecutado o razón documentada.
- [ ] `npm run build` ejecutado o razón documentada.

## Checklist de revisión UI/UX

- [ ] La pantalla responde a una tarea real.
- [ ] La jerarquía visual es clara.
- [ ] La acción primaria no compite con acciones secundarias.
- [ ] La densidad es adecuada.
- [ ] Los textos ayudan a decidir.
- [ ] Los estados alternativos están diseñados.
- [ ] Mobile no queda roto.
- [ ] El usuario entiende qué pasó después de cada acción.

## Checklist de release frontend

- [ ] Build productivo exitoso.
- [ ] Tests relevantes pasan.
- [ ] Variables de entorno documentadas.
- [ ] Rutas protegidas verificadas.
- [ ] Auth y sesión expirada verificadas.
- [ ] Contratos API sincronizados.
- [ ] Estados de error no exponen datos sensibles.
- [ ] Rollback considerado.
- [ ] Cambios breaking comunicados.

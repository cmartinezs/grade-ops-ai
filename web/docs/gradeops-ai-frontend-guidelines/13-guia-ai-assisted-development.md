# 13 — Guía de AI-assisted development

## 1. Regla principal

Un agente AI puede acelerar implementación, pero no debe reemplazar decisiones de producto, UX, seguridad ni revisión humana.

Toda contribución AI debe respetar:

- Stack real del repo.
- Design System existente.
- Jerarquía de componentes.
- Hooks por lógica no trivial.
- Accesibilidad.
- Tests.
- Contratos backend.

## 2. Antes de pedir código

El prompt debe incluir:

- Objetivo de usuario.
- Ruta afectada.
- Componentes existentes relevantes.
- Contratos API.
- Estados de UI.
- Restricciones visuales.
- Tests esperados.

Evitar prompts vagos como:

```text
Haz una pantalla bonita de evaluaciones.
```

Preferir:

```text
Implementa /assessments como pantalla protegida para docentes.
Debe usar AppShell, Button/Card/Badge del DS, hook useAssessmentListPage,
estados loading/empty/error, y consumir getAssessments().
```

## 3. AI no debe inventar el sistema visual

El agente debe revisar:

- `src/components/ds`
- `src/styles/ds-tokens`
- `src/components/shell`
- Páginas existentes

Antes de crear estilos nuevos.

## 4. AI debe proponer jerarquía

Para features medianas, pedir:

- Layout involucrado.
- Page.
- Sections.
- Components.
- Hooks.
- Tipos.
- Tests.

La propuesta debe ser concreta, no arquitectura abstracta.

## 5. AI y maquetas funcionales

Cuando backend no existe, AI puede crear maqueta funcional con:

- Mocks explícitos.
- Datos realistas.
- Estados simulables.
- Componentes cercanos a producción.

Debe quedar claro qué es mock y qué es integración real.

## 6. AI y accesibilidad

El agente debe cuidar:

- Roles.
- Labels.
- `aria-label`.
- Focus visible.
- Botones semánticos.
- Contraste.
- Teclado.

No aceptar UI generada que solo funcione visualmente.

## 7. AI y tests

Para cada cambio funcional, pedir tests:

- Render.
- Interacción.
- Validación.
- Loading/empty/error.
- Error de API.

El agente debe ejecutar `npm run test` y `npm run build` cuando sea razonable.

## 8. AI y contratos backend

El agente no debe inventar endpoints como definitivos.

Debe:

- Revisar `src/lib/api`.
- Revisar `src/types`.
- Revisar documentación backend si existe.
- Mantener nombres alineados.
- Documentar supuestos si backend no está listo.

## 9. AI y seguridad

Prohibido aceptar código AI que:

- Guarde tokens en localStorage sin decisión explícita.
- Use secrets en frontend.
- Renderice HTML externo sin sanitización.
- Confíe en UI para autorización real.
- Loguee datos sensibles.
- Ignore 401/403.

## 10. AI y revisión

Todo PR asistido por AI debe revisarse con foco en:

- ¿Respeta el DS?
- ¿La UX resuelve el flujo?
- ¿Hay estados completos?
- ¿La lógica está en hooks?
- ¿Hay pruebas?
- ¿No inventó contratos?
- ¿No introdujo dependencias innecesarias?

## 11. Prompt operativo recomendado

```text
Contexto:
- Repo: grade-ops-ai-web.
- Stack: Next.js App Router, React, TS, Firebase, Jest, Testing Library.
- Usar DS en src/components/ds y tokens en src/styles/ds-tokens.

Tarea:
- Implementar [feature/ruta].

Reglas:
- Page compone.
- Componentes no triviales tienen hook propio.
- Incluir loading/empty/error.
- No fetch directo fuera de lib/api.
- No inventar colores fuera del DS.
- Incluir tests de comportamiento.

Entrega:
- Archivos modificados.
- Comandos ejecutados.
- Riesgos o supuestos.
```

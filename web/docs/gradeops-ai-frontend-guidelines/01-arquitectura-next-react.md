# 01 — Arquitectura Next.js y React

## 1. Estructura base del proyecto

La estructura recomendada para `grade-ops-ai-web` es:

```text
src/
  app/
    layout.tsx
    globals.css
    login/page.tsx
    register/page.tsx
    (protected)/
      layout.tsx
      dashboard/page.tsx
  components/
    auth/
    brand/
    dashboard/
    ds/
    shell/
  features/
    assessment/
      components/
      hooks/
      mocks/
      types.ts
  hooks/
  lib/
    api/
    firebase/
  styles/
    ds-tokens/
  test/
  types/
```

El repo actual ya usa `app`, `components`, `lib`, `styles`, `test` y `types`. Para features nuevas de mayor tamaño se recomienda agregar `src/features/<feature>/` para evitar que `components/` se convierta en una carpeta plana sin contexto.

## 2. Responsabilidad de `app/`

`src/app` debe contener rutas, layouts y composición de alto nivel.

Una `page.tsx` puede:

- Definir la entrada de una ruta.
- Invocar hooks de feature.
- Configurar el shell.
- Componer sections.
- Manejar redirecciones propias de la ruta.
- Definir metadata si aplica.

Una `page.tsx` no debe:

- Implementar formularios complejos completos.
- Contener mapeos extensos de DTO a vista.
- Contener estilos largos repetidos.
- Implementar componentes reusables internos de muchas líneas.
- Hablar directamente con Firebase o `fetch` si ya existe un servicio.

## 3. Responsabilidad de `components/`

`components/` contiene piezas compartidas o de una zona visible existente.

Convención recomendada:

```text
components/
  ds/              # Design System base
  shell/           # App shell, navegación, layout persistente
  auth/            # Componentes de autenticación
  brand/           # Logo, marca, identidad
  dashboard/       # Componentes del dashboard actual
```

Si una feature crece, mover sus componentes a `features/<feature>/components` y dejar en `components/` solo lo realmente compartido.

## 4. Responsabilidad de `features/`

Usar `features/<feature>/` cuando exista una capacidad de negocio con varias piezas:

```text
features/assessments/
  components/
    AssessmentListSection.tsx
    AssessmentFilters.tsx
    AssessmentStatusBadge.tsx
  hooks/
    useAssessmentList.ts
    useAssessmentFilters.ts
  mocks/
    assessmentList.mock.ts
  types.ts
  index.ts
```

Una feature puede contener:

- Componentes específicos.
- Hooks específicos.
- Mocks de maqueta funcional.
- Adaptadores de vista.
- Tipos de UI derivados del contrato backend.

Una feature no debe duplicar el API client global ni redefinir DTOs backend incompatibles.

## 5. Responsabilidad de `lib/`

`lib/` contiene integración técnica:

- `lib/api`: cliente HTTP y funciones por recurso.
- `lib/firebase`: inicialización Firebase.
- `lib/format`: formateadores compartidos si se agregan.
- `lib/errors`: normalización de errores si se agrega.

Regla: código en `lib/` no debe importar componentes React. Puede importar tipos, constantes y utilidades puras.

## 6. Server Components y Client Components

En este repo las rutas autenticadas son client-driven por Firebase. Aun así, usar `"use client"` con intención.

Debe ser client component si:

- Usa `useState`, `useEffect`, `useMemo`, `useForm` o hooks.
- Accede a Firebase Auth cliente.
- Usa eventos del usuario.
- Lee `window`, `localStorage`, `document` o media queries del navegador.

Puede ser server component si:

- Renderiza contenido estático.
- No necesita interacción.
- No usa APIs del navegador.
- Puede recibir props ya resueltas.

No marcar todo como client por costumbre. Cada `"use client"` aumenta el bundle y obliga a pensar en hidratación.

## 7. Providers

Los providers deben ubicarse cerca del scope que necesitan:

- Auth global: layout protegido.
- Shell config: layout protegido.
- Theme/DS global: root layout o CSS global.
- Estado de feature: dentro de la feature, no global.

Evitar providers globales para estado local de una pantalla.

## 8. Imports

Usar aliases del proyecto:

```ts
import { Button } from "@/components/ds";
import { getAssessments } from "@/lib/api/assessments";
import type { AssessmentSummaryDto } from "@/types/assessment";
```

Evitar rutas relativas profundas:

```ts
import Button from "../../../components/ds/Button";
```

## 9. Barriles (`index.ts`)

Usar `index.ts` cuando simplifique importaciones estables:

- `components/ds/index.ts`
- `features/<feature>/index.ts`

No usar barriles para ocultar dependencias circulares ni para exportar todo sin criterio.

## 10. Regla de crecimiento

Cuando una página supere aproximadamente estos umbrales, dividir:

- Más de 150 líneas de JSX.
- Más de 5 estados locales.
- Más de 3 efectos.
- Más de 4 handlers.
- Más de 2 responsabilidades visibles.
- Un formulario con validación y submit.

La división recomendada es:

1. Extraer hook de lógica.
2. Extraer sections.
3. Extraer componentes visuales.
4. Extraer mapeadores o formateadores puros.

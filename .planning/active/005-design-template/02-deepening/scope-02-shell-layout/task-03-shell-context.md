# ⚛️ TASK 03 — ShellContext: contexto de título/subtítulo/acciones del topbar

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-02-shell-layout.md)

---

## Objective

Implementar un React Context (`ShellContext`) que permita a cualquier página dentro del layout `(protected)` configurar el título, subtítulo y acciones del topbar sin prop drilling, usando un hook `useShellConfig()` para setear la configuración desde cada página.

---

## Technical Design

- **Approach:** Context API de React con un Provider que vive en el layout `(protected)`. Las páginas llaman `useShellConfig({ title, subtitle, actions })` en un `useEffect` para setear la config del topbar. El `AppShell` (task-04) lee el contexto para renderizar el topbar. Este approach evita que el layout tenga que conocer el estado de cada página.

- **Affected files / components:**
  - `web/src/components/shell/ShellContext.tsx` (nuevo — Context + Provider + hooks)

- **Interfaces / contracts:**
  ```tsx
  interface ShellConfig {
    title: string;
    subtitle?: string;
    actions?: React.ReactNode;
  }

  // Context value:
  interface ShellContextValue {
    config: ShellConfig;
    setConfig: (config: ShellConfig) => void;
  }

  // Hook para páginas:
  function useShellConfig(config: ShellConfig): void

  // Hook para el shell (leer la config):
  function useShell(): ShellContextValue
  ```

- **Design notes:**
  - `useShellConfig` usa `useEffect` + `setConfig` para evitar render loops. El efecto se dispara solo cuando cambia el título (comparación por referencia de `config`).
  - El Provider tiene un default config: `{ title: "GradeOps AI", subtitle: undefined, actions: undefined }`.
  - Si una página no llama `useShellConfig`, el topbar muestra el default ("GradeOps AI").
  - El contexto es client-side only (`"use client"`) — el layout `(protected)` ya es client porque envuelve AuthGuard.

---

## Implementation Steps

1. Crear `web/src/components/shell/ShellContext.tsx`:

   ```tsx
   "use client";
   import React, { createContext, useContext, useState, useEffect } from "react";

   interface ShellConfig {
     title: string;
     subtitle?: string;
     actions?: React.ReactNode;
   }

   interface ShellContextValue {
     config: ShellConfig;
     setConfig: (c: ShellConfig) => void;
   }

   const DEFAULT: ShellConfig = { title: "GradeOps AI" };

   const ShellContext = createContext<ShellContextValue>({
     config: DEFAULT,
     setConfig: () => {},
   });

   export function ShellProvider({ children }: { children: React.ReactNode }) {
     const [config, setConfig] = useState<ShellConfig>(DEFAULT);
     return (
       <ShellContext.Provider value={{ config, setConfig }}>
         {children}
       </ShellContext.Provider>
     );
   }

   // Para el AppShell — leer la config
   export function useShell() {
     return useContext(ShellContext);
   }

   // Para las páginas — setear la config
   export function useShellConfig(config: ShellConfig) {
     const { setConfig } = useContext(ShellContext);
     useEffect(() => {
       setConfig(config);
       // eslint-disable-next-line react-hooks/exhaustive-deps
     }, [config.title]);
   }
   ```

2. Verificar que el contexto no genera errores de hidratación (es client-side).

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | Página llama `useShellConfig({ title: "Panel" })` → topbar muestra "Panel" | Título correcto en el topbar | Integración en dashboard |
| 2 | Página sin `useShellConfig` → topbar muestra "GradeOps AI" | Título default | Ruta sin config |
| 3 | Navegar entre dos páginas → título del topbar cambia | Sin render loops | Navegación manual |

---

## Done Criteria

- [x] `web/src/components/shell/ShellContext.tsx` existe con `ShellProvider`, `useShell`, `useShellConfig`
- [x] `useShellConfig` acepta `title`, `subtitle?`, `actions?`
- [x] El contexto tiene un valor default ("GradeOps AI")
- [x] TypeScript sin errores
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-02-shell-layout.md)

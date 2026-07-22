# Resumen ejecutivo

GradeOps necesita una estrategia de testing en capas, reproducible localmente y automatizable en CI. El objetivo no es ejecutar “todo contra todo” en cada cambio, sino detectar cada defecto en la capa más rápida y determinista posible.

## Capas objetivo

| Nivel | Dependencias | Propósito | Ejecución |
|---|---|---|---|
| Unitario/componente | En memoria | Reglas, UI, handlers, prompts y adaptadores | Cada PR |
| Aceptación de artefacto | Todos los externos simulados | Validar la frontera pública del contenedor | Cada PR relevante |
| Contrato | Consumidor/proveedor sin despliegue completo | Evitar incompatibilidades Web–API–Agents | Cada PR relevante |
| Integración Compose | Servicios GradeOps reales; Firebase/GenAI simulados | Validar comunicación y configuración | Cada PR transversal y nightly |
| Funcional local real | Stack local + Firebase/GenAI aislados reales | Exploración y smoke real controlado | Manual explícito |
| Beta | Vercel + Render + Neon | Validación continua pre-demo | Post-deploy |
| Demo | GCP | Evidencia y estabilidad del ambiente demostrable | Promoción/post-deploy |

## Decisión central

Se recomienda un único sistema de Compose en la raíz, compuesto mediante archivos y perfiles, en vez de varios stacks duplicados:

- `compose.base.yml`: red, PostgreSQL, healthchecks y convenciones.
- `compose.acceptance.yml`: un artefacto bajo prueba y sus simuladores.
- `compose.integration.yml`: Web/API/Agents reales con Firebase y GenAI simulados.
- `compose.functional.yml`: stack completo con integraciones externas reales.

Los escenarios `api → agents`, `web → api` y `web → api → agents` deben ser perfiles o targets del mismo Compose de integración para evitar divergencia.

## Principio de seguridad

La suite hermética nunca consume Firebase ni GenAI reales. El modo funcional real debe requerir una acción explícita y usar:

- otro Firebase Project ID;
- otra aplicación web Firebase;
- otra service account de Firebase Admin;
- otra API key/proyecto GenAI;
- cuotas y presupuesto limitados;
- datos sintéticos y desechables.

## Resultado esperado

Cada ejecución debe producir resultados legibles por humanos y máquinas: JUnit XML, cobertura XML/LCOV y HTML, reportes Playwright, logs de Compose, manifiesto del entorno, resultados de contratos y un resumen unificado. Estos artefactos permiten construir después un dashboard sin acoplar la estrategia a GitHub Actions.

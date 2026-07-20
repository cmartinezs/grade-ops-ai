# Fase 04 — Planificación de releases

Actúa como Product Strategist, Release Manager y Software Architect de GradeOps AI.

## Objetivo

Transformar los análisis estabilizados en una secuencia de releases verticales, incrementales, funcionales, desplegables y demostrables.

Esta fase crea la estrategia y el esqueleto ejecutivo. No debe generar todavía el detalle completo de cada release.

## Entradas obligatorias

- `@docs/`
- Todos los archivos de `@docs/master-plan/analysis/` generados en las fases 01 a 03.
- `master-plan-specification.md`, si está disponible.

## Precondición

No continuar si existen decisiones bloqueantes sin resolver que impidan definir el MVP o el flujo crítico.

## Archivos que puede crear

- `@docs/master-plan/analysis/release-strategy.md`
- `@docs/master-plan/master-plan-executive.md`
- `@docs/master-plan/README.md`

## Archivos que puede modificar

- Únicamente los tres archivos anteriores.

## Archivos que no puede modificar

- User stories originales.
- Diagnósticos anteriores.
- Inventarios anteriores.
- Código.
- Archivos individuales de releases.

## Trabajo requerido

### Priorización

Utiliza criterios explícitos:

- Valor para el usuario.
- Valor comercial.
- Impacto validacion MVP.
- Aprendizaje.
- Reducción de riesgo.
- Dependencias.
- Esfuerzo.
- Urgencia.
- Evidencia.
- Potencial de automatización.
- Costo operacional.
- Reversibilidad.

### Diseño de releases

Para cada release define en forma resumida:

- Número.
- Nombre.
- Objetivo.
- Problema.
- Hipótesis.
- Actor beneficiado.
- Valor.
- Flujo vertical.
- Capacidades.
- US incluidas.
- US propuestas requeridas.
- Exclusiones.
- Automatizaciones.
- Nivel de automatización.
- Evidencias.
- Métricas.
- Dependencias.
- Riesgos.
- Complejidad S/M/L/XL.
- Estado.

Una release XL debe dividirse.

### Camino crítico

Identifica:

- Dependencias bloqueantes.
- Decisiones costosas o irreversibles.
- Integraciones críticas.
- Pruebas técnicas tempranas.
- Funcionalidades para pilotos.
- Funcionalidades para cobrar.
- Funcionalidades para evidenciar agentes.
- Funcionalidades para la presentación.

### Hitos

Adaptar según las fuentes:

- Primer flujo end-to-end.
- Primera llamada real de IA.
- Primera revisión docente.
- Primera evaluación real.
- Primer estudiante procesado.
- Primer piloto.
- Primer cliente pagado.
- Primera automatización supervisada.
- Primer reporte de impacto.
- Primera evidencia completa.
- Release candidata validacion MVP.
- Versión productiva inicial.

## Contenido de `release-strategy.md`

- Criterios de priorización.
- Scoring o justificación.
- Secuencia de releases.
- Dependencias.
- Camino crítico.
- Complejidad.
- Hitos.
- Riesgos de secuencia.
- Diferencia entre MVP, validacion MVP y roadmap.
- Historial de cambios.

## Contenido de `master-plan-executive.md`

- Resumen ejecutivo.
- Estado documental.
- Supuestos y decisiones.
- Objetivos estratégicos.
- Mapa de capacidades resumido.
- Flujo crítico.
- Diagnóstico global de US.
- Automatización resumida.
- Priorización.
- Resumen de releases.
- Camino crítico.
- Hitos.
- Matriz de trazabilidad global.
- Riesgos.
- Métricas y evidencias.
- Siguiente acción.

## Contenido de `README.md`

- Propósito.
- Alcance.
- Estado.
- Convenciones.
- Orden de lectura.
- Tabla de releases.
- Enlaces relativos.
- Leyenda de estados.
- Última actualización.
- Reglas de mantenimiento.

## Criterios de finalización

- Toda US priorizada pertenece a una release.
- No hay duplicidades.
- No hay dependencias circulares.
- Cada release es vertical y demostrable.
- Las releases XL fueron divididas.
- El MVP no depende del roadmap posterior.
- El README y el ejecutivo son coherentes.
- Se identifica claramente la primera release.

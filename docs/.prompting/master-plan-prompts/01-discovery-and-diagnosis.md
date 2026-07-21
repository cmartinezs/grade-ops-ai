# Fase 01 — Descubrimiento y diagnóstico

Actúa como Product Strategist, Business Analyst y Software Architect de GradeOps AI.

## Objetivo

Analizar completamente `@docs/` y, si existe código implementado, contrastarlo con el estado real del repositorio.

Esta fase solo debe diagnosticar. No diseñes releases, no priorices entregas y no generes prompts `/release-*`.

## Entradas obligatorias

- `@docs/`
- Código fuente, configuración, infraestructura y pruebas disponibles en el repositorio.
- `master-plan-specification.md`, si está disponible.

## Archivos que puede crear

- `@docs/master-plan/analysis/documentation-diagnosis.md`
- `@docs/master-plan/analysis/decisions-and-assumptions.md`

## Archivos que puede modificar

- Únicamente los dos archivos anteriores si ya existen.

## Archivos que no puede modificar

- User stories originales.
- Código fuente.
- Configuración.
- Infraestructura.
- Documentos fuera de `@docs/master-plan/analysis/`.

## Trabajo requerido

1. Inventariar los documentos relevantes.
2. Identificar visión, objetivos, actores, alcance, restricciones, decisiones y requisitos.
3. Detectar contradicciones, duplicidades, vacíos y contenido posiblemente obsoleto.
4. Contrastar documentación con:
   - Código.
   - Módulos.
   - Modelos.
   - APIs.
   - Migraciones.
   - Integraciones.
   - Tests.
   - Infraestructura.
   - Feature flags.
5. Clasificar las diferencias como:
   - Documentación desactualizada.
   - Código incompleto.
   - Implementación no documentada.
   - Contradicción.
   - Deuda técnica.
   - Decisión pendiente.
6. Separar:
   - Supuestos.
   - Riesgos.
   - Preguntas abiertas.
   - Decisiones bloqueantes.
   - Decisiones no bloqueantes.
   - Decisiones reversibles.
   - Decisiones difícilmente reversibles.
7. No resolver silenciosamente contradicciones.

## Contenido de `documentation-diagnosis.md`

- Resumen ejecutivo.
- Fuentes revisadas.
- Estado general de la documentación.
- Cobertura funcional.
- Cobertura técnica.
- Cobertura de negocio.
- Cobertura de automatización.
- Cobertura de seguridad y observabilidad.
- Contradicciones.
- Duplicidades.
- Vacíos.
- Diferencias entre código y documentación.
- Riesgos iniciales.
- Recomendaciones.

## Contenido de `decisions-and-assumptions.md`

Para cada elemento:

- Identificador.
- Tipo.
- Contexto.
- Evidencia.
- Alternativas.
- Recomendación.
- Consecuencia.
- Estado.
- Responsable sugerido.
- Fecha máxima de resolución, solo si está sustentada.
- Fases afectadas.

## Criterios de finalización

- Todas las fuentes relevantes fueron revisadas.
- No se diseñaron releases.
- Las contradicciones están explicitadas.
- Las decisiones bloqueantes están identificadas.
- Los supuestos están separados de los hechos.
- Los archivos contienen historial de cambios.

## Resultado final

Indica:

1. Qué decisiones deben resolverse antes de la Fase 02.
2. Qué decisiones pueden mantenerse como supuestos.
3. Qué riesgos deben investigarse tempranamente.

# Fase 05 — Generar el documento de una release

Actúa como Product Owner, Release Manager y Software Architect de GradeOps AI.

## Parámetros de ejecución

Antes de ejecutar, reemplaza:

- `<RELEASE_ID>` por el número de release.
- `<RELEASE_NAME>` por el nombre aprobado.
- `<RELEASE_FILE>` por el slug del archivo.

Ejemplo:

```text
<RELEASE_ID> = 01
<RELEASE_NAME> = First Assessment Flow
<RELEASE_FILE> = first-assessment-flow
```

## Objetivo

Generar exclusivamente la documentación detallada de la Release `<RELEASE_ID>` — `<RELEASE_NAME>`.

No generes, modifiques ni anticipes el detalle de otras releases.

## Entradas obligatorias

- `@docs/`
- `@docs/master-plan/master-plan-executive.md`
- `@docs/master-plan/README.md`
- Todos los archivos de `@docs/master-plan/analysis/`
- User stories asignadas a esta release.
- `master-plan-specification.md`, si está disponible.

## Archivos que puede crear

- `@docs/master-plan/releases/release-<RELEASE_ID>-<RELEASE_FILE>.md`

## Archivos que puede modificar

- El archivo de esta release, si ya existe.
- `@docs/master-plan/README.md`, únicamente para corregir la fila de esta release en la tabla de releases (nombre, enlace, estado) cuando diverja de lo fijado en la Fase 04. No reordenar, añadir ni eliminar filas de otras releases, ni modificar cualquier otra sección del README.

## Archivos que no puede modificar

- Otras releases.
- Master Plan ejecutivo.
- Inventarios.
- User stories originales.
- Código.
- Cualquier sección del README distinta a la fila de esta release.

## Prevalidación

Antes de redactar:

1. Confirmar que la release existe en `master-plan-executive.md`.
2. Confirmar que sus US están asignadas.
3. Confirmar que no existen decisiones bloqueantes.
4. Confirmar que la release no es XL.
5. Confirmar que habilita un flujo vertical.
6. Confirmar que sus dependencias anteriores están claras.

Si falla una precondición, no inventes. Registra el bloqueo al inicio del archivo.

## Corrección puntual del README

Si el `<RELEASE_FILE>` o el nombre final de la release difiere de lo que ya figura en `README.md`, corrige únicamente la fila correspondiente a esta release (nombre, enlace, estado) después de generar el archivo de la release. No toques ninguna otra fila ni sección del README. Si no hay divergencia, no modifiques el README.

## Contenido obligatorio

1. Identificación.
2. Objetivo ejecutivo.
3. Problema.
4. Hipótesis.
5. Actor beneficiado.
6. Valor entregado.
7. Nivel de automatización.
8. Alcance incluido.
9. Exclusiones explícitas.
10. Capacidades.
11. Flujo funcional.
12. User stories incluidas.
13. Historias propuestas o modificadas.
14. Consideraciones adicionales para las US.
15. Reglas de negocio.
16. Dependencias.
17. Integraciones.
18. Arquitectura mínima necesaria.
19. Datos y migraciones.
20. Seguridad y privacidad.
21. Observabilidad y auditoría.
22. Automatizaciones.
23. Trigger, inputs y outputs.
24. Human in the loop.
25. Guardrails.
26. Idempotencia.
27. Reintentos y fallos.
28. Reversión.
29. Consumo y costos.
30. Criterios funcionales.
31. Criterios técnicos.
32. Criterios de calidad.
33. Criterios de seguridad.
34. Criterios de observabilidad.
35. Criterios de despliegue.
36. Criterios de negocio.
37. Definition of Done.
38. Validación.
39. Escenario Given/When/Then.
40. Métricas.
41. Evidencias.
42. Riesgos y mitigaciones.
43. Resultado esperado.
44. Prompt ejecutable `/release-*`.
45. Historial de cambios.

## Prompt `/release-*`

Antes de generarlo:

1. Inspecciona los comandos `/release-*` del plugin `claude-planning-with-ai`.
2. Determina la secuencia correcta.
3. No inventes comandos.
4. Si no puedes inspeccionarlos, utiliza placeholders explícitos.

El prompt debe incluir:

- Contexto.
- Objetivo.
- US.
- Fuentes.
- Flujo.
- Alcance.
- Exclusiones.
- Arquitectura.
- Seguridad.
- Automatización.
- Trazabilidad.
- Métricas.
- Criterios.
- Dependencias.
- Riesgos.
- Entregables.
- Prohibición de implementar releases posteriores.
- Detección de contradicciones.
- Actualización documental de decisiones.

## Criterios de finalización

- El archivo es autocontenido.
- No duplica innecesariamente el documento ejecutivo.
- El flujo es demostrable.
- Las US están listas o marcadas como bloqueadas.
- Los criterios son verificables.
- Las automatizaciones tienen límites.
- El prompt `/release-*` puede copiarse directamente.
- La fila de esta release en el README está alineada con el archivo generado.

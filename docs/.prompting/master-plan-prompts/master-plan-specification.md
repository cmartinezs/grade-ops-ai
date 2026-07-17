# Especificación maestra del Master Plan Ejecutivo

## Propósito

Definir los criterios que debe cumplir el Master Plan Ejecutivo de GradeOps AI.

El plan debe transformar la visión, decisiones, user stories, restricciones y objetivos de `@docs/` en releases incrementales, funcionales, desplegables, demostrables y orientadas a valor.

## Principios

### Entregas verticales

Cada release debe habilitar un flujo funcional de extremo a extremo. No debe limitarse a infraestructura, endpoints, modelos de dominio o componentes visuales aislados.

### Valor temprano

Priorizar primero lo que permita validar hipótesis, conseguir pilotos, procesar evaluaciones reales, generar evidencia, medir impacto y cobrar.

### Automatización progresiva

Clasificar cada capacidad como:

- Manual.
- Asistida.
- Supervisada.
- Automatizada.
- Autónoma controlada.

### Human in the loop

La IA puede recomendar y ejecutar tareas preparatorias, pero el docente conserva autoridad pedagógica en decisiones sensibles. Toda intervención humana debe quedar trazada.

### Evidencia desde el inicio

Registrar, cuando corresponda:

- Usuario.
- Organización, curso y evaluación.
- Agente o proceso.
- Input y output estructurado.
- Modelo.
- Tokens.
- Costo estimado.
- Duración.
- Estado.
- Errores y reintentos.
- Propuesta de IA.
- Decisión humana.
- Tiempo estimado ahorrado.
- Valor generado.

### Arquitectura evolutiva

Aplicar KISS, DRY, separación de responsabilidades, arquitectura hexagonal, idempotencia, observabilidad, seguridad por diseño, control de costos, manejo de errores y feature flags cuando aporten valor.

## Organización documental

```text
@docs/master-plan/
├── README.md
├── master-plan-executive.md
├── validation-report.md
├── analysis/
│   ├── documentation-diagnosis.md
│   ├── decisions-and-assumptions.md
│   ├── capability-map.md
│   ├── user-story-inventory.md
│   ├── automation-inventory.md
│   └── release-strategy.md
└── releases/
    ├── release-01-<nombre-descriptivo>.md
    └── ...
```

## Contenido del README del Master Plan

- Propósito.
- Alcance.
- Estado general.
- Convenciones.
- Orden de lectura.
- Tabla de releases.
- Enlaces relativos.
- Leyenda de estados.
- Última actualización.
- Reglas de mantenimiento.

## Contenido del documento ejecutivo

- Resumen ejecutivo.
- Estado de la documentación.
- Supuestos y decisiones pendientes.
- Objetivos estratégicos.
- Mapa de capacidades.
- Flujo crítico.
- Diagnóstico global de user stories.
- Inventario global de automatización.
- Priorización.
- Resumen de releases.
- Camino crítico.
- Hitos.
- Trazabilidad.
- Riesgos.
- Métricas y evidencias.
- Próxima acción.

## Contenido mínimo de cada release

1. Identificación.
2. Objetivo ejecutivo.
3. Problema e hipótesis.
4. Valor entregado.
5. Alcance.
6. Exclusiones.
7. User stories.
8. Historias propuestas o modificadas.
9. Flujo funcional.
10. Nivel de automatización.
11. Procesos automatizados.
12. Intervención humana.
13. Dependencias.
14. Integraciones.
15. Arquitectura.
16. Seguridad y privacidad.
17. Observabilidad y auditoría.
18. Costos y consumo.
19. Datos y migraciones.
20. Criterios de salida.
21. Validación.
22. Escenario demostrable.
23. Métricas y evidencias.
24. Riesgos.
25. Prompt `/release-*`.
26. Resultado esperado.
27. Historial de cambios.

## Definition of Ready

Una US ejecutable debe tener:

- Actor.
- Problema o necesidad.
- Valor.
- Criterios verificables.
- Dependencias.
- Reglas de negocio.
- Restricciones.
- Datos requeridos.
- Comportamiento esperado.
- Ausencia de decisiones bloqueantes.

## Definition of Done

Una release termina cuando:

- El flujo completo está implementado.
- Los criterios de aceptación están validados.
- Las pruebas necesarias están aprobadas.
- Los errores relevantes están controlados.
- Observabilidad y auditoría están habilitadas.
- Seguridad y documentación están actualizadas.
- Puede desplegarse y demostrarse.
- Puede recopilar métricas y evidencias.
- No depende de pasos manuales ocultos.
- Lo incompleto está protegido o excluido.

## Tamaño de releases

- S: pequeña.
- M: mediana.
- L: grande.
- XL: debe dividirse.

Las divisiones deben hacerse por valor vertical, no por capas técnicas.

## Reglas de consistencia

- No duplicar detalle entre ejecutivo y releases.
- Mantener enlaces relativos.
- Mantener numeración estable.
- No reutilizar números.
- Toda release debe tener archivo.
- Toda release debe aparecer en el README.
- Toda US priorizada debe ser trazable.
- No dejar observabilidad, seguridad, costos o auditoría para el final.
- No inventar fechas ni comandos.
- Diferenciar MVP, alcance hackathon y roadmap posterior.

## Control de cambios

Cada documento debe contener un historial con:

- Fecha.
- Cambio.
- Motivo.
- Elementos afectados.
- Decisión asociada.

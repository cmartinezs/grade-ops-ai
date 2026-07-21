# GradeOps AI — Plan de seguridad para `web/`

## Propósito

Este paquete documenta el estado comprobado, el modelo objetivo y la estrategia incremental de seguridad del frontend `web/`. Es insumo para un agente planificador: no es una afirmación de que los controles propuestos ya estén implementados.

## Baseline

- Repositorio: `cmartinezs/grade-ops-ai`
- Proyecto: `web/`
- Rama: `develop`
- Commit: `aa2dc4e`
- Revisión: 21 de julio de 2026

## Orden recomendado

1. [Resumen general](01-resumen-general.md)
2. [Estado actual y evidencia](02-estado-actual-y-evidencia.md)
3. [Modelo objetivo y fronteras](03-modelo-objetivo-y-fronteras.md)
4. [Identidades, sesiones y rutas](04-identidades-sesiones-y-rutas.md)
5. [Cliente API y protección de datos](05-cliente-api-y-proteccion-de-datos.md)
6. [Flujos Teacher, Operator y Student](06-flujos-por-actor.md)
7. [Headers y amenazas del navegador](07-headers-y-amenazas.md)
8. [Pruebas, observabilidad y aceptación](08-pruebas-observabilidad-aceptacion.md)
9. [Implementación incremental](09-estrategia-incremental.md)
10. [Riesgos y decisiones abiertas](10-riesgos-y-decisiones.md)
11. [Topología de seguridad multiambiente](11-topologia-seguridad-multiambiente.md)

## Reglas para el planificador

- Distinguir `EVIDENCIA ACTUAL`, `BRECHA` y `PROPUESTA`.
- No tratar guards, menús ocultos o redirects como autorización real.
- Todo endpoint debe seguir autorizado por `api/` con permission, ownership y estado de dominio.
- No almacenar ID tokens en `localStorage`, logs, analytics ni estado persistido.
- Cada alcance que cambie hosting, headers o secretos debe incluir una tarea de `infra/`.
- Las entregas deben incluir pruebas negativas, no solo happy path.
- El plan debe separar `demo` en GCP de `beta` en Vercel–Render–Neon y probar aislamiento entre ambientes.

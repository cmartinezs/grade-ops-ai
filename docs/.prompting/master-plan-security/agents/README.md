# GradeOps AI — Plan de seguridad para `agents/`

## Propósito

Plan profesional de seguridad del runtime `agents/`, basado en código real. Complementa el RBAC de `api/`; no lo replica. `agents/` autoriza servicios y capacidades de ejecución, valida contratos y limita el uso de proveedores/modelos/herramientas.

## Baseline

- Proyecto: `agents/`
- Rama: `develop`
- Commit: `aa2dc4e`
- Revisión: 21 de julio de 2026

## Contenido

1. [Resumen general](01-resumen-general.md)
2. [Estado actual y evidencia](02-estado-actual-y-evidencia.md)
3. [Modelo de confianza](03-modelo-de-confianza.md)
4. [Identidad service-to-service](04-identidad-service-to-service.md)
5. [Autorización por capacidad y policy](05-capacidades-y-policy.md)
6. [Contratos, prompts y output](06-contratos-prompts-output.md)
7. [Proveedores, secretos y egress](07-proveedores-secretos-egress.md)
8. [Herramientas, archivos y sandbox](08-herramientas-archivos-sandbox.md)
9. [Auditoría, pruebas y respuesta](09-auditoria-pruebas-respuesta.md)
10. [Implementación y decisiones](10-implementacion-y-decisiones.md)
11. [Topología de seguridad multiambiente](11-topologia-seguridad-multiambiente.md)

## Reglas para el planificador

- `api/` conserva roles humanos, ownership, cuotas y decisiones del dominio.
- `agents/` nunca confía en campos actor/organization enviados como autorización.
- No registrar prompts, respuestas completas, tokens ni secretos.
- Todo nuevo proveedor o herramienta requiere allowlist, presupuesto, timeout, telemetría y tarea de `infra/`.
- Separar controles existentes de controles propuestos.

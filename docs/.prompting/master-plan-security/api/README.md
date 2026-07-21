# GradeOps AI — Seguridad, roles y autorización de `api/`

## Propósito

Este paquete documenta el estado real de seguridad observado en `grade-ops-ai/api`, las brechas entre autenticación y autorización, y la arquitectura recomendada para incorporar roles, permisos, protección de endpoints, ownership y auditoría confiable.

Está preparado como fuente de contexto para un agente de IA encargado de generar un plan de implementación. El agente debe distinguir en todo momento entre:

- **Evidencia actual:** comportamiento confirmado en el código revisado.
- **Brecha:** capacidad ausente o incompleta.
- **Decisión recomendada:** arquitectura objetivo propuesta.
- **Trabajo futuro:** implementación que todavía no existe.

## Baseline de revisión

| Elemento | Valor |
|---|---|
| Repositorio | `cmartinezs/grade-ops-ai` |
| Componente | `api/` |
| Rama | `develop` |
| Commit | `aa2dc4e` |
| Fecha de revisión | 20 de julio de 2026 |

La revisión se realizó sobre código, configuración de Spring Security, integración Firebase, controladores, casos de uso, repositorios, migraciones, pruebas existentes y documentación funcional disponible. La suite Maven no pudo ejecutarse en el entorno de revisión porque intentó escribir en `/root/.m2`, ubicación de solo lectura.

## Contenido

1. [Resumen general](01-resumen-general.md)
2. [Estado actual y evidencia](02-estado-actual-y-evidencia.md)
3. [Modelo objetivo de autorización](03-modelo-objetivo-autorizacion.md)
4. [Roles, permisos e identidades](04-roles-permisos-identidades.md)
5. [Protección de endpoints](05-proteccion-endpoints.md)
6. [Ownership y autorización por recurso](06-ownership-y-dominio.md)
7. [Estudiantes y enlaces firmados](07-estudiantes-enlaces-firmados.md)
8. [Endpoints internos y service-to-service](08-seguridad-interna.md)
9. [Auditoría, observabilidad y pruebas](09-auditoria-observabilidad-pruebas.md)
10. [Estrategia incremental](10-estrategia-incremental.md)
11. [Riesgos y decisiones abiertas](11-riesgos-decisiones-abiertas.md)
12. [Topología de seguridad multiambiente](12-topologia-seguridad-multiambiente.md)

## Instrucciones para el agente planificador

El plan derivado debe:

1. Mantener Firebase como mecanismo de autenticación humana.
2. No confundir autenticación, RBAC, permisos, ownership y reglas de dominio.
3. Implementar inicialmente solo `TEACHER` y `OPERATOR` como roles humanos.
4. No crear `ROLE_STUDENT` para el MVP.
5. Mantener PostgreSQL como fuente canónica de roles y estado de cuenta.
6. Proteger capacidades con `@PreAuthorize`, pero preservar el control de ownership en repositorios/casos de uso.
7. Obtener la identidad auditada desde el principal autenticado, nunca desde el body.
8. Separar endpoints humanos de Operator y endpoints service-to-service.
9. Incluir migraciones, compatibilidad, pruebas negativas y estrategia de rollback.
10. Referenciar evidencia concreta y no presentar recomendaciones como código existente.
11. Diseñar y probar por separado `demo` (GCP) y `beta` (Vercel–Render–Neon); no asumir que IAM/OIDC de Cloud Run existe en Render.

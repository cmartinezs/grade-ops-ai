# Riesgos y decisiones abiertas

## Riesgos críticos

1. **Confiar solo en `@PreAuthorize`.** No evita IDOR sin ownership/scope.
2. **Usar exclusivamente Firebase Custom Claims.** La revocación queda sujeta a renovación del token.
3. **Agregar `role` directamente a `teacher`.** Mezcla dominios e impide múltiples roles.
4. **Crear `ROLE_STUDENT`.** Amplía innecesariamente la superficie del MVP.
5. **Dar acceso académico completo a Operator.** Viola mínimo privilegio y minimización de PII.
6. **Mantener `setBy` en el body.** Permite falsificar auditoría.
7. **Conservar secreto compartido en producción.** Impide atribución y revocación individual.
8. **Aplicar cambios masivos sin inventario de endpoints.** Puede dejar rutas sin protección o bloquear flujos válidos.
9. **Cachear roles sin estrategia de revocación.** Extiende privilegios después de retirarlos.
10. **Devolver códigos inconsistentes.** Puede filtrar existencia de recursos o dificultar clientes.

## Decisiones abiertas para el plan

| Decisión | Recomendación inicial |
|---|---|
| Estado de cuenta: `401` o `403` al bloquear | Definir política global; preferir `403` para identidad válida sin acceso, evitando detalles |
| TTL exacto del caché de authorities | 1–5 minutos, según SLA de revocación |
| Ubicación de `@PreAuthorize` | Puertos/controladores de entrada; mantener dominio libre de Spring |
| Error por estado inválido | Estandarizar `409` o `422` en ADR de API |
| Bootstrap del primer Operator | Procedimiento controlado, auditable y no expuesto públicamente |
| Custom Claims | Solo optimización o señal, no fuente exclusiva |
| Retención de auditoría | Definir según evidencia del piloto, privacidad y normativa aplicable |

## Restricciones para el agente planificador

- No inventar roles adicionales sin historia de usuario y amenaza concreta.
- No reemplazar ownership por anotaciones.
- No compartir módulos de seguridad que acoplen innecesariamente servicios.
- No introducir un proveedor IAM adicional antes de validar que Firebase + PostgreSQL es insuficiente.
- No mezclar esta iniciativa con rediseños funcionales ajenos.
- Cada release debe incluir migración, compatibilidad, pruebas negativas, observabilidad y rollback.

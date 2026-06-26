# 14 — Checklists

## Checklist de nueva feature

### Diseño

- [ ] La feature tiene nombre de negocio claro.
- [ ] Se identificó artifact: `api`, `agents`, `worker`, etc.
- [ ] Se identificó package: `<base>.<artifact>.<feature>`.
- [ ] Se definió si requiere DDD táctico completo o modelo simple.
- [ ] Se definió si el caso de uso es simple, con orquestador o con pasos.
- [ ] Se identificaron eventos de dominio relevantes.
- [ ] Se identificaron puertos externos necesarios.

### Dominio

- [ ] No hay imports de Spring/JPA/Jackson/HTTP.
- [ ] Aggregates protegen invariantes.
- [ ] Value objects validan sus valores.
- [ ] Estados usan enum o modelo explícito, no strings sueltos.
- [ ] Domain events están en pasado y son inmutables.
- [ ] No hay setters públicos para modificar reglas de negocio.

### Aplicación

- [ ] Existe `UseCase` en `application.port.in`.
- [ ] Existe `Command` o `Query`.
- [ ] Existe `Result`.
- [ ] Handler implementa el puerto de entrada.
- [ ] Persistencia/proveedores externos entran por puertos de salida.
- [ ] Transacción está en lugar correcto.
- [ ] Errores de negocio son explícitos.

### Infraestructura

- [ ] Adapter implementa puerto de salida.
- [ ] JPA entity no sale del adapter.
- [ ] Mapper no contiene reglas de negocio.
- [ ] Cambios de schema tienen migración.
- [ ] Queries multi-tenant filtran por `tenantId`.
- [ ] Llamadas externas tienen timeout y manejo de error.

### API

- [ ] Controller no contiene lógica de negocio.
- [ ] Request y Response son DTOs separados.
- [ ] Request tiene validación de formato.
- [ ] Controller invoca UseCase, no repositories.
- [ ] Errores se traducen por exception handler.
- [ ] OpenAPI se actualizó si aplica.

### Testing

- [ ] Tests de dominio para invariantes.
- [ ] Tests de aplicación para flujo.
- [ ] Tests de adapter si hay integración técnica.
- [ ] Tests de controller si hay endpoint.
- [ ] Tests de seguridad si toca permisos/tenant/auth.
- [ ] Tests AI si hay output estructurado, timeout o error provider.

### Seguridad y observabilidad

- [ ] No se loguean datos sensibles.
- [ ] Correlation ID propagado.
- [ ] MDC se limpia.
- [ ] AI execution log registra hashes/costos/estado.
- [ ] Secrets no están en repo.
- [ ] Errores no exponen stack trace ni payload sensible.

## Checklist de Pull Request

- [ ] El PR tiene descripción clara.
- [ ] El cambio corresponde al scope declarado.
- [ ] No mezcla refactor grande con feature grande sin necesidad.
- [ ] Compila localmente.
- [ ] Tests pasan localmente.
- [ ] No introduce dependencias nuevas sin justificar.
- [ ] No rompe arquitectura hexagonal.
- [ ] No agrega código muerto.
- [ ] No deja TODOs críticos sin issue.
- [ ] No expone datos sensibles en logs.
- [ ] Incluye migración si cambia DB.
- [ ] Incluye actualización de docs si cambia contrato.

## Checklist de revisión de arquitectura

- [ ] `domain` no depende de frameworks.
- [ ] `application` no depende de adapters concretos.
- [ ] `infrastructure` no contiene reglas de dominio.
- [ ] `api` no retorna JPA entities.
- [ ] Puertos tienen nombres de capacidad, no de tecnología.
- [ ] Adapters tienen nombres de tecnología/capacidad.
- [ ] Las transacciones no envuelven llamadas remotas largas sin necesidad.
- [ ] Los eventos críticos usan outbox o mecanismo confiable.

## Checklist de AI feature

- [ ] Prompt versionado.
- [ ] Output estructurado validado.
- [ ] Provider aislado detrás de puerto.
- [ ] Costos/tokens estimados registrados.
- [ ] Créditos consumidos mediante caso de uso/política.
- [ ] Input/output sensible sanitizado o hasheado.
- [ ] Timeout y retry definidos.
- [ ] Fallback o error claro definido.
- [ ] Aprobación humana cuando afecte evaluación o feedback.
- [ ] Tests para output malformado.

## Checklist de release

- [ ] CI verde.
- [ ] Migraciones revisadas.
- [ ] Config de entorno documentada.
- [ ] Variables sensibles fuera del repo.
- [ ] Observabilidad disponible.
- [ ] Health checks funcionando.
- [ ] Rollback considerado.
- [ ] Cambios breaking comunicados.

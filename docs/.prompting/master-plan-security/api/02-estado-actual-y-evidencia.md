# Estado actual y evidencia

## Autenticación Firebase

`FirebaseTokenFilter.java`, bajo `shared/infrastructure/config/security`, verifica el token Firebase y crea un `UsernamePasswordAuthenticationToken` con authorities vacías:

```java
new UsernamePasswordAuthenticationToken(
    principal,
    null,
    Collections.emptyList()
);
```

Consecuencia comprobada: el principal queda autenticado, pero Spring no dispone de `ROLE_TEACHER`, `ROLE_OPERATOR` ni permisos del tipo `assessment:create`.

## Configuración HTTP

`SecurityConfig.java` termina protegiendo las rutas no públicas con:

```java
.anyRequest().authenticated()
```

Esto implementa autenticación general, no autorización funcional. También falta habilitar seguridad de métodos mediante `@EnableMethodSecurity`.

## Ownership existente

Los casos de uso de assessment utilizan `OwnershipVerifier.java`. Ante acceso de un docente distinto:

- Se registra el intento.
- Se lanza `ResourceNotFoundException`.
- La API responde `404` en lugar de `403`.
- No se confirma al atacante que el recurso existe.

Esta conducta coincide con el objetivo de US-007 y debe preservarse.

La mejora propuesta consiste en consultar directamente por `assessmentId + teacherUid`, evitando que un recurso ajeno sea cargado en memoria antes de validar ownership.

## Endpoints internos

`InternalTeacherController.java` permite operaciones sensibles, entre ellas aprovisionar docentes y modificar flags asociados al piloto, related party y evidencia.

El acceso depende de `X-Internal-Key`, pero el diseño actual presenta estas brechas:

- No autentica una identidad humana individual.
- No diferencia roles o permisos.
- Todos los poseedores del secreto comparten el mismo poder.
- No existe revocación individual.
- `setBy` llega desde el request y puede falsificarse.
- La atribución de auditoría no es confiable.

Se observó además drift de nombres: código y documentación histórica mencionan variantes como `X-Internal-Key` y `X-Internal-Secret`.

## Tabla de capacidades actuales

| Capacidad | Estado |
|---|---|
| Verificación de Firebase ID Token | Implementada |
| Comprobación de revocación/token | Implementada según flujo revisado |
| Exigencia de correo verificado | Implementada |
| Rutas públicas vs autenticadas | Implementada |
| Authorities de Spring | No implementadas |
| Roles persistidos | No implementados |
| `@EnableMethodSecurity` | No implementado |
| `@PreAuthorize` efectivo por permiso | No implementado |
| Ownership de assessments | Parcialmente implementado |
| Ownership transversal | No implementado completamente |
| Identidad individual de Operator | No implementada |
| OIDC service-to-service | Documentado/recomendado, no confirmado como implementación actual |

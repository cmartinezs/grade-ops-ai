# Modelo objetivo de autorización

## Principio rector

GradeOps AI debe implementar una autorización híbrida:

```text
Firebase Authentication
→ Platform Account
→ Roles
→ Permissions / Granted Authorities
→ Resource Scope / Ownership
→ Domain Invariants
→ Audit Event
```

RBAC por sí solo no evita IDOR. `@PreAuthorize` debe decidir si un actor posee la capacidad general; el caso de uso y el repositorio deben limitar el recurso; el dominio debe validar si la transición es legal.

## Principal autenticado

El principal actual orientado exclusivamente a Teacher debería evolucionar a uno neutral:

```java
public record AuthenticatedAccount(
    String uid,
    String email,
    Set<PlatformRole> roles
) {}
```

La neutralidad permite que una misma identidad posea más de un rol sin duplicar usuarios o contaminar el agregado Teacher.

## Configuración Spring

Objetivo mínimo:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
}
```

Tras verificar Firebase, la API debe cargar roles/permisos y construir una autenticación con authorities reales:

```java
Set<GrantedAuthority> authorities =
    authorizationService.loadAuthorities(identity.uid());

var authentication =
    UsernamePasswordAuthenticationToken.authenticated(
        principal,
        null,
        authorities
    );
```

## Fuente de verdad

PostgreSQL debe ser la fuente canónica para:

- Estado activo/bloqueado de la cuenta.
- Roles vigentes.
- Concesión y revocación.
- Actor que otorgó o revocó privilegios.

Firebase Custom Claims puede reflejar roles para optimizar, pero no debe ser la única autoridad en operaciones sensibles: los ID tokens conservan claims hasta su renovación.

Se admite un caché breve de 1–5 minutos, con invalidación ante cambios privilegiados si la infraestructura lo permite.

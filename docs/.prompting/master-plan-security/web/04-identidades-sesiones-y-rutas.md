# Identidades, sesiones y rutas

| Actor | Credencial | Superficie | Regla |
|---|---|---|---|
| Teacher | Firebase ID token | `/dashboard`, `/assessments`, etc. | Capacidades Teacher + ownership API |
| Operator | Firebase ID token | `/operator/**` | Permisos operacionales mínimos |
| Student | Token de invitación | `/access/{token}` o equivalente | Solo assessment/invitación/acciones permitidas |

## Teacher

El guard debe distinguir: inicializando, anónimo, pendiente de verificación, cuenta deshabilitada y autenticado. Después debe cargar sesión de plataforma; tener usuario Firebase no implica tener una cuenta GradeOps activa.

## Operator

Debe usar layout y navegación separados. Nunca habilitar acceso por una lista de correos embebida ni por `NEXT_PUBLIC_*`. Los permisos vienen de API. La UI no debe exponer entregas o PII académica si el permiso operacional solo permite métricas agregadas.

## Student

No inicializa una sesión Teacher ni reutiliza el token en URLs secundarias. El token debe evitar referrer leakage, eliminarse de la barra cuando sea viable mediante canje por sesión limitada y no persistirse en analytics. La API valida expiración, revocación, nonce, assessment y acción.

## Renovación y cierre

- Renovar ID token mediante Firebase, no mediante timers caseros.
- Ante revocación, limpiar estado sensible y cachés de datos.
- `sign-out` en API y Firebase deben ser idempotentes.
- No mostrar contenido del usuario anterior durante transiciones de sesión.

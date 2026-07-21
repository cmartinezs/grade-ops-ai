# Resumen general

`web/` autentica docentes con Firebase en el navegador y adjunta el ID token a llamadas a `api/`. La protección actual de páginas es un `AuthGuard` cliente que comprueba usuario y correo verificado. Esto mejora la experiencia, pero el HTML/JavaScript se entrega al navegador y no representa una frontera de seguridad.

El diseño objetivo mantiene `api/` como autoridad: `web/` consume una sesión, representa capacidades devueltas por la API y evita ofrecer acciones no disponibles, mientras cada operación vuelve a ser validada en servidor.

## Conclusiones

- Teacher y Operator usan identidad Firebase, pero reciben experiencias y capacidades diferentes.
- Student no inicia sesión: usa un enlace firmado y de alcance limitado.
- El frontend nunca decide ownership ni aprobación académica.
- Los roles no deben deducirse del pathname, correo o variables públicas.
- La UI debe tolerar `401`, `403`, `404` y conflictos de estado sin filtrar información.
- Para producción conviene evaluar un patrón BFF/cookie `HttpOnly`; no debe introducirse a ciegas durante el MVP si Firebase cliente sigue siendo el mecanismo aprobado.

## Resultado esperado

Una aplicación que minimiza exposición de tokens y PII, separa superficies por actor, aplica navegación basada en capacidades, incorpora headers defensivos y demuestra mediante pruebas que el backend sigue blindado aunque el cliente sea manipulado.

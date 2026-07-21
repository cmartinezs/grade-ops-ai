# Herramientas, archivos y sandbox

Hoy el Assessment Agent no usa herramientas ni ejecuta código. Esta ausencia es una propiedad de seguridad que debe preservarse hasta diseñar controles.

## Mediador de herramientas

Toda tool call futura pasa por un broker determinista que valida: capability, argumentos con schema, recurso/tenant, límite de llamadas, timeout y resultado máximo. El LLM propone; el runtime autoriza.

## Archivos

- URLs firmadas cortas emitidas por API.
- Verificación de tamaño, MIME real, extensión y malware.
- Extracción en proceso aislado; proteger contra zip bombs y path traversal.
- No permitir fetch de URLs arbitrarias (SSRF).
- Minimizar PII enviada al proveedor.

## Sandbox de código

Debe ser efímero, sin credenciales, sin red por defecto, filesystem temporal, usuario no privilegiado, CPU/memoria/PID/tiempo limitados y destrucción posterior. No ejecutar submissions dentro del proceso Spring ni montar repositorios/secretos del servicio.

Incorporar sandbox solo con threat model, infra dedicada y pruebas de escape; no como método auxiliar directo del agente.

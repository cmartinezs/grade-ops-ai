# Privacidad, seguridad, retención y costo

## Clasificación

| Clase | Ejemplo | Tratamiento |
|---|---|---|
| Pública | versión del servicio | permitida |
| Interna | latencia, error code | acceso operacional |
| Confidencial | UID, organization ID | pseudonimizar/restringir |
| Sensible académica | submission, nota, feedback | no registrar en telemetría técnica |
| Secreto | token, API key | prohibido |

## Controles

- Redaction central antes de exportar.
- Allowlist de campos; evitar blacklist como única defensa.
- Mínimo privilegio con IAM en GCP y credenciales/tokens de alcance mínimo en Vercel, Render, Neon y el backend OTel elegido.
- Auditoría de consultas/exportaciones sensibles.
- Retención diferenciada por ambiente y señal.
- Lifecycle rules en el object storage seleccionado.
- Separación entre ambientes/proyectos o labels controlados.
- Eliminación coherente con política de privacidad y solicitudes aplicables.

## Cardinalidad y costo

- Nunca usar IDs como labels de métricas.
- Evitar logs por token, chunk o elemento de batch salvo diagnóstico temporal.
- Sampling de trazas y logs informativos.
- Presupuestos y alertas por ingestión/retención separados por ambiente y proveedor.
- Verificar los límites reales del plan contratado antes de fijar retenciones o SLO.
- Métricas agregadas desde fuente canónica cuando sea más barato que escanear logs.

## Telemetría del navegador

- Consentimiento y finalidad definidos antes de analytics de terceros.
- No usar session replay en MVP.
- No capturar inputs de formularios.
- Source maps privados y acceso restringido.
- Identidad pseudónima solo cuando sea necesaria para diagnóstico.

## Riesgos principales

1. Filtrar contenido estudiantil a un backend de observabilidad.
2. Usar logs como auditoría canónica.
3. Costos descontrolados por alta cardinalidad.
4. Dar al Operator acceso técnico equivalente a datos académicos.
5. Conservar payloads de IA indefinidamente “por si acaso”.

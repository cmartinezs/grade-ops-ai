# Topología de seguridad multiambiente de `agents/`

## Diferencia esencial

`agents/` no autoriza usuarios humanos. Solo acepta comandos autenticados de `api/`, limitados por capacidad, presupuesto y contrato. La forma de demostrar la identidad de `api/` cambia por plataforma.

| Dimensión | `demo` | `beta` |
|---|---|---|
| Hosting | Cloud Run | Render |
| Identidad entrante | OIDC/IAM | JWT interno firmado y rotatorio |
| Gemini | Vertex/Google GenAI con identidad de workload | API key `GRADEOPS_GEMINI_API_KEY` |
| Groq | Secreto administrado | `GRADEOPS_GROQ_API_KEY` en Render |
| Exposición | Servicio privado/invoker restringido | Servicio potencialmente público, endurecido en aplicación |

## Evidencia actual

- `application-demo.yml` configura Google GenAI mediante proyecto y región; el runtime puede usar identidad GCP.
- `application-beta.yml` usa API keys para Gemini y Groq.
- `InternalAuthFilter` exige `X-Internal-Key` y `application.yml` conserva un secreto simétrico compartido con default inseguro.
- El repositorio no contiene infraestructura declarativa para Render equivalente al Terraform de demo.

## Contrato común

Después de autenticar al caller, `agents/` debe autorizar:

- `subject`: servicio `api` conocido.
- `audience`: instancia exacta de `agents` y ambiente correcto.
- `capability`: agente/operación permitida, por ejemplo `assessment:generate`.
- `operationId`, `agentRunId` e idempotency key.
- límites de tamaño, timeout, concurrencia, tokens y costo.
- provider/model desde una allowlist server-side; nunca confiar ciegamente en la selección del caller.

## `demo`: Cloud Run

- Servicio `agents` sin acceso público.
- `api` usa su service account para emitir un ID token con audience de `agents`.
- `agents` valida issuer, audience y principal; IAM restringe invocación.
- Vertex/Gemini usa ADC y roles mínimos, sin clave JSON.
- Egress a proveedores externos queda limitado y auditado cuando Groq esté habilitado.

## `beta`: Render

Hasta disponer de identidad administrada equivalente:

- `api` firma JWT de vida corta con clave asimétrica.
- `agents` solo almacena las claves públicas y selecciona por `kid`.
- Claims mínimos: `iss`, `sub`, `aud`, `env=beta`, `capabilities`, `iat`, `exp`, `jti`.
- Rechazar `alg=none`, algoritmos no previstos, expiraciones amplias y audiences genéricas.
- Añadir replay protection cuando el comando no sea idempotente y registrar `jti`/operationId sin guardar el token.
- Red privada de Render, si está disponible, es una capa adicional; no reemplaza autenticación criptográfica.
- Las API keys de Gemini/Groq se separan por ambiente, tienen cuota propia y se rotan sin reconstruir imágenes.
- El endpoint interno mantiene rate limit, body limit y timeout aun para requests válidos.

## Migración desde `X-Internal-Key`

1. Rechazar en startup el valor por defecto en perfiles `demo` y `beta`.
2. Incorporar validadores OIDC y JWT interno detrás de una interfaz común `ServiceIdentityVerifier`.
3. Ejecutar modo dual por una ventana corta, con métrica de uso del mecanismo legado.
4. Cambiar `api` al mecanismo nuevo por ambiente.
5. Deshabilitar y rotar definitivamente `X-Internal-Key`.

Nunca permitir fallback al secreto compartido cuando la validación del token moderno falla.

## Pruebas de aceptación

- OIDC válido funciona solo en demo; JWT beta válido funciona solo en beta.
- Token cruzado, audience incorrecta, capability ausente, firma inválida o expiración producen denegación.
- `api` no puede elegir un modelo fuera de allowlist ni superar el presupuesto del ambiente.
- Ningún secreto, prompt crudo o token aparece en logs o errores.
- Rotación con `kid` permite transición controlada y luego invalida la clave anterior.
- Un request repetido no duplica ejecución facturable cuando comparte la misma idempotency key.

## Decisiones pendientes

1. Algoritmo y custodia de claves de firma para beta.
2. Disponibilidad de red privada Render en el plan usado.
3. Allowlist y presupuesto de modelos por ambiente.
4. Estrategia de egress y proveedor de secretos para beta.

# Datos, seguridad y costos

## Datos de prueba

- Factories/builders para dominio y fixtures compartidos para contratos.
- IDs, clocks y random seeds controlados.
- Dataset sintético; prohibido copiar producción.
- Seed versionado y cleanup idempotente.
- Cada ejecución usa tenant/user/prefix único.
- Pruebas de migración desde una versión soportada, no solo base vacía.

## Secretos

- Suites unitarias, aceptación e integración no reciben credenciales reales.
- Secret scanning sobre repositorio y artefactos.
- Variables `NEXT_PUBLIC_*` se consideran públicas.
- En GitHub usar OIDC donde exista; secretos de ambiente con approvals para demo.
- En local usar archivo ignorado y gestor seguro; nunca incluirlo en ZIP/logs.

## Prevención de cruces

Cada servicio debe validar `GRADEOPS_ENVIRONMENT_ID` y los audiences/URLs esperados. Agregar pruebas negativas:

- Web beta no apunta a API demo.
- API beta no llama Agents demo.
- Firebase token de otro ambiente se rechaza.
- clave GenAI/local no coincide con beta/demo.
- DB/bucket de otro ambiente se rechaza por configuración o IAM.

## Costos

- La mayor parte del pipeline usa simuladores.
- GenAI real solo en smoke/eval con presupuesto dedicado.
- Limitar tokens, casos, concurrencia y frecuencia.
- Registrar costo estimado por ejecución `ai-eval`.
- Aplicar retención y compresión a logs/traces/videos.
- Cancelar jobs obsoletos de PR mediante concurrency groups.

## Supply chain

Aunque no reemplaza la estrategia de seguridad, el pipeline debe verificar dependencias, imágenes, SBOM y digest. La imagen promovida debe ser exactamente la probada.

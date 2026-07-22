# Estrategia para beta y demo

## Ambientes

| Ambiente | Topología | Objetivo |
|---|---|---|
| `beta` | Vercel + Render + Neon | Integración continua y validación pre-demo |
| `demo` | GCP/Cloud Run + Cloud SQL/servicios GCP | Ambiente estable de demostración/evidencia |

Cada ambiente debe tener Firebase, secretos, datasets, GenAI credentials y dominios propios. Nunca compartir base de datos, buckets, API keys o proyectos Firebase.

## Beta

Después de cada deployment exitoso:

- smoke de health/readiness/version;
- CORS, CSP, headers y redirecciones;
- login con usuario sintético del ambiente;
- journey crítico `web → api → agents` con dataset controlado;
- verificación de migraciones Neon;
- observabilidad/correlación y ausencia de secretos;
- cleanup de datos de prueba.

Las previews de Vercel no deben apuntar por defecto al backend beta compartido. Usar backend efímero/aislado o limitar previews a pruebas con API simulada.

## Demo

Demo no es un laboratorio. Promover una imagen inmutable ya validada en beta, sin reconstruirla. Ejecutar:

- smoke técnico y funcional pequeño;
- verificación IAM/OIDC `api → agents`;
- Firebase/Cloud SQL/Storage/GenAI del ambiente;
- evidencia del deployment, versión y resultados;
- rollback probado.

## GenAI en ambientes

- Mantener una pequeña prueba real para comprobar credenciales/modelo/schema.
- No ejecutar suites masivas contra el modelo real en cada deployment.
- Separar `smoke-ai` de `ai-eval`.
- Las evaluaciones de calidad usan dataset dorado, métricas/rúbrica, tolerancias y revisión humana para cambios significativos.

## Promoción

Orden recomendado:

```text
PR hermético → imagen candidata → beta → soak/validación → demo
```

La misma imagen y digest deben avanzar entre ambientes; solo cambia configuración/secretos.

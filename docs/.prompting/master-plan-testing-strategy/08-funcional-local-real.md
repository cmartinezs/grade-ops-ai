# Ejecución funcional local con Firebase y GenAI reales

## Propósito

Permitir exploración humana y smoke del stack completo antes de promover, usando integraciones reales pero aisladas de `beta`, `demo` y producción.

## Topología

```text
Browser → Web local → API local → Agents local → GenAI real aislado
                         ↓
                  PostgreSQL local
                         ↓
                Firebase real aislado
```

## Identidades obligatoriamente separadas

- `FIREBASE_PROJECT_ID=gradeops-local-functional-*`
- Firebase Web App exclusiva.
- Service account Firebase Admin exclusiva, mínimo privilegio y rotación.
- `GENAI_PROJECT_ID` o cuenta/API key exclusiva.
- API key distinta de `beta` y `demo`.
- Usuarios y datos sintéticos.

## Invocación segura

El modo no debe arrancar accidentalmente con `docker compose up`. Requiere un comando explícito y archivo no versionado:

```text
docker compose \
  --env-file .env.functional.local \
  -f compose.base.yml \
  -f compose.functional.yml \
  --profile real-external up
```

## Guardas fail-fast

Antes de iniciar:

- rechazar Project IDs/dominios que coincidan con `beta`, `demo` o producción;
- rechazar claves vacías o compartidas conocidas;
- mostrar proyecto, proveedor y modelo sin imprimir secretos;
- exigir confirmación explícita para modelos con costo;
- establecer límites de requests, tokens, concurrencia y timeout;
- deshabilitar envíos de email reales o redirigirlos a un buzón controlado;
- marcar todos los registros como `environment=local-functional`.

## Qué automatizar

Automatizar el arranque, healthchecks, seed, smoke y teardown. No hacer de esta suite un gate regular de PR: es más lenta, tiene costo, puede fallar por cuotas y el LLM no es determinista.

Para GenAI real, evaluar propiedades y schema, no texto exacto. Mantener un dataset pequeño, temperature controlada cuando aplique y registro de modelo/versión.

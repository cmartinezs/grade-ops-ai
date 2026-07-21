# Riesgos y decisiones abiertas

## Riesgos

- Confundir AuthGuard con seguridad real.
- Mostrar todos los menús y depender de errores tardíos de API.
- Renderizar salida LLM como HTML confiable.
- Introducir cookies sin protección CSRF.
- Filtrar token Student en referrer/telemetría.
- Reintentar mutaciones y duplicar costo/artefactos.
- Incluir secretos en variables `NEXT_PUBLIC_*`.

## Decisiones requeridas

1. Firebase cliente directo o BFF con cookie HttpOnly.
2. Contrato exacto de `/api/v1/me` y estrategia de caché.
3. URL/canje del acceso Student.
4. Librería y política para Markdown generado.
5. Proveedor de telemetry y redacción de datos.
6. CSP compatible con Firebase/Google en cada ambiente.

## Recomendación CTO

Para el MVP, conservar Firebase cliente, fortalecer el cliente API, introducir capabilities desde API y headers defensivos. Posponer BFF hasta tener un ADR y una necesidad operativa clara. Construir Student y Operator como superficies separadas, no como variantes cosméticas del portal Teacher.

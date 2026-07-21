# Proveedores, secretos y egress

## Secretos

- Credenciales Gemini/Groq solo en Secret Manager o identidad workload cuando exista.
- Service account distinta por servicio y ambiente.
- No incluir secretos en imagen, repositorio, logs, prompts o errores.
- Rotación y revocación probadas.
- `.env` exclusivamente local e ignorado.

## Egress

Permitir solo endpoints de proveedores aprobados. Aplicar timeouts de conexión/respuesta, límites de body y TLS verificado. Bloquear redirects hacia hosts arbitrarios y metadata endpoints. Nuevos providers requieren revisión de residencia de datos, retención y entrenamiento.

## Fallos y resiliencia

- Circuit breaker por provider/model.
- Backoff con jitter para fallos transitorios.
- No reintentar invalid command, output inválido persistente o budget exceeded.
- Deadline común; cada retry consume el tiempo restante.
- Métricas por provider/model sin prompt.

La configuración actual tiene costos blended provisionales. El control económico debe usar catálogo por modelo/version y conciliar con uso real reportado.

# ⚛️ TASK 02 — Flyway V8: índice en `password_reset_codes.created_at`

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Crear la migración `V8__add_index_prc_created_at.sql` que añade un índice en `password_reset_codes.created_at` para soportar eficientemente la query del cleanup job.

---

## Technical Design

- **Approach:** El cleanup ejecuta `WHERE created_at < :threshold AND (used_at IS NOT NULL OR expires_at < NOW())`. Sin índice en `created_at`, la query hace full scan de la tabla. Un índice simple en `created_at` es suficiente para esta iteración; un índice compuesto puede evaluarse más adelante con `EXPLAIN ANALYZE` sobre datos reales.
- **Affected files:** `src/main/resources/db/migration/V8__add_index_prc_created_at.sql` (nuevo)
- **Interfaces / contracts:** Ninguna — cambio puramente de infraestructura de DB.
- **Design notes:**
  - El nombre sigue la convención existente: `idx_prc_{columna}` (ver `idx_prc_teacher_uid` en V5).
  - La migración no es destructiva: solo añade un índice. Puede aplicarse sin downtime en tablas pequeñas (MVP).
  - La próxima migración disponible es V8 (V7 es la última existente).

---

## Implementation Steps

1. Crear `src/main/resources/db/migration/V8__add_index_prc_created_at.sql`:
   ```sql
   CREATE INDEX idx_prc_created_at ON password_reset_codes(created_at);
   ```

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | Archivo existe en el path correcto con nombre exacto `V8__add_index_prc_created_at.sql` | `ls src/main/resources/db/migration/` |
| 2 | SQL no contiene `DROP`, `DELETE`, `TRUNCATE` ni `ALTER TABLE` | Revisión manual del contenido |
| 3 | Nombre de índice sigue convención `idx_prc_*` | Revisión manual |

---

## Done Criteria

- [ ] Archivo `V8__add_index_prc_created_at.sql` existe en `src/main/resources/db/migration/`
- [ ] El SQL crea exactamente un índice en `created_at` sin operaciones destructivas
- [ ] `./mvnw test` pasa (Flyway valida el checksum del archivo al arrancar el contexto de test)

---

> [← story file](../story-01-cleanup-job.md)

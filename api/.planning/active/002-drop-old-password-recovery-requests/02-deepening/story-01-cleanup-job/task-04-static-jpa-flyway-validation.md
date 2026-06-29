# ⚛️ TASK 04 — Validación estática JPA y Flyway

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Añadir reglas ArchUnit a `HexagonalArchitectureTest` que verifiquen en tiempo de compilación que las entidades JPA en el paquete `persistence` no usan `String` como tipo de `@Id`; y revisar manualmente el archivo `V8__add_index_prc_created_at.sql` contra un checklist de calidad de migración.

---

## Technical Design

- **Approach:** La corrección del PK (task-01) introduce una convención: los `@Id` de entidades JPA deben ser `UUID`, no `String`. ArchUnit puede verificar esto sin levantar el entorno. La revisión de V8 es manual sobre el texto plano del archivo — no requiere ejecutar la DB.
- **Affected files:**
  - `src/test/java/cl/gradeops/ai/api/HexagonalArchitectureTest.java` — añadir una regla ArchUnit
- **Interfaces / contracts:** Ninguna — solo enriquece las reglas de arquitectura existentes.
- **Design notes:**
  - ArchUnit analiza bytecode; la regla se ejecuta con `./mvnw test` sin levantar Spring ni DB.
  - La regla verifica que ninguna clase anotada con `@Entity` en `..adapter.out.persistence..` tenga un campo `String` anotado con `@Id`. Esto protege contra regresiones futuras.
  - La revisión manual de V8 sigue el checklist: (1) nombre correcto `VN__*.sql`, (2) solo `CREATE INDEX`, (3) sin `DROP`/`DELETE`/`TRUNCATE`/`ALTER TABLE`, (4) nombre de índice sigue convención `idx_prc_*`.

---

## Implementation Steps

1. Abrir `HexagonalArchitectureTest.java`.
2. Añadir la siguiente regla al final de la clase:
   ```java
   @ArchTest
   static final ArchRule jpa_entities_must_not_use_string_as_id =
       noFields().that()
           .areDeclaredInClassesThat().resideInAPackage("..adapter.out.persistence..")
           .and().areAnnotatedWith(jakarta.persistence.Id.class)
           .should().haveRawType(String.class)
           .allowEmptyShould(true);
   ```
3. Revisar manualmente `V8__add_index_prc_created_at.sql` contra el checklist:
   - [ ] Nombre: `V8__add_index_prc_created_at.sql` (sin espacios, doble guion bajo, versión secuencial)
   - [ ] Solo contiene `CREATE INDEX` — sin `DROP`, `DELETE`, `TRUNCATE`, `ALTER TABLE`
   - [ ] Nombre del índice sigue convención: `idx_prc_created_at`
   - [ ] Tabla destino es `password_reset_codes`
   - [ ] Columna indexada es `created_at`

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | La regla ArchUnit detecta violaciones si existe un `@Id String` en persistence | La regla falla si `PasswordResetCodeJpaEntity` tiene `@Id` en campo `String` (regresión detectada) |
| 2 | La regla pasa con `@Id UUID id` en la entidad corregida | `./mvnw test` no reporta fallo en `HexagonalArchitectureTest` |
| 3 | Checklist manual de V8 completo sin ítems fallidos | Revisión documentada en comentario de commit |

---

## Done Criteria

- [ ] Regla `jpa_entities_must_not_use_string_as_id` añadida a `HexagonalArchitectureTest`
- [ ] `./mvnw test` pasa con la nueva regla activa
- [ ] Checklist manual de V8 completado y sin observaciones

---

> [← story file](../story-01-cleanup-job.md)

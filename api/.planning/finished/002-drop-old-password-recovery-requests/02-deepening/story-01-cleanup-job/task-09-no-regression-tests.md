# ⚛️ TASK 09 — Tests de no-regresión: flujos existentes de password reset

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-05
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Verificar que los tests existentes de `IssuePasswordResetCodeHandlerTest` y `ResetPasswordOrchestratorTest` pasan sin modificación tras el cambio de PK en la entidad JPA (task-01) y la adición del nuevo método al port (task-05).

---

## Technical Design

- **Approach:** No se crean tests nuevos. Esta tarea es una ejecución deliberada y documentada de la suite existente para confirmar que el cambio de `@Id` en la entidad y la extensión del port no rompieron los flujos de negocio existentes. Si algún test falla, se corrige aquí antes de continuar.
- **Affected files:** Ninguno nuevo. Potencialmente correcciones menores en:
  - `PasswordResetCodePersistenceAdapterTest.java` — el stub de `findByTeacherUid` puede necesitar actualizarse
  - `IssuePasswordResetCodeHandlerTest.java` — usa mocks del port; no debería verse afectado
  - `ResetPasswordOrchestratorTest.java` — igual
- **Interfaces / contracts:** Ninguna nueva.
- **Design notes:**
  - `IssuePasswordResetCodeHandlerTest` y `ResetPasswordOrchestratorTest` mockean `PasswordResetCodeRepositoryPort` directamente → no dependen de la entidad JPA ni del adapter. No deberían verse afectados por task-01.
  - `PasswordResetCodePersistenceAdapterTest` sí depende del adapter y mapper. Sus tests de `save()` necesitarán ajuste por el cambio en `save()` (load-then-update). Este ajuste ya está contemplado en task-01 como parte de la actualización del test de adapter.
  - Esta tarea confirma que después de task-01 + task-05, la suite completa del bounded context `auth` pasa limpia.

---

## Implementation Steps

1. Ejecutar `./mvnw test -Dtest="IssuePasswordResetCodeHandlerTest,ResetPasswordOrchestratorTest,PasswordResetCodePersistenceAdapterTest,PasswordResetCodeTest"`.
2. Si algún test falla por causa de task-01 o task-05 y no fue contemplado en esas tareas, corregirlo aquí.
3. Documentar el resultado: todos pasan / lista de correcciones aplicadas.

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | `IssuePasswordResetCodeHandlerTest` — todos los tests pasan | `./mvnw test -Dtest=IssuePasswordResetCodeHandlerTest` |
| 2 | `ResetPasswordOrchestratorTest` — todos los tests pasan | `./mvnw test -Dtest=ResetPasswordOrchestratorTest` |
| 3 | `PasswordResetCodePersistenceAdapterTest` — todos los tests pasan | `./mvnw test -Dtest=PasswordResetCodePersistenceAdapterTest` |
| 4 | `PasswordResetCodeTest` — todos los tests pasan | `./mvnw test -Dtest=PasswordResetCodeTest` |

---

## Verification Result

> Ejecutado el 2026-06-30. Sin correcciones aplicadas: las 4 clases de test pasan sin modificación tras task-01 y task-05.

**Comando 1 — clases de no-regresión específicas:**

```
./mvnw test -Dtest="IssuePasswordResetCodeHandlerTest,ResetPasswordOrchestratorTest,PasswordResetCodePersistenceAdapterTest,PasswordResetCodeTest"
```

| Clase | Tests run | Failures | Errors | Skipped |
|-------|-----------|----------|--------|---------|
| `PasswordResetCodeTest` | 5 | 0 | 0 | 0 |
| `PasswordResetCodePersistenceAdapterTest` | 7 | 0 | 0 | 0 |
| `ResetPasswordOrchestratorTest` | 6 | 0 | 0 | 0 |
| `IssuePasswordResetCodeHandlerTest` | 3 | 0 | 0 | 0 |
| **Total** | **21** | **0** | **0** | **0** |

Resultado: `BUILD SUCCESS`.

**Comando 2 — suite completa:**

```
./mvnw test
```

Resultado: `Tests run: 136, Failures: 0, Errors: 0, Skipped: 0` → `BUILD SUCCESS`.

No se aplicaron correcciones; ningún test falló por causa de task-01 (cambio de PK en la entidad JPA) ni de task-05 (nuevo método del port). El ajuste previsto en `PasswordResetCodePersistenceAdapterTest` ya estaba contemplado en task-01.

---

## Done Criteria

- [x] Los 4 clases de test listadas pasan sin fallos
- [x] No se introdujeron cambios de comportamiento en los flujos existentes
- [x] `./mvnw test` completo pasa

---

> [← story file](../story-01-cleanup-job.md)

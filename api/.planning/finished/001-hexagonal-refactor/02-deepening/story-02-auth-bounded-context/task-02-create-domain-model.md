# ⚛️ TASK 02 — Create PasswordResetCode aggregate + InvalidResetCodeException + PasswordResetCodeTest

> **Status:** DONE
> **Workflow:** IMPLEMENT
> **Depends On:** —
> [← story file](../story-02-auth-bounded-context.md)

---

## Objective

Create `PasswordResetCode` as a pure Java aggregate root (no JPA) that encapsulates the business logic currently embedded in `auth.PasswordResetCodeEntity`. Create `InvalidResetCodeException` as a domain exception. Write `PasswordResetCodeTest` covering all aggregate invariants.

---

## Technical Design

- **Approach:** The existing `PasswordResetCodeEntity` mixes JPA mapping (`@Entity`, `@Table`, etc.) with business logic (`isExpired()`, `isUsed()`, `markUsed()`). This task extracts the logic into a pure domain aggregate. The JPA entity is created in task-09 as a dumb mapping object. `PasswordResetCode` extends `AggregateRoot<String>` where the ID type is `String` (the `teacherUid` is the natural identifier per domain — a teacher can only have one active reset code at a time). The factory method `issue(String teacherUid, String rawCode, Instant expiresAt)` encapsulates valid creation; `restore(...)` reconstructs from persistence without emitting events.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/auth/domain/model/PasswordResetCode.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/auth/domain/exception/InvalidResetCodeException.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/auth/domain/PasswordResetCodeTest.java` ← NEW
- **Interfaces / contracts:**
  - `PasswordResetCode.issue(String teacherUid, String rawCode, Instant expiresAt)` — static factory; sets `createdAt = Instant.now()`, `usedAt = null`.
  - `PasswordResetCode.restore(String teacherUid, String rawCode, Instant expiresAt, Instant createdAt, Instant usedAt)` — static factory for reconstruction from persistence; emits no events.
  - `boolean isExpired()` — `Instant.now().isAfter(expiresAt)`.
  - `boolean isUsed()` — `usedAt != null`.
  - `void markUsed()` — sets `usedAt = Instant.now()`.
  - Getters: `getTeacherUid()`, `getRawCode()`, `getExpiresAt()`, `getUsedAt()`, `getCreatedAt()`.
- **Design notes:** The aggregate ID passed to `AggregateRoot<String>` constructor is `teacherUid` (one active code per teacher is the domain invariant). `rawCode` is separate — it's the lookup key but not the aggregate identity. No domain events are defined for PasswordResetCode in this planning scope; `registerEvent` is available but unused. Lombok is NOT used in domain classes — getters are written by hand. No Spring/JPA/Lombok annotations anywhere in this file.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/auth/domain/exception/`.
2. Create `InvalidResetCodeException.java`:
   ```java
   package cl.gradeops.ai.api.auth.domain.exception;

   import cl.gradeops.ai.api.shared.domain.exception.DomainException;

   public class InvalidResetCodeException extends DomainException {
       public InvalidResetCodeException(String reason) {
           super("Invalid reset code: " + reason);
       }
   }
   ```
3. Create `PasswordResetCode.java` in `auth/domain/model/`:
   ```java
   package cl.gradeops.ai.api.auth.domain.model;

   import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;
   import java.time.Instant;

   public class PasswordResetCode extends AggregateRoot<String> {

       private final String rawCode;
       private final Instant expiresAt;
       private final Instant createdAt;
       private Instant usedAt;

       private PasswordResetCode(String teacherUid, String rawCode,
                                  Instant expiresAt, Instant createdAt, Instant usedAt) {
           super(teacherUid);
           this.rawCode   = rawCode;
           this.expiresAt = expiresAt;
           this.createdAt = createdAt;
           this.usedAt    = usedAt;
       }

       public static PasswordResetCode issue(String teacherUid, String rawCode, Instant expiresAt) {
           return new PasswordResetCode(teacherUid, rawCode, expiresAt, Instant.now(), null);
       }

       public static PasswordResetCode restore(String teacherUid, String rawCode,
                                               Instant expiresAt, Instant createdAt, Instant usedAt) {
           return new PasswordResetCode(teacherUid, rawCode, expiresAt, createdAt, usedAt);
       }

       public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
       public boolean isUsed()    { return usedAt != null; }
       public void markUsed()     { this.usedAt = Instant.now(); }

       public String getTeacherUid() { return getId(); }
       public String getRawCode()    { return rawCode; }
       public Instant getExpiresAt() { return expiresAt; }
       public Instant getCreatedAt() { return createdAt; }
       public Instant getUsedAt()    { return usedAt; }
   }
   ```
4. Create `PasswordResetCodeTest.java` in `src/test/java/cl/gradeops/ai/api/auth/domain/`:
   - Test `issue()`: verify `isUsed() == false`, `isExpired() == false` for future expiry, `getTeacherUid()` matches, `getCreatedAt()` is non-null.
   - Test `isExpired()`: returns `true` when `expiresAt` is in the past.
   - Test `isUsed()` before and after `markUsed()`.
   - Test `markUsed()`: sets `usedAt` to non-null.
   - Test `restore()`: reconstructs a used code correctly (`isUsed() == true` when `usedAt` is provided).
5. Run `./mvnw test -Dtest=PasswordResetCodeTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `issue()` creates non-expired, non-used code | `assertThat(code.isUsed()).isFalse()` and `assertThat(code.isExpired()).isFalse()` |
| 2 | `isExpired()` returns `true` for past expiry | `issue(uid, raw, Instant.now().minus(1, HOURS)).isExpired() == true` |
| 3 | `markUsed()` sets `isUsed()` to `true` | `code.markUsed(); assertThat(code.isUsed()).isTrue()` |
| 4 | `restore()` reconstructs state correctly | `restore(uid, raw, future, past, past).isUsed() == true` |
| 5 | No Spring/JPA imports in domain files | `grep -r "springframework\|jakarta.persistence" src/main/java/cl/gradeops/ai/api/auth/domain/` returns 0 |

---

## Done Criteria

- [ ] `auth/domain/model/PasswordResetCode.java` exists, extends `AggregateRoot<String>`, has `issue()`, `restore()`, `markUsed()`, `isExpired()`, `isUsed()`
- [ ] `auth/domain/exception/InvalidResetCodeException.java` exists, extends `DomainException`
- [ ] `PasswordResetCodeTest` covers all 5 invariants above and passes
- [ ] Zero Spring/JPA/Lombok imports in both new domain files
- [ ] `./mvnw test -Dtest=PasswordResetCodeTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-02-auth-bounded-context.md)

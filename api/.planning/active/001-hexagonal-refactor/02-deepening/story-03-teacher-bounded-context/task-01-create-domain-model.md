# ⚛️ TASK 01 — Create teacher domain model (Teacher, TeacherId, AuthProvider, TeacherNotFoundException, TeacherTest)

> **Status:** TODO
> **Workflow:** IMPLEMENT
> **Depends On:** Story 01 complete (AggregateRoot, DomainException, ResourceNotFoundException available)
> [← story file](../story-03-teacher-bounded-context.md)

---

## Objective

Create the `teacher.domain` package with a pure Java `Teacher` aggregate root (extends `AggregateRoot<TeacherId>`), `TeacherId` value object, `AuthProvider` enum, `TeacherNotFoundException`, and `TeacherTest` covering the aggregate's invariants.

---

## Technical Design

- **Approach:** `Teacher` is extracted from `domain.teacher.TeacherEntity` — the JPA entity that currently holds all teacher state. The aggregate has no Spring, JPA, or validation imports. All 13 fields are replicated as plain Java fields. Business mutations happen through named methods (`provision`, `updatePilotFlags`); there are no public setters for business-logic fields. `TeacherId` is a Java `record` wrapping the Firebase UID string. `AuthProvider` is an `enum` mapping to the existing `"EMAIL_PASSWORD"` and `"GOOGLE"` string values stored in the DB. `TeacherNotFoundException` extends `ResourceNotFoundException` (from `shared.domain.exception`) so the existing `GlobalExceptionHandler` maps it to 404 without any additional handler registration.
- **Affected files / components:**
  - `src/main/java/cl/gradeops/ai/api/teacher/domain/model/Teacher.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/domain/model/TeacherId.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/domain/model/AuthProvider.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/domain/exception/TeacherNotFoundException.java` ← NEW
  - `src/test/java/cl/gradeops/ai/api/teacher/domain/model/TeacherTest.java` ← NEW
- **Interfaces / contracts:**
  - `Teacher.getId()` returns `TeacherId`.
  - `Teacher.provision(String firebaseUid, String firstName, String lastName, String email, AuthProvider authProvider) → Teacher` — factory method for new teachers.
  - `Teacher.restore(String firebaseUid, String firstName, String lastName, String email, AuthProvider authProvider, OffsetDateTime createdAt, OffsetDateTime updatedAt, String planType, boolean relatedParty, String offerDetails, String evidenceLink, String flagSetBy, OffsetDateTime flagSetAt) → Teacher` — factory method for DB reconstruction (does not emit events).
  - `Teacher.updatePilotFlags(String planType, Boolean relatedParty, String offerDetails, String evidenceLink, String setBy) → void` — updates only non-null fields, always sets `flagSetAt` and `updatedAt` to `OffsetDateTime.now()`.
- **Design notes:** `Teacher` extends `AggregateRoot<TeacherId>`. `provision()` sets `createdAt` and `updatedAt` to `OffsetDateTime.now()` and defaults `relatedParty = false`. `restore()` sets all fields verbatim with no side-effects. No Lombok on `Teacher` itself — explicit `@Getter` is acceptable, but field mutations go through named methods.

---

## Implementation Steps

1. Create directories:
   - `src/main/java/cl/gradeops/ai/api/teacher/domain/model/`
   - `src/main/java/cl/gradeops/ai/api/teacher/domain/exception/`
2. Create `TeacherId.java`:
   ```java
   package cl.gradeops.ai.api.teacher.domain.model;

   import java.util.Objects;

   public record TeacherId(String value) {
       public TeacherId {
           Objects.requireNonNull(value, "firebaseUid must not be null");
           if (value.isBlank()) throw new IllegalArgumentException("firebaseUid must not be blank");
       }
   }
   ```
3. Create `AuthProvider.java`:
   ```java
   package cl.gradeops.ai.api.teacher.domain.model;

   public enum AuthProvider {
       EMAIL_PASSWORD, GOOGLE
   }
   ```
4. Create `Teacher.java`:
   ```java
   package cl.gradeops.ai.api.teacher.domain.model;

   import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;
   import java.time.OffsetDateTime;

   public class Teacher extends AggregateRoot<TeacherId> {

       private TeacherId id;
       private String firstName;
       private String lastName;
       private String email;
       private AuthProvider authProvider;
       private OffsetDateTime createdAt;
       private OffsetDateTime updatedAt;
       private String planType;
       private boolean relatedParty;
       private String offerDetails;
       private String evidenceLink;
       private String flagSetBy;
       private OffsetDateTime flagSetAt;

       private Teacher() {}

       public static Teacher provision(String firebaseUid, String firstName, String lastName,
                                       String email, AuthProvider authProvider) {
           Teacher t = new Teacher();
           t.id = new TeacherId(firebaseUid);
           t.firstName = firstName;
           t.lastName = lastName;
           t.email = email;
           t.authProvider = authProvider;
           OffsetDateTime now = OffsetDateTime.now();
           t.createdAt = now;
           t.updatedAt = now;
           t.relatedParty = false;
           return t;
       }

       public static Teacher restore(String firebaseUid, String firstName, String lastName,
                                     String email, AuthProvider authProvider,
                                     OffsetDateTime createdAt, OffsetDateTime updatedAt,
                                     String planType, boolean relatedParty, String offerDetails,
                                     String evidenceLink, String flagSetBy, OffsetDateTime flagSetAt) {
           Teacher t = new Teacher();
           t.id = new TeacherId(firebaseUid);
           t.firstName = firstName;
           t.lastName = lastName;
           t.email = email;
           t.authProvider = authProvider;
           t.createdAt = createdAt;
           t.updatedAt = updatedAt;
           t.planType = planType;
           t.relatedParty = relatedParty;
           t.offerDetails = offerDetails;
           t.evidenceLink = evidenceLink;
           t.flagSetBy = flagSetBy;
           t.flagSetAt = flagSetAt;
           return t;
       }

       public void updatePilotFlags(String planType, Boolean relatedParty, String offerDetails,
                                    String evidenceLink, String setBy) {
           if (planType != null)     this.planType = planType;
           if (relatedParty != null) this.relatedParty = relatedParty;
           if (offerDetails != null) this.offerDetails = offerDetails;
           if (evidenceLink != null) this.evidenceLink = evidenceLink;
           if (setBy != null)        this.flagSetBy = setBy;
           OffsetDateTime now = OffsetDateTime.now();
           this.flagSetAt = now;
           this.updatedAt = now;
       }

       @Override public TeacherId getId()        { return id; }
       public String getFirebaseUid()            { return id.value(); }
       public String getFirstName()              { return firstName; }
       public String getLastName()               { return lastName; }
       public String getEmail()                  { return email; }
       public AuthProvider getAuthProvider()     { return authProvider; }
       public OffsetDateTime getCreatedAt()      { return createdAt; }
       public OffsetDateTime getUpdatedAt()      { return updatedAt; }
       public String getPlanType()               { return planType; }
       public boolean isRelatedParty()           { return relatedParty; }
       public String getOfferDetails()           { return offerDetails; }
       public String getEvidenceLink()           { return evidenceLink; }
       public String getFlagSetBy()              { return flagSetBy; }
       public OffsetDateTime getFlagSetAt()      { return flagSetAt; }
   }
   ```
5. Create `TeacherNotFoundException.java`:
   ```java
   package cl.gradeops.ai.api.teacher.domain.exception;

   import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;

   public class TeacherNotFoundException extends ResourceNotFoundException {
       public TeacherNotFoundException(String uid) {
           super("Teacher not found: " + uid);
       }
   }
   ```
6. Create test directory `src/test/java/cl/gradeops/ai/api/teacher/domain/model/`.
7. Create `TeacherTest.java`:
   - `provision()` sets `firstName`, `lastName`, `email`, `authProvider`, non-null `createdAt`/`updatedAt`, `relatedParty = false`.
   - `restore()` sets all fields verbatim including `planType`, `relatedParty = true`, `flagSetAt`.
   - `updatePilotFlags()` with all non-null → all fields updated, `flagSetAt` is non-null, `updatedAt` is refreshed.
   - `updatePilotFlags()` with null `relatedParty` → `relatedParty` unchanged.
   - `TeacherId("")` → `IllegalArgumentException`.
   - `TeacherId(null)` → `NullPointerException`.
8. Run `./mvnw test -Dtest=TeacherTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `provision()` sets default `relatedParty = false` | `assertThat(teacher.isRelatedParty()).isFalse()` |
| 2 | `provision()` sets `createdAt` and `updatedAt` to now | `assertThat(teacher.getCreatedAt()).isNotNull()` |
| 3 | `restore()` sets all 13 fields verbatim | Assert each field matches what was passed in |
| 4 | `updatePilotFlags()` updates only non-null fields | Pass null `relatedParty` → old value preserved |
| 5 | `updatePilotFlags()` always refreshes `flagSetAt` and `updatedAt` | Both non-null after call |
| 6 | `TeacherId` rejects blank and null | `assertThatThrownBy(...)` for both cases |

---

## Done Criteria

- [ ] `Teacher.java` exists in `teacher/domain/model/`; extends `AggregateRoot<TeacherId>`; no Spring, JPA, or validation imports
- [ ] `Teacher` has `provision()` and `restore()` factory methods; private constructor
- [ ] `Teacher.updatePilotFlags()` updates only non-null fields and refreshes timestamps
- [ ] `TeacherId` is a `record` with null/blank validation
- [ ] `AuthProvider` enum has `EMAIL_PASSWORD` and `GOOGLE` values
- [ ] `TeacherNotFoundException` extends `ResourceNotFoundException` (from `shared.domain.exception`)
- [ ] `TeacherTest` passes all 6 cases
- [ ] `./mvnw test -Dtest=TeacherTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-03-teacher-bounded-context.md)

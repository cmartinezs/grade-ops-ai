# ⚛️ TASK 02 — Apply OwnershipVerifier and Denial Logging

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← scope file](../scope-07-cross-teacher-access-denial.md)

---

## Objective

Apply `OwnershipVerifier` to all existing teacher-scoped endpoints and configure structured Cloud Logging for denied access attempts, with teacher id, target resource, and timestamp.

---

## Technical Design

- **Approach:** For each service method that returns a teacher-scoped resource, add a call to `ownershipVerifier.verify(resource.getTeacherFirebaseUid(), authenticatedTeacher.uid(), resourceId)` before returning data. Since the only teacher-scoped endpoint that exists at this point is `GET /assessments` (scope-02), apply it there. Log denied attempts in `OwnershipVerifier.verify()` using SLF4J structured logging (or Cloud Logging's JSON format) with MDC keys `teacher_uid`, `resource_id`, `timestamp`. In Cloud Run, SLF4J structured JSON logs are automatically indexed by Cloud Logging.
- **Affected files / components:**
  - `api/src/main/java/com/gradeops/api/security/OwnershipVerifier.java` (update — add structured log on violation)
  - `api/src/main/java/com/gradeops/api/assessment/AssessmentService.java` (update — add ownership check)
  - `api/src/main/resources/logback-spring.xml` (new or update — configure JSON structured logging for Cloud Run)
- **Interfaces / contracts:** Every service method touching a teacher-scoped resource calls `ownershipVerifier.verify(...)`. Logs include `teacher_uid`, `resource_id`, `path`, `timestamp` as structured fields.
- **Design notes:** The log level for denied attempts is `WARN` (not `ERROR`). Legitimate users don't trigger this; it's a security signal. Cloud Run JSON logs are emitted on stdout; the `logback-spring.xml` must format them as `{"severity": "WARNING", "message": "...", "teacher_uid": "...", ...}` for Cloud Logging to parse correctly.

---

## Implementation Steps

1. Update `OwnershipVerifier.verify()` to log `WARN` with structured fields when UIDs don't match:
   ```java
   log.warn("Cross-teacher access denied: teacher={} resource={}", authenticatedUid, resourceId);
   // MDC or structured args for Cloud Logging JSON format
   ```
2. Update `AssessmentService.java` to call `ownershipVerifier.verify(assessment.getTeacherFirebaseUid(), authenticatedUid, assessmentId)` before returning each assessment (or filter the query to return only owned assessments — prefer the query-level filter for `findAll` use cases).
3. Create `api/src/main/resources/logback-spring.xml` with a JSON appender for the Cloud Run / Cloud Logging format (severity, message, and custom fields).
4. Write `OwnershipLoggingTest.java` verifying that a cross-teacher access attempt produces a `WARN` log entry.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Cross-teacher GET /assessments/{id} | HTTP 404 (same as not-found) | Integration test |
| 2 | Denied attempt produces WARN log | Log entry with `teacher_uid` and `resource_id` fields | `OwnershipLoggingTest` (log capture) |
| 3 | Owner GET /assessments/{id} | HTTP 200 with correct data | Integration test |
| 4 | Logback outputs JSON format | Log line parseable as JSON with `severity` field | `logback-spring.xml` smoke test |

---

## Done Criteria

- [x] `OwnershipVerifier.verify()` logs `WARN` with structured fields on denial.
- [x] `AssessmentService` calls ownership check (or filters by teacher UID at query level).
- [x] Cross-teacher access returns 404 identical to a not-found response.
- [x] `logback-spring.xml` configured for Cloud Logging JSON format.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-07-cross-teacher-access-denial.md)

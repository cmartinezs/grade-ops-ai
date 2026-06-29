# ⚛️ TASK 01 — Add ArchUnit dependency to pom.xml

> **Status:** DONE
> **Workflow:** CONFIGURE
> **Depends On:** —
> [← story file](../story-01-shared-kernel.md)

---

## Objective

Add `com.tngtech.archunit:archunit-junit5:1.3.0` as a `test`-scoped dependency in `pom.xml` so that `HexagonalArchitectureTest` (task-08) can compile and run.

---

## Technical Design

- **Approach:** Add a single `<dependency>` block in the existing `<dependencies>` section of `pom.xml`, after the `com.h2database:h2` test dependency (line 82–86). ArchUnit 1.3.0 is the latest stable release compatible with Java 21 and JUnit 5. No Spring Boot BOM manages ArchUnit, so the version must be declared explicitly.
- **Affected files / components:**
  - `pom.xml` (1 block added, no other changes)
- **Interfaces / contracts:** None — this is a build-only change with no runtime effect.
- **Design notes:** `archunit-junit5` transitively pulls in `archunit-core`; no separate `archunit` artifact needed. Scope must be `test` — ArchUnit must never appear on the production classpath.

---

## Implementation Steps

1. Open `pom.xml`.
2. After the `<dependency>` block for `com.h2database:h2` (lines 82–86), insert:
   ```xml
   <dependency>
       <groupId>com.tngtech.archunit</groupId>
       <artifactId>archunit-junit5</artifactId>
       <version>1.3.0</version>
       <scope>test</scope>
   </dependency>
   ```
3. Run `./mvnw dependency:resolve -q` to confirm Maven resolves the artifact without errors.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Dependency resolves from Maven Central | `./mvnw dependency:resolve -q` exits 0 with no download errors |
| 2 | ArchUnit is on the test classpath only | `./mvnw dependency:tree -Dscope=test \| grep archunit` shows `archunit-junit5:1.3.0:test` |

---

## Done Criteria

- [ ] `pom.xml` contains a `<dependency>` block for `com.tngtech.archunit:archunit-junit5:1.3.0` with `<scope>test</scope>`
- [ ] `./mvnw dependency:resolve -q` exits 0
- [ ] ArchUnit does **not** appear in `./mvnw dependency:tree -Dscope=compile` output
- [ ] No other lines in `pom.xml` were modified
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-01-shared-kernel.md)

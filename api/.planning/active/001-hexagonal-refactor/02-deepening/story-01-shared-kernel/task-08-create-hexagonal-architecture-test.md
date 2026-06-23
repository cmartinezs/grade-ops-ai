# ⚛️ TASK 08 — Create HexagonalArchitectureTest

> **Status:** DONE
> **Workflow:** IMPLEMENT
> **Depends On:** task-01, task-02
> [← story file](../story-01-shared-kernel.md)

---

## Objective

Create `HexagonalArchitectureTest.java` with 4 ArchUnit rules that enforce the hexagonal dependency constraints. The test must compile and pass green after this task — no rules may be violated by the `shared` domain package created in task-02.

---

## Technical Design

- **Approach:** The test is a plain JUnit 5 class annotated with `@AnalyzeClasses`. It uses ArchUnit's fluent API to declare 4 architectural rules. The rules are scoped to the base package `cl.gradeops.ai.api` so they cover all future bounded contexts (auth, teacher, assessment) as those are added in stories 02–04. Writing the test now means each subsequent story is immediately guarded.
- **Affected files / components:**
  - `src/test/java/cl/gradeops/ai/api/HexagonalArchitectureTest.java` ← NEW
- **Interfaces / contracts:** None — this is a test-only file. The 4 rules it encodes are the contract.
- **Design notes on rule 3 (controllers don't hold JPA repos):** The ArchUnit predicate is `implement(org.springframework.data.repository.Repository.class)`. At this point in Story 01, there are no controllers with direct repo injection (that anti-pattern existed in earlier code and will be gone after Story 02–04), so the rule passes. If a future story injects a `JpaRepository` into a controller, this test will catch it immediately.
  - Rule 4 uses package matching on `..adapter.in.web..` → should not access `..adapter.out.persistence..`. This is enforced via `noClasses().that().resideInAPackage("..adapter.in.web..").should().accessClassesThat().resideInAPackage("..adapter.out.persistence..")`.
  - After this task, only `shared.domain.*` exists that's testable against rule 1. Rules 2–4 will gain more coverage as bounded contexts are built in stories 02–04.

---

## Implementation Steps

1. Create `src/test/java/cl/gradeops/ai/api/HexagonalArchitectureTest.java`:
   ```java
   package cl.gradeops.ai.api;

   import com.tngtech.archunit.core.importer.ImportOption;
   import com.tngtech.archunit.junit.AnalyzeClasses;
   import com.tngtech.archunit.junit.ArchTest;
   import com.tngtech.archunit.lang.ArchRule;

   import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

   @AnalyzeClasses(
       packages = "cl.gradeops.ai.api",
       importOptions = ImportOption.DoNotIncludeTests.class
   )
   public class HexagonalArchitectureTest {

       @ArchTest
       static final ArchRule domain_has_no_spring_or_jpa_imports =
           noClasses().that().resideInAPackage("..domain..")
               .should().dependOnClassesThat()
               .resideInAnyPackage("org.springframework..", "jakarta.persistence..");

       @ArchTest
       static final ArchRule application_has_no_infrastructure_imports =
           noClasses().that().resideInAPackage("..application..")
               .should().dependOnClassesThat()
               .resideInAPackage("..infrastructure..");

       @ArchTest
       static final ArchRule controllers_do_not_hold_jpa_repositories =
           noClasses().that().resideInAPackage("..adapter.in.web..")
               .should().dependOnClassesThat()
               .implement(org.springframework.data.repository.Repository.class);

       @ArchTest
       static final ArchRule web_adapters_do_not_access_persistence_classes =
           noClasses().that().resideInAPackage("..adapter.in.web..")
               .should().accessClassesThat()
               .resideInAPackage("..adapter.out.persistence..");
   }
   ```
2. Run `./mvnw test -Dtest=HexagonalArchitectureTest -q` to confirm all 4 rules pass.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | Test class compiles (ArchUnit on classpath from task-01) | `./mvnw test-compile -q` exits 0 |
| 2 | Rule 1: `shared.domain.*` has no Spring/JPA | `./mvnw test -Dtest=HexagonalArchitectureTest#domain_has_no_spring_or_jpa_imports -q` passes |
| 3 | Rule 2: `shared.application.*` (empty at this stage) trivially passes | `./mvnw test -Dtest=HexagonalArchitectureTest#application_has_no_infrastructure_imports -q` passes |
| 4 | Rules 3 and 4: no `..adapter.in.web..` classes at this stage → trivially pass | `./mvnw test -Dtest=HexagonalArchitectureTest -q` exits 0 |

---

## Done Criteria

- [ ] `src/test/java/cl/gradeops/ai/api/HexagonalArchitectureTest.java` exists
- [ ] Class is annotated with `@AnalyzeClasses(packages = "cl.gradeops.ai.api", importOptions = ImportOption.DoNotIncludeTests.class)`
- [ ] All 4 `@ArchTest` fields are declared with the exact rules from the initial planning
- [ ] `./mvnw test -Dtest=HexagonalArchitectureTest -q` exits 0 (all 4 rules pass)
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-01-shared-kernel.md)

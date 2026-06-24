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
    static final ArchRule shared_domain_has_no_spring_or_jpa_imports =
        noClasses().that().resideInAPackage("..shared.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule application_has_no_infrastructure_imports =
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllers_do_not_hold_jpa_repositories =
        noClasses().that().resideInAPackage("..adapter.in.web..")
            .should().dependOnClassesThat()
            .implement(org.springframework.data.repository.Repository.class)
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule web_adapters_do_not_access_persistence_classes =
        noClasses().that().resideInAPackage("..adapter.in.web..")
            .should().accessClassesThat()
            .resideInAPackage("..adapter.out.persistence..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule application_has_no_spring_web_or_jpa_imports =
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework.web..",
                "org.springframework.data..",
                "jakarta.persistence.."
            )
            .allowEmptyShould(true);
}

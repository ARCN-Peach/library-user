package com.library.user.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class UserArchitectureTest {

    private static final String BASE_PKG = "com.library.user";

    private static JavaClasses classes;

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PKG);
    }

    @Test
    void domain_does_not_depend_on_spring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..");
        rule.check(classes);
    }

    @Test
    void domain_does_not_depend_on_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PKG + ".infrastructure..");
        rule.check(classes);
    }

    @Test
    void application_does_not_depend_on_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".application..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PKG + ".infrastructure..");
        rule.check(classes);
    }
}

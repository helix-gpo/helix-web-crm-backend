package com.helix.gpo.web_crm;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularityTest {

    ApplicationModules modules = ApplicationModules.of(WebCrmApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writesDocumentationSnapshot() {
        // Erzeugt bei jedem Testlauf aktuelle PlantUML/AsciiDoc-Diagramme
        // der Modulgrenzen unter target/spring-modulith-docs/ - später
        // nützlich für die "Dokumentation von allem", die du erwähnt hast
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml();
    }

}

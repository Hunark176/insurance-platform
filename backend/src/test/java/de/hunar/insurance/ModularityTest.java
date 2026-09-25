package de.hunar.insurance;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {
    private final ApplicationModules modules = ApplicationModules.of(InsurancePlatformApplication.class);

    @Test
    void verifiesModuleBoundaries() {
        modules.verify();
    }
}

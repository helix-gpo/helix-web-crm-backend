package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.internal.config.AdminBootstrapProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

// creates the first admins on an empty database, does nothing as soon as one active admin exists
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminBootstrapProperties.class)
class AdminBootstrap implements ApplicationRunner {

    private final AdminBootstrapProperties properties;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmployeeService employeeService;

    @Override
    public void run(ApplicationArguments args) {
        if (properties.admins() == null || properties.admins().isEmpty()) {
            return;
        }
        if (employeeRepository.countActiveAdmins() > 0) {
            log.info("Admin-Bootstrap übersprungen - es existiert bereits ein aktiver Administrator.");
            return;
        }

        Role adminRole = roleRepository.findFirstByUnrestrictedTrue()
                .orElseThrow(() -> new IllegalStateException("Keine Administrator-Rolle vorhanden - Migration V6 prüfen."));

        properties.admins().forEach(admin -> {
            employeeService.bootstrapAdmin(admin.email(), admin.firstName(), admin.lastName(), adminRole);
            log.info("Admin {} angelegt und eingeladen.", admin.email());
        });
    }

}

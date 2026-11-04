package com.helix.gpo.web_crm.access.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// env: HELIX_BOOTSTRAP_ADMINS_0_EMAIL, HELIX_BOOTSTRAP_ADMINS_0_FIRST_NAME, HELIX_BOOTSTRAP_ADMINS_0_LAST_NAME, then _1_ ...
@ConfigurationProperties(prefix = "helix.bootstrap")
public record AdminBootstrapProperties(
        List<AdminEntry> admins
) {

    public record AdminEntry(String email, String firstName, String lastName) {
    }

}

package com.helix.gpo.web_crm.security.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

// comma separated in env vars, e.g. "https://www.helix-gpo.com,https://helix-gpo.com"
@Validated
@ConfigurationProperties(prefix = "helix.cors")
record CorsProperties(
        List<String> crmOrigins,
        List<String> publicOrigins
) {
}

package com.helix.gpo.web_crm.security.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "helix.security")
public record PublicSecurityProperties(
        List<String> publicIpAllowlist
) {
}

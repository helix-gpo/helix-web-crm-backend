package com.helix.gpo.web_crm.website.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "helix.security")
public record WebsiteSecurityProperties(
        List<String> publicIpAllowlist
) {
}

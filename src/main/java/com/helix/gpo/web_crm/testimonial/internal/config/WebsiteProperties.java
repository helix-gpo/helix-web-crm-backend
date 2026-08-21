package com.helix.gpo.web_crm.testimonial.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helix.website")
public record WebsiteProperties(
        String baseUrl
) {
}

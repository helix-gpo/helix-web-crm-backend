package com.helix.gpo.web_crm.storage.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helix.s3")
public record S3Properties(
        String bucket,
        String region
) {
}

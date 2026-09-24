package com.helix.gpo.web_crm.access.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helix.security.cognito")
public record CognitoAdminProperties(
        String userPoolId
) {
}

package com.helix.gpo.web_crm.security.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helix.security.cognito")
public record CognitoProperties(
        String clientId
) {
}

package com.helix.gpo.web_crm.notification.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helix.notification")
public record NotificationProperties(
        String senderEmail,
        String senderName,
        String replyToEmail,
        String logoUrl,
        String region
) {
}

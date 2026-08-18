package com.helix.gpo.web_crm.notification.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
@EnableConfigurationProperties(NotificationProperties.class)
class SesConfig {

    @Bean
    SesV2Client sesV2Client(NotificationProperties properties) {
        return SesV2Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(ProfileCredentialsProvider.create("helix-crm"))
                .build();
    }

}

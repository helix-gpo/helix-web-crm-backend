package com.helix.gpo.web_crm.access.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
@EnableConfigurationProperties(CognitoAdminProperties.class)
class CognitoAdminClientConfig {

    @Bean
    CognitoIdentityProviderClient cognitoIdentityProviderClient(CognitoAdminProperties properties) {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(properties.region()))
                .build();
    }

}

package com.helix.gpo.web_crm.access.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
@EnableConfigurationProperties(CognitoAdminProperties.class)
class CognitoAdminClientConfig {

    @Bean
    CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("helix-crm"))
                .build();
    }

}

package com.helix.gpo.web_crm.security.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
class JwtDecoderConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private final CognitoTokenValidator cognitoTokenValidator;

    @Bean
    JwtDecoder jwtDecoder() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        // keys stay cached until cognito rotates them (unknown kid triggers a refetch)
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri)
                .restOperations(new RestTemplate(requestFactory))
                .cache(new ConcurrentMapCache("cognito-jwks"))
                .build();

        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                new JwtTimestampValidator(),
                cognitoTokenValidator
        );

        decoder.setJwtValidator(combinedValidator);
        return decoder;
    }

}

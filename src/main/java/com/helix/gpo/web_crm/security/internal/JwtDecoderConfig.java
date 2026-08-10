package com.helix.gpo.web_crm.security.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@RequiredArgsConstructor
class JwtDecoderConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private final CognitoTokenValidator cognitoTokenValidator;

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuerUri);

        // Standard-Validierung (Signatur, Issuer, Ablaufzeit) + unser Cognito-Check kombiniert
        OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt> defaultValidator =
                JwtValidators.createDefaultWithIssuer(issuerUri);

        OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt> combinedValidator =
                new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                        defaultValidator,
                        new JwtTimestampValidator(),
                        cognitoTokenValidator
                );

        decoder.setJwtValidator(combinedValidator);
        return decoder;
    }

}

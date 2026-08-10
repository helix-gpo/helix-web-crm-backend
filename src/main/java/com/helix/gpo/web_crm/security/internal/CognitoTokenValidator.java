package com.helix.gpo.web_crm.security.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CognitoTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final CognitoProperties cognitoProperties;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String tokenUse = jwt.getClaimAsString("token_use");
        if (!"access".equals(tokenUse)) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Expected an access token, got: " + tokenUse, null));
        }

        String clientId = jwt.getClaimAsString("client_id");
        if (!cognitoProperties.clientId().equals(clientId)) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Token was not issued for this client", null));
        }

        return OAuth2TokenValidatorResult.success();
    }

}

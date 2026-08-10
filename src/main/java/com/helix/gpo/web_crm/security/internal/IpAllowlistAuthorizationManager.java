package com.helix.gpo.web_crm.security.internal;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
class IpAllowlistAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PublicSecurityProperties properties;
    private List<IpAddressMatcher> matchers;

    private List<IpAddressMatcher> matchers() {
        if (matchers == null) {
            matchers = properties.publicIpAllowlist().stream()
                    .map(String::trim)
                    .filter(cidr -> !cidr.isBlank())
                    .map(IpAddressMatcher::new)
                    .toList();
        }
        return matchers;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication,
                                                   RequestAuthorizationContext context) {
        String remoteAddr = context.getRequest().getRemoteAddr();
        boolean allowed = matchers().stream().anyMatch(matcher -> matcher.matches(remoteAddr));
        return new AuthorizationDecision(allowed);
    }

}

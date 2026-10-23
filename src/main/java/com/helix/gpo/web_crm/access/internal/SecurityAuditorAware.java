package com.helix.gpo.web_crm.access.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class SecurityAuditorAware implements AuditorAware<String> {

    private final CurrentUserDirectory currentUserDirectory;

    // access tokens carry no email claim - resolve via the employee cache filled by CurrentUserFilter
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("system");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            return Optional.of(currentUserDirectory.emailOf(sub).orElse(sub));
        }

        return Optional.of(authentication.getName());
    }

}

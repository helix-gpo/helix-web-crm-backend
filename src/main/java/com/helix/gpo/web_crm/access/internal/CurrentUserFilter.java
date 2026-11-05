package com.helix.gpo.web_crm.access.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// runs after the spring security filter chain (order -100), so the authentication is already set
@Component
@Order()
@RequiredArgsConstructor
class CurrentUserFilter extends OncePerRequestFilter {

    private final CurrentUserDirectory currentUserDirectory;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            if (currentUserDirectory.isDeactivated(jwt.getSubject())) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("""
                {"status":401,"code":"ACCOUNT_DEACTIVATED","detail":"Dein Zugang wurde deaktiviert."}""");
                return;
            }
            currentUserDirectory.resolve(jwt.getSubject());
        }
        filterChain.doFilter(request, response);
    }

}

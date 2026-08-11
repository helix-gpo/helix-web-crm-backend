package com.helix.gpo.web_crm.security.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({PublicSecurityProperties.class, CognitoProperties.class})
class PublicSecurityConfig {

    private final IpAllowlistAuthorizationManager ipAllowlistManager;

    @Bean
    @Order(1)
    SecurityFilterChain publicApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/public/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().access(ipAllowlistManager)
                );
        return http.build();
    }

    // Sicherheitsnetz: alles, was nicht /api/public/** oder /api/crm/** ist,
    // wird pauschal blockiert - verhindert versehentlich offene Endpunkte
    // (z.B. Actuator), falls die mal ohne explizite Config dazukommen
    @Bean
    @Order(3)
    SecurityFilterChain defaultDenyFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll()
                );
        return http.build();
    }

}

package com.helix.gpo.web_crm.security.internal;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(CognitoProperties.class)
class PublicSecurityConfig {

    // Echt öffentlich - erreichbar von jedem Website-Besucher-Browser, nicht
    // mehr IP-beschränkt. Die IP-Allowlist (IpAllowlistAuthorizationManager)
    // war für Server-zu-Server-Calls gedacht und hätte hier jeden echten
    // Website-Besucher ausgesperrt.
    @Bean
    @Order(1)
    SecurityFilterChain publicApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/public/**")
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
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

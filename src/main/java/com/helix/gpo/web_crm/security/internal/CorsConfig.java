package com.helix.gpo.web_crm.security.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration crmConfiguration = new CorsConfiguration();
        // Lokale CRM-SPA - produktive Domain (z.B. https://crm.helix-gpo.com) hier später ergänzen
        crmConfiguration.setAllowedOrigins(List.of("http://localhost:4200"));
        crmConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        crmConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        crmConfiguration.setAllowCredentials(true);

        // Öffentliche Website - kein Auth-Header, daher allowCredentials(false).
        // localhost:4201, weil die Website standardmäßig auch auf 4200 laufen
        // würde (Konflikt mit dem CRM-Dev-Server) - siehe Hinweis unten
        CorsConfiguration publicConfiguration = new CorsConfiguration();
        publicConfiguration.setAllowedOrigins(List.of(
                "http://localhost:4201",
                "https://helix-gpo.com",
                "https://www.helix-gpo.com"
        ));
        publicConfiguration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        publicConfiguration.setAllowedHeaders(List.of("Content-Type"));
        publicConfiguration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/crm/**", crmConfiguration);
        source.registerCorsConfiguration("/api/public/**", publicConfiguration);
        return source;
    }

}

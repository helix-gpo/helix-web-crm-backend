package com.helix.gpo.web_crm.security.internal;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration crmConfiguration = new CorsConfiguration();
        crmConfiguration.setAllowedOrigins(properties.crmOrigins());
        crmConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        crmConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        crmConfiguration.setAllowCredentials(true);

        CorsConfiguration publicConfiguration = new CorsConfiguration();
        publicConfiguration.setAllowedOrigins(properties.publicOrigins());
        publicConfiguration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        publicConfiguration.setAllowedHeaders(List.of("Content-Type"));
        publicConfiguration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/crm/**", crmConfiguration);
        source.registerCorsConfiguration("/api/public/**", publicConfiguration);
        return source;
    }

}

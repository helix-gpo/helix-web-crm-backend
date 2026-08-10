package com.helix.gpo.web_crm.website.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableConfigurationProperties(WebsiteSecurityProperties.class)
class WebsiteSecurityConfig {

    @Bean
    SecurityFilterChain publicApiSecurityFilterChain(HttpSecurity http,
                                                     IpAllowlistAuthorizationManager ipAllowlistManager) throws Exception {
        http
                .securityMatcher("/api/public/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().access(ipAllowlistManager)
                );
        return http.build();
    }

}

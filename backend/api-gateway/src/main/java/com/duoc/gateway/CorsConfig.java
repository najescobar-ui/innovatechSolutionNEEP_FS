package com.duoc.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS expuesto como CorsConfigurationSource (no CorsWebFilter) para que
 * Spring Security lo agarre via http.cors(...) y procese el preflight
 * OPTIONS antes de exigir el Bearer token.
 */
@Configuration
public class CorsConfig {

    /**
     * Orígenes permitidos, configurables por entorno (CORS_ALLOWED_ORIGINS).
     * Default: localhost para desarrollo. En despliegue se inyecta la IP/dominio del frontend.
     */
    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setExposedHeaders(List.of("Location"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}

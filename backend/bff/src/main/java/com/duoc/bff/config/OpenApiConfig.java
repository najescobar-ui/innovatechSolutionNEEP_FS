package com.duoc.bff.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la documentacion OpenAPI/Swagger del BFF. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("BFF API")
                .version("0.1.0")
                .description("Backend for Frontend: auth, dashboard y agregacion para Innovatech Solutions."));
    }
}

package com.duoc.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la documentacion OpenAPI/Swagger del servicio. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("MS Analytics API")
                .version("0.1.0")
                .description("KPIs, utilizacion y series historicas (Innovatech Solutions)."));
    }
}

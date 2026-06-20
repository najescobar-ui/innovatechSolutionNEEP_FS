package com.duoc.resources.config;

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
                .title("MS Resources API")
                .version("0.1.0")
                .description("Gestion de recursos humanos y disponibilidad (Innovatech Solutions)."));
    }
}

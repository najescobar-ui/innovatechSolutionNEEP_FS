package com.duoc.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void buildsCorsConfigurationSource() {
        var cfg = new CorsConfig();
        ReflectionTestUtils.setField(cfg, "allowedOrigins", List.of("http://localhost:3000"));

        assertThat(cfg.corsConfigurationSource()).isNotNull();
    }
}

package cl.duoc.innovatech.bff.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class BffClientsConfig {

    // Ojo: el eureka-client resuelve el RestClient.Builder por tipo. Si dejamos
    // solo el @LoadBalanced, se rompe el registro de heartbeats.
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder lbRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> proyectosBreaker() {
        var cb = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(50f)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(1)
                .build();
        var tl = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .build();
        return factory -> factory.configure(
                b -> b.circuitBreakerConfig(cb).timeLimiterConfig(tl).build(),
                "proyectos");
    }
}

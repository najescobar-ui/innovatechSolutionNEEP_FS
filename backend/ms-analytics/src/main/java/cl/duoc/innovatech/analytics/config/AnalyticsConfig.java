package cl.duoc.innovatech.analytics.config;

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
public class AnalyticsConfig {

    /**
     * Misma trampa que en bff: el eureka client toma el RestClient.Builder
     * por tipo, asi que dejamos un Primary sin @LoadBalanced.
     */
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

    /**
     * Un solo Customizer configura ambos breakers (projects y resources)
     * con los mismos parametros, los dejo iguales por simplicidad.
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> breakersConfig() {
        var cb = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(50f)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(1)
                .build();
        var tl = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .build();
        return factory -> {
            factory.configure(b -> b.circuitBreakerConfig(cb).timeLimiterConfig(tl).build(), "projects");
            factory.configure(b -> b.circuitBreakerConfig(cb).timeLimiterConfig(tl).build(), "resources");
        };
    }
}

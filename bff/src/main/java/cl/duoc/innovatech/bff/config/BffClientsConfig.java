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

    // Default (non-LB) RestClient.Builder. Marked @Primary so the eureka
    // client (RestClientEurekaHttpClient) — which resolves the builder by
    // type, not by qualifier — gets this one and reaches `eureka-server:8761`
    // directly. Without @Primary the context has two candidate builders and
    // the eureka client fails with "expected single matching bean but found 2".
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    // @LoadBalanced lets us use Eureka logical names as URL hosts
    // (internal docs §5 rule 6): `http://ms-proyectos/...` resolves through
    // the registry instead of a hardcoded container hostname. Consumers
    // must opt in by qualifying the injection point with @LoadBalanced.
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // Circuit Breaker for outbound calls to ms-proyectos.
    // Config matches internal docs §6:
    //   - call timeout: 3s
    //   - opens when failure rate > 50% in a sliding window of 10 requests
    //   - stays OPEN for 30s, then one probe request decides CLOSED vs OPEN.
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> proyectosBreakerCustomizer() {
        return factory -> factory.configure(builder -> builder
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3))
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .failureRateThreshold(50.0f)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .permittedNumberOfCallsInHalfOpenState(1)
                        .build())
                .build(), "proyectos");
    }
}

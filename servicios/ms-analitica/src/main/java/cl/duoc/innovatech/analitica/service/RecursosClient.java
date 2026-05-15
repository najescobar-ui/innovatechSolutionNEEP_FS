package cl.duoc.innovatech.analitica.service;

import cl.duoc.innovatech.analitica.dto.RecursoView;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class RecursosClient {

    private static final ParameterizedTypeReference<List<RecursoView>> TIPO_LISTA =
            new ParameterizedTypeReference<>() {};

    private final RestClient http;
    private final CircuitBreakerFactory<?, ?> breakers;

    public RecursosClient(@LoadBalanced RestClient.Builder builder,
                          CircuitBreakerFactory<?, ?> breakers) {
        this.http = builder.baseUrl("http://ms-recursos").build();
        this.breakers = breakers;
    }

    public List<RecursoView> listar() {
        return breakers.create("recursos").run(
                () -> http.get().uri("/recursos").retrieve().body(TIPO_LISTA),
                ex -> { throw new IllegalStateException("ms-recursos: " + ex.getMessage(), ex); }
        );
    }
}

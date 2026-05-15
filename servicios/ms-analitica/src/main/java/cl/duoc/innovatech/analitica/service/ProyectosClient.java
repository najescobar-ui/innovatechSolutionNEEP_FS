package cl.duoc.innovatech.analitica.service;

import cl.duoc.innovatech.analitica.dto.ProyectoView;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProyectosClient {

    private final RestClient http;
    private final CircuitBreakerFactory<?, ?> breakers;

    public ProyectosClient(@LoadBalanced RestClient.Builder builder,
                           CircuitBreakerFactory<?, ?> breakers) {
        this.http = builder.baseUrl("http://ms-proyectos").build();
        this.breakers = breakers;
    }

    public List<ProyectoView> listar() {
        return breakers.create("proyectos").run(
                () -> http.get().uri("/proyectos").retrieve()
                        .body(new ParameterizedTypeReference<List<ProyectoView>>() {}),
                ex -> { throw new IllegalStateException("ms-proyectos: " + ex.getMessage(), ex); }
        );
    }
}

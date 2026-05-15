package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.ProyectoSummary;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

// Wraps the HTTP call to ms-proyectos. The @LoadBalanced RestClient.Builder
// resolves the logical Eureka service id (`ms-proyectos`) to an actual
// instance — no hardcoded container hostnames.
@Component
public class ProyectosClient {

    private static final ParameterizedTypeReference<List<ProyectoSummary>> LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public ProyectosClient(@LoadBalanced RestClient.Builder loadBalancedBuilder) {
        this.restClient = loadBalancedBuilder.baseUrl("http://ms-proyectos").build();
    }

    public List<ProyectoSummary> listar() {
        return restClient.get()
                .uri("/proyectos")
                .retrieve()
                .body(LIST_TYPE);
    }
}

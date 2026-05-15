package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.ProyectoSummary;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProyectosClient {

    private final RestClient http;

    public ProyectosClient(@LoadBalanced RestClient.Builder builder) {
        // baseUrl con el service id de Eureka, no el hostname Docker
        this.http = builder.baseUrl("http://ms-proyectos").build();
    }

    public List<ProyectoSummary> listar() {
        return http.get()
                .uri("/proyectos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProyectoSummary>>() {});
    }
}

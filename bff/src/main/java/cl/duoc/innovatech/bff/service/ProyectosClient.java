package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.ActualizarProyectoRequest;
import cl.duoc.innovatech.bff.domain.CrearProyectoRequest;
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

    public ProyectoSummary crear(CrearProyectoRequest req) {
        return http.post()
                .uri("/proyectos")
                .body(req)
                .retrieve()
                .body(ProyectoSummary.class);
    }

    public void eliminar(Long id) {
        http.delete()
                .uri("/proyectos/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public ProyectoSummary actualizar(Long id, ActualizarProyectoRequest req) {
        return http.patch()
                .uri("/proyectos/{id}", id)
                .body(req)
                .retrieve()
                .body(ProyectoSummary.class);
    }
}

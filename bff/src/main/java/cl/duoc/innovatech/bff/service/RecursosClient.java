package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.ActualizarRecursoRequest;
import cl.duoc.innovatech.bff.domain.CrearRecursoRequest;
import cl.duoc.innovatech.bff.domain.RecursoSummary;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class RecursosClient {

    private final RestClient http;

    public RecursosClient(@LoadBalanced RestClient.Builder builder) {
        this.http = builder.baseUrl("http://ms-recursos").build();
    }

    public List<RecursoSummary> listar() {
        return http.get()
                .uri("/recursos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<RecursoSummary>>() {});
    }

    public RecursoSummary crear(CrearRecursoRequest req) {
        return http.post()
                .uri("/recursos")
                .body(req)
                .retrieve()
                .body(RecursoSummary.class);
    }

    public void eliminar(Long id) {
        http.delete()
                .uri("/recursos/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public RecursoSummary actualizar(Long id, ActualizarRecursoRequest req) {
        return http.patch()
                .uri("/recursos/{id}", id)
                .body(req)
                .retrieve()
                .body(RecursoSummary.class);
    }
}

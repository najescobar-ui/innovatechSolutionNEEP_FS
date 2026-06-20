package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.CreateResourceRequest;
import cl.duoc.innovatech.bff.domain.ResourceSummary;
import cl.duoc.innovatech.bff.domain.UpdateResourceRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ResourcesClient {

    private final RestClient http;

    public ResourcesClient(@LoadBalanced RestClient.Builder builder) {
        this.http = builder.baseUrl("http://ms-resources").build();
    }

    public List<ResourceSummary> list() {
        return http.get()
                .uri("/resources")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ResourceSummary>>() {});
    }

    /** Resuelve un recurso por email. Devuelve null si no existe (404 no lanza). */
    public ResourceSummary byEmail(String email) {
        return http.get()
                .uri(uri -> uri.path("/resources/by-email").queryParam("email", email).build())
                .retrieve()
                .onStatus(s -> s.value() == 404, (request, response) -> { })
                .body(ResourceSummary.class);
    }

    public ResourceSummary create(CreateResourceRequest req) {
        return http.post()
                .uri("/resources")
                .body(req)
                .retrieve()
                .body(ResourceSummary.class);
    }

    public void delete(Long id) {
        http.delete()
                .uri("/resources/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public ResourceSummary update(Long id, UpdateResourceRequest req) {
        return http.patch()
                .uri("/resources/{id}", id)
                .body(req)
                .retrieve()
                .body(ResourceSummary.class);
    }
}

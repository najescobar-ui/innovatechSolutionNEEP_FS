package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.CreateProjectRequest;
import cl.duoc.innovatech.bff.domain.ProjectSummary;
import cl.duoc.innovatech.bff.domain.UpdateProjectRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProjectsClient {

    private final RestClient http;

    public ProjectsClient(@LoadBalanced RestClient.Builder builder) {
        // baseUrl con el service id de Eureka, no el hostname Docker
        this.http = builder.baseUrl("http://ms-projects").build();
    }

    public List<ProjectSummary> list() {
        return http.get()
                .uri("/projects")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProjectSummary>>() {});
    }

    public ProjectSummary create(CreateProjectRequest req) {
        return http.post()
                .uri("/projects")
                .body(req)
                .retrieve()
                .body(ProjectSummary.class);
    }

    public void delete(Long id) {
        http.delete()
                .uri("/projects/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public ProjectSummary update(Long id, UpdateProjectRequest req) {
        return http.patch()
                .uri("/projects/{id}", id)
                .body(req)
                .retrieve()
                .body(ProjectSummary.class);
    }
}

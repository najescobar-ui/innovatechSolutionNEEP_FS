package cl.duoc.innovatech.analytics.service;

import cl.duoc.innovatech.analytics.dto.ProjectView;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProjectsClient {

    private final RestClient http;
    private final CircuitBreakerFactory<?, ?> breakers;

    public ProjectsClient(@LoadBalanced RestClient.Builder builder,
                          CircuitBreakerFactory<?, ?> breakers) {
        this.http = builder.baseUrl("http://ms-projects").build();
        this.breakers = breakers;
    }

    public List<ProjectView> list() {
        return breakers.create("projects").run(
                () -> http.get().uri("/projects").retrieve()
                        .body(new ParameterizedTypeReference<List<ProjectView>>() {}),
                ex -> { throw new IllegalStateException("ms-projects: " + ex.getMessage(), ex); }
        );
    }
}

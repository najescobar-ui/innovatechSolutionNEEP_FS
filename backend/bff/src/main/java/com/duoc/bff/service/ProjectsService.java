package com.duoc.bff.service;

import com.duoc.bff.domain.CreateProjectRequest;
import com.duoc.bff.domain.ProjectSummary;
import com.duoc.bff.domain.ProjectsResponse;
import com.duoc.bff.domain.UpdateProjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectsService {

    private static final Logger log = LoggerFactory.getLogger(ProjectsService.class);

    private final ProjectsClient client;
    private final CircuitBreakerFactory<?, ?> breakers;

    public ProjectsService(ProjectsClient client, CircuitBreakerFactory<?, ?> breakers) {
        this.client = client;
        this.breakers = breakers;
    }

    public ProjectsResponse list() {
        return breakers.create("projects").run(
                () -> ProjectsResponse.ok(client.list()),
                ex -> {
                    log.warn("ms-projects cayo, devolviendo fallback ({})", ex.getMessage());
                    return ProjectsResponse.unavailable();
                });
    }

    /** Crear no usa fallback: si falla, el llamador necesita saberlo. */
    public ProjectSummary create(CreateProjectRequest req) {
        return client.create(req);
    }

    public void delete(Long id) {
        client.delete(id);
    }

    public ProjectSummary update(Long id, UpdateProjectRequest req) {
        return client.update(id, req);
    }
}

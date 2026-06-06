package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.CreateResourceRequest;
import cl.duoc.innovatech.bff.domain.ResourceSummary;
import cl.duoc.innovatech.bff.domain.ResourcesResponse;
import cl.duoc.innovatech.bff.domain.UpdateResourceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

@Service
public class ResourcesService {

    private static final Logger log = LoggerFactory.getLogger(ResourcesService.class);

    private final ResourcesClient client;
    private final CircuitBreakerFactory<?, ?> breakers;

    public ResourcesService(ResourcesClient client, CircuitBreakerFactory<?, ?> breakers) {
        this.client = client;
        this.breakers = breakers;
    }

    public ResourcesResponse list() {
        return breakers.create("resources").run(
                () -> ResourcesResponse.ok(client.list()),
                ex -> {
                    log.warn("ms-resources cayo, devolviendo fallback ({})", ex.getMessage());
                    return ResourcesResponse.unavailable();
                });
    }

    public ResourceSummary create(CreateResourceRequest req) {
        return client.create(req);
    }

    public void delete(Long id) {
        client.delete(id);
    }

    public ResourceSummary update(Long id, UpdateResourceRequest req) {
        return client.update(id, req);
    }
}

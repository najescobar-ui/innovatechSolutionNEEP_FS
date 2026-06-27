package com.duoc.analytics.service;

import com.duoc.analytics.dto.ResourceView;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ResourcesClient {

    private static final ParameterizedTypeReference<List<ResourceView>> RESOURCE_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient http;
    private final CircuitBreakerFactory<?, ?> breakers;

    public ResourcesClient(@LoadBalanced RestClient.Builder builder,
                           CircuitBreakerFactory<?, ?> breakers) {
        this.http = builder.baseUrl("http://ms-resources").build();
        this.breakers = breakers;
    }

    public List<ResourceView> list() {
        return breakers.create("resources").run(
                () -> http.get().uri("/resources").retrieve().body(RESOURCE_LIST_TYPE),
                ex -> { throw new IllegalStateException("ms-resources: " + ex.getMessage(), ex); }
        );
    }
}

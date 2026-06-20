package com.duoc.analytics.service;

import com.duoc.analytics.dto.TaskView;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TasksClient {

    private final RestClient http;
    private final CircuitBreakerFactory<?, ?> breakers;

    public TasksClient(@LoadBalanced RestClient.Builder builder,
                       CircuitBreakerFactory<?, ?> breakers) {
        this.http = builder.baseUrl("http://ms-projects").build();
        this.breakers = breakers;
    }

    public List<TaskView> list() {
        return breakers.create("tasks").run(
                () -> http.get().uri("/tasks").retrieve()
                        .body(new ParameterizedTypeReference<List<TaskView>>() {}),
                ex -> { throw new IllegalStateException("ms-projects/tasks: " + ex.getMessage(), ex); }
        );
    }
}

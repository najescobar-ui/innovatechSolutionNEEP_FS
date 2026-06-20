package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.CreateTaskRequest;
import cl.duoc.innovatech.bff.domain.TaskSummary;
import cl.duoc.innovatech.bff.domain.UpdateTaskRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TasksClient {

    private final RestClient http;

    public TasksClient(@LoadBalanced RestClient.Builder builder) {
        this.http = builder.baseUrl("http://ms-projects").build();
    }

    public List<TaskSummary> list(Long projectId, Long assigneeResourceId, String status) {
        return http.get()
                .uri(uri -> {
                    var b = uri.path("/tasks");
                    if (projectId != null) b.queryParam("projectId", projectId);
                    if (assigneeResourceId != null) b.queryParam("assigneeResourceId", assigneeResourceId);
                    if (status != null && !status.isBlank()) b.queryParam("status", status);
                    return b.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<TaskSummary>>() {});
    }

    public TaskSummary create(CreateTaskRequest req) {
        return http.post()
                .uri("/tasks")
                .body(req)
                .retrieve()
                .body(TaskSummary.class);
    }

    public void delete(Long id) {
        http.delete()
                .uri("/tasks/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    public TaskSummary update(Long id, UpdateTaskRequest req) {
        return http.patch()
                .uri("/tasks/{id}", id)
                .body(req)
                .retrieve()
                .body(TaskSummary.class);
    }
}

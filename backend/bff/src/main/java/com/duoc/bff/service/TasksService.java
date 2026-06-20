package com.duoc.bff.service;

import com.duoc.bff.domain.CreateTaskRequest;
import com.duoc.bff.domain.TaskSummary;
import com.duoc.bff.domain.TasksResponse;
import com.duoc.bff.domain.UpdateTaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TasksService {

    private static final Logger log = LoggerFactory.getLogger(TasksService.class);

    private final TasksClient client;
    private final CircuitBreakerFactory<?, ?> breakers;

    public TasksService(TasksClient client, CircuitBreakerFactory<?, ?> breakers) {
        this.client = client;
        this.breakers = breakers;
    }

    public TasksResponse list(Long projectId, Long assigneeResourceId, String status) {
        return breakers.create("tasks").run(
                () -> TasksResponse.ok(client.list(projectId, assigneeResourceId, status)),
                ex -> {
                    log.warn("ms-projects/tasks cayo, devolviendo fallback ({})", ex.getMessage());
                    return TasksResponse.unavailable();
                });
    }

    /** Tareas de un recurso. Usado por el dashboard DEV; degrada a vacio si falla. */
    public List<TaskSummary> listForAssignee(Long resourceId) {
        return breakers.create("tasks").run(
                () -> client.list(null, resourceId, null),
                ex -> {
                    log.warn("ms-projects/tasks (assignee) cayo, fallback vacio ({})", ex.getMessage());
                    return List.of();
                });
    }

    public TaskSummary create(CreateTaskRequest req) {
        return client.create(req);
    }

    public void delete(Long id) {
        client.delete(id);
    }

    public TaskSummary update(Long id, UpdateTaskRequest req) {
        return client.update(id, req);
    }
}

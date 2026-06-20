package com.duoc.bff.web;

import com.duoc.bff.domain.CreateTaskRequest;
import com.duoc.bff.domain.TaskSummary;
import com.duoc.bff.domain.TasksResponse;
import com.duoc.bff.domain.UpdateTaskRequest;
import com.duoc.bff.service.TasksService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class TasksController {

    private final TasksService tasks;

    public TasksController(TasksService tasks) {
        this.tasks = tasks;
    }

    @GetMapping("/tasks")
    public TasksResponse list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeResourceId,
            @RequestParam(required = false) String status) {
        return tasks.list(projectId, assigneeResourceId, status);
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskSummary> create(@RequestBody CreateTaskRequest req) {
        var created = tasks.create(req);
        return ResponseEntity
                .created(URI.create("/tasks/" + created.id()))
                .body(created);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tasks.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tasks/{id}")
    public TaskSummary update(@PathVariable Long id, @RequestBody UpdateTaskRequest req) {
        return tasks.update(id, req);
    }
}

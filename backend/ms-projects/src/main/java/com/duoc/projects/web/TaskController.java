package com.duoc.projects.web;

import com.duoc.projects.dto.CreateTaskRequest;
import com.duoc.projects.dto.TaskDto;
import com.duoc.projects.dto.UpdateTaskRequest;
import com.duoc.projects.entity.TaskStatus;
import com.duoc.projects.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<TaskDto> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeResourceId,
            @RequestParam(required = false) TaskStatus status) {
        return service.list(projectId, assigneeResourceId, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaskDto> create(@RequestBody CreateTaskRequest req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create("/tasks/" + created.id()))
                .body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskDto> patch(@PathVariable Long id, @RequestBody UpdateTaskRequest req) {
        return service.patch(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

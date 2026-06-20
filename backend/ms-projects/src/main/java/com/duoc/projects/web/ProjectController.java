package com.duoc.projects.web;

import com.duoc.projects.dto.UpdateProjectRequest;
import com.duoc.projects.dto.CreateProjectRequest;
import com.duoc.projects.dto.ProjectDto;
import com.duoc.projects.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProjectDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProjectDto> create(@RequestBody CreateProjectRequest req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create("/projects/" + created.id()))
                .body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectDto> patch(@PathVariable Long id, @RequestBody UpdateProjectRequest req) {
        return service.patch(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

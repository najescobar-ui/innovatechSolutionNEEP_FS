package com.duoc.bff.web;

import com.duoc.bff.domain.CreateProjectRequest;
import com.duoc.bff.domain.ProjectSummary;
import com.duoc.bff.domain.ProjectsResponse;
import com.duoc.bff.domain.UpdateProjectRequest;
import com.duoc.bff.service.ProjectsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class ProjectsController {

    private final ProjectsService projects;

    public ProjectsController(ProjectsService projects) {
        this.projects = projects;
    }

    @GetMapping("/projects")
    public ProjectsResponse list() {
        return projects.list();
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectSummary> create(@RequestBody CreateProjectRequest req) {
        var created = projects.create(req);
        return ResponseEntity
                .created(URI.create("/projects/" + created.id()))
                .body(created);
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projects.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/projects/{id}")
    public ProjectSummary update(@PathVariable Long id, @RequestBody UpdateProjectRequest req) {
        return projects.update(id, req);
    }
}

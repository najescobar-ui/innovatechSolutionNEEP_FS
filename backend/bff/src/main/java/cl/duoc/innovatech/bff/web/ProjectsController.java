package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.CreateProjectRequest;
import cl.duoc.innovatech.bff.domain.ProjectSummary;
import cl.duoc.innovatech.bff.domain.ProjectsResponse;
import cl.duoc.innovatech.bff.domain.UpdateProjectRequest;
import cl.duoc.innovatech.bff.service.ProjectsService;
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

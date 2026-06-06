package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.CreateResourceRequest;
import cl.duoc.innovatech.bff.domain.ResourceSummary;
import cl.duoc.innovatech.bff.domain.ResourcesResponse;
import cl.duoc.innovatech.bff.domain.UpdateResourceRequest;
import cl.duoc.innovatech.bff.service.ResourcesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourcesController {

    private final ResourcesService resources;

    public ResourcesController(ResourcesService resources) {
        this.resources = resources;
    }

    @GetMapping("/resources")
    public ResourcesResponse list() {
        return resources.list();
    }

    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceSummary create(@RequestBody CreateResourceRequest req) {
        return resources.create(req);
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resources.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/resources/{id}")
    public ResourceSummary update(@PathVariable Long id, @RequestBody UpdateResourceRequest req) {
        return resources.update(id, req);
    }
}

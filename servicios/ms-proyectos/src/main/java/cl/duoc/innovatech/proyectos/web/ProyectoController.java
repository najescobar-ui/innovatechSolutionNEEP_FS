package cl.duoc.innovatech.proyectos.web;

import cl.duoc.innovatech.proyectos.dto.CrearProyectoRequest;
import cl.duoc.innovatech.proyectos.dto.ProyectoDto;
import cl.duoc.innovatech.proyectos.service.ProyectoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/proyectos")
public class ProyectoController {

    private final ProyectoService service;

    public ProyectoController(ProyectoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProyectoDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectoDto> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProyectoDto> create(@RequestBody CrearProyectoRequest req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create("/proyectos/" + created.id()))
                .body(created);
    }
}

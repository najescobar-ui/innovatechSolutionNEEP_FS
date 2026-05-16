package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.ActualizarProyectoRequest;
import cl.duoc.innovatech.bff.domain.CrearProyectoRequest;
import cl.duoc.innovatech.bff.domain.ProyectoSummary;
import cl.duoc.innovatech.bff.domain.ProyectosResponse;
import cl.duoc.innovatech.bff.service.ProyectosService;
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
public class ProyectosController {

    private final ProyectosService proyectos;

    public ProyectosController(ProyectosService proyectos) {
        this.proyectos = proyectos;
    }

    @GetMapping("/proyectos")
    public ProyectosResponse listar() {
        return proyectos.listar();
    }

    @PostMapping("/proyectos")
    public ResponseEntity<ProyectoSummary> crear(@RequestBody CrearProyectoRequest req) {
        var creado = proyectos.crear(req);
        return ResponseEntity
                .created(URI.create("/proyectos/" + creado.id()))
                .body(creado);
    }

    @DeleteMapping("/proyectos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proyectos.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/proyectos/{id}")
    public ProyectoSummary actualizar(@PathVariable Long id, @RequestBody ActualizarProyectoRequest req) {
        return proyectos.actualizar(id, req);
    }
}

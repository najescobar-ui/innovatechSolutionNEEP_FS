package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.ActualizarRecursoRequest;
import cl.duoc.innovatech.bff.domain.CrearRecursoRequest;
import cl.duoc.innovatech.bff.domain.RecursoSummary;
import cl.duoc.innovatech.bff.domain.RecursosResponse;
import cl.duoc.innovatech.bff.service.RecursosService;
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
public class RecursosController {

    private final RecursosService recursos;

    public RecursosController(RecursosService recursos) {
        this.recursos = recursos;
    }

    @GetMapping("/recursos")
    public RecursosResponse listar() {
        return recursos.listar();
    }

    @PostMapping("/recursos")
    @ResponseStatus(HttpStatus.CREATED)
    public RecursoSummary crear(@RequestBody CrearRecursoRequest req) {
        return recursos.crear(req);
    }

    @DeleteMapping("/recursos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        recursos.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/recursos/{id}")
    public RecursoSummary actualizar(@PathVariable Long id, @RequestBody ActualizarRecursoRequest req) {
        return recursos.actualizar(id, req);
    }
}

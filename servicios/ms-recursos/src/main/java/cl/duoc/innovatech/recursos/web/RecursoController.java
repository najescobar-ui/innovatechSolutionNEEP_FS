package cl.duoc.innovatech.recursos.web;

import cl.duoc.innovatech.recursos.dto.CrearRecursoRequest;
import cl.duoc.innovatech.recursos.dto.RecursoDto;
import cl.duoc.innovatech.recursos.service.RecursoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recursos")
public class RecursoController {

    private final RecursoService service;

    public RecursoController(RecursoService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecursoDto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public RecursoDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecursoDto crear(@RequestBody CrearRecursoRequest request) {
        return service.crear(request);
    }
}

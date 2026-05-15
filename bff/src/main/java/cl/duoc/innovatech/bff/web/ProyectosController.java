package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.ProyectosResponse;
import cl.duoc.innovatech.bff.service.ProyectosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProyectosController {

    private final ProyectosService service;

    public ProyectosController(ProyectosService service) {
        this.service = service;
    }

    // Internal path /proyectos. External URL via gateway: /api/proyectos
    // (internal docs §5 rule 9: gateway StripPrefix=1 removes /api before
    // the request reaches the BFF).
    @GetMapping("/proyectos")
    public ProyectosResponse listar() {
        return service.listar();
    }
}

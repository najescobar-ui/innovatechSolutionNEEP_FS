package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.ProyectosResponse;
import cl.duoc.innovatech.bff.service.ProyectosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.RecursosResponse;
import cl.duoc.innovatech.bff.service.RecursosService;
import org.springframework.web.bind.annotation.GetMapping;
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
}

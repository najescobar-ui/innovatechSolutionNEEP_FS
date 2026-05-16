package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.service.KpisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class KpisController {

    private final KpisService kpis;

    public KpisController(KpisService kpis) {
        this.kpis = kpis;
    }

    @GetMapping("/kpis")
    public Map<String, Object> obtener() {
        return kpis.obtener();
    }

    @GetMapping("/kpis/historico")
    public Map<String, Object> historico(
            @RequestParam(defaultValue = "12") int puntos,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        return kpis.historico(puntos, desde, hasta);
    }
}

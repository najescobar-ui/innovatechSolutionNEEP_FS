package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.service.KpisService;
import org.springframework.web.bind.annotation.GetMapping;
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
}

package cl.duoc.innovatech.analitica.web;

import cl.duoc.innovatech.analitica.dto.KpiResponse;
import cl.duoc.innovatech.analitica.service.KpiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analitica")
public class AnaliticaController {

    private final KpiService kpis;

    public AnaliticaController(KpiService kpis) {
        this.kpis = kpis;
    }

    @GetMapping("/kpis")
    public KpiResponse kpis() {
        return kpis.calcular();
    }
}

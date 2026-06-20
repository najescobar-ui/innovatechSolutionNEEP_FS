package com.duoc.bff.web;

import com.duoc.bff.service.KpisService;
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
    public Map<String, Object> get() {
        return kpis.get();
    }

    @GetMapping("/kpis/history")
    public Map<String, Object> history(
            @RequestParam(defaultValue = "12") int points,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return kpis.history(points, from, to);
    }
}

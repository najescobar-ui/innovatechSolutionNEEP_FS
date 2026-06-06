package cl.duoc.innovatech.bff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KpisService {

    private static final Logger log = LoggerFactory.getLogger(KpisService.class);

    private final KpisClient client;
    private final CircuitBreakerFactory<?, ?> breakers;

    public KpisService(KpisClient client, CircuitBreakerFactory<?, ?> breakers) {
        this.client = client;
        this.breakers = breakers;
    }

    public Map<String, Object> get() {
        return breakers.create("kpis").run(
                client::get,
                ex -> {
                    log.warn("ms-analytics cayo, devolviendo fallback ({})", ex.getMessage());
                    return Map.of("status", "datos no disponibles");
                });
    }

    public Map<String, Object> history(int points, String from, String to) {
        return breakers.create("kpis-history").run(
                () -> client.history(points, from, to),
                ex -> {
                    log.warn("ms-analytics history cayo, fallback ({})", ex.getMessage());
                    return Map.of(
                            "status", "datos no disponibles",
                            "utilization", List.of(),
                            "activeProjects", List.of()
                    );
                });
    }

    // Devuelve la utilizacion real (0..1) calculada en ms-analytics.
    // Cero si el backend no respondio o si el campo no esta presente.
    public double utilizationPercentage() {
        var kpi = get();
        var raw = kpi.get("utilizationPercentage");
        if (raw instanceof Number n) return n.doubleValue();
        return 0.0;
    }
}

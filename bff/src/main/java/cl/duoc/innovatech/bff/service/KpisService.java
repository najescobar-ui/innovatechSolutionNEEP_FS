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

    public Map<String, Object> obtener() {
        return breakers.create("kpis").run(
                client::obtener,
                ex -> {
                    log.warn("ms-analitica cayo, devolviendo fallback ({})", ex.getMessage());
                    return Map.of("status", "datos no disponibles");
                });
    }

    public Map<String, Object> historico(int puntos, String desde, String hasta) {
        return breakers.create("kpis-historico").run(
                () -> client.historico(puntos, desde, hasta),
                ex -> {
                    log.warn("ms-analitica historico cayo, fallback ({})", ex.getMessage());
                    return Map.of(
                            "status", "datos no disponibles",
                            "utilizacion", List.of(),
                            "proyectosActivos", List.of()
                    );
                });
    }

    // Devuelve la utilizacion real (0..1) calculada en ms-analitica.
    // Cero si el backend no respondio o si el campo no esta presente.
    public double porcentajeUtilizacion() {
        var kpi = obtener();
        var raw = kpi.get("porcentajeUtilizacion");
        if (raw instanceof Number n) return n.doubleValue();
        return 0.0;
    }
}

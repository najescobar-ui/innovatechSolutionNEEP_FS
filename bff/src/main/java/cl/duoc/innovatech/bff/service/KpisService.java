package cl.duoc.innovatech.bff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

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
}

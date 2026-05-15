package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.RecursosResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecursosService {

    private static final Logger log = LoggerFactory.getLogger(RecursosService.class);

    private final RecursosClient client;
    private final CircuitBreakerFactory<?, ?> breakers;

    public RecursosService(RecursosClient client, CircuitBreakerFactory<?, ?> breakers) {
        this.client = client;
        this.breakers = breakers;
    }

    public RecursosResponse listar() {
        return breakers.create("recursos").run(
                () -> RecursosResponse.ok(client.listar()),
                ex -> {
                    log.warn("ms-recursos cayo, devolviendo fallback ({})", ex.getMessage());
                    return RecursosResponse.unavailable();
                });
    }
}

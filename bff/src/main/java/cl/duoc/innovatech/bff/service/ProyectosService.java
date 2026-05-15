package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.ProyectosResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProyectosService {

    private static final Logger log = LoggerFactory.getLogger(ProyectosService.class);

    private final ProyectosClient client;
    private final CircuitBreakerFactory<?, ?> breakers;

    public ProyectosService(ProyectosClient client, CircuitBreakerFactory<?, ?> breakers) {
        this.client = client;
        this.breakers = breakers;
    }

    public ProyectosResponse listar() {
        return breakers.create("proyectos").run(
                () -> ProyectosResponse.ok(client.listar()),
                ex -> {
                    log.warn("ms-proyectos cayo, devolviendo fallback ({})", ex.getMessage());
                    return ProyectosResponse.unavailable();
                });
    }
}

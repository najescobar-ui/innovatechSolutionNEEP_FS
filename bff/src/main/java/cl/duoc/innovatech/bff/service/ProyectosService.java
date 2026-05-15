package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.ProyectosResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

// Circuit Breaker (internal docs §6): wraps the outbound call to ms-proyectos.
// When the breaker is OPEN — or any individual call times out / throws —
// we return the "datos no disponibles" envelope instead of propagating the
// exception. Innovatech takes decisions about people with these dashboards,
// so showing stale or fake data is worse than showing nothing.
@Service
public class ProyectosService {

    private static final Logger log = LoggerFactory.getLogger(ProyectosService.class);
    private static final String CB_NAME = "proyectos";

    private final ProyectosClient client;
    private final CircuitBreakerFactory<?, ?> breakerFactory;

    public ProyectosService(ProyectosClient client, CircuitBreakerFactory<?, ?> breakerFactory) {
        this.client = client;
        this.breakerFactory = breakerFactory;
    }

    public ProyectosResponse listar() {
        return breakerFactory.create(CB_NAME).run(
                () -> ProyectosResponse.ok(client.listar()),
                throwable -> {
                    log.warn("ms-proyectos unavailable, returning fallback: {}", throwable.toString());
                    return ProyectosResponse.unavailable();
                }
        );
    }
}

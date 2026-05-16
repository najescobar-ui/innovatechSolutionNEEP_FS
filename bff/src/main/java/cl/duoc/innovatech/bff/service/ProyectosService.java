package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.ActualizarProyectoRequest;
import cl.duoc.innovatech.bff.domain.CrearProyectoRequest;
import cl.duoc.innovatech.bff.domain.ProyectoSummary;
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

    // Crear no usa fallback: si falla, el llamador necesita saberlo.
    public ProyectoSummary crear(CrearProyectoRequest req) {
        return client.crear(req);
    }

    public void eliminar(Long id) {
        client.eliminar(id);
    }

    public ProyectoSummary actualizar(Long id, ActualizarProyectoRequest req) {
        return client.actualizar(id, req);
    }
}

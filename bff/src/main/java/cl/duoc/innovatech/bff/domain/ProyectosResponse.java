package cl.duoc.innovatech.bff.domain;

import java.util.List;

// Envelope so the BFF can advertise "datos no disponibles" when the breaker
// is OPEN (internal docs §6 ethical fallback rule). `items` is always present
// so the frontend never has to branch on null.
public record ProyectosResponse(
        String status,
        List<ProyectoSummary> items
) {

    public static ProyectosResponse ok(List<ProyectoSummary> items) {
        return new ProyectosResponse("ok", items);
    }

    public static ProyectosResponse unavailable() {
        return new ProyectosResponse("datos no disponibles", List.of());
    }
}

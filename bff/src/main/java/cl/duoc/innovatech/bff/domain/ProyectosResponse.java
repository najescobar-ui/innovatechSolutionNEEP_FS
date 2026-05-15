package cl.duoc.innovatech.bff.domain;

import java.util.List;

public record ProyectosResponse(String status, List<ProyectoSummary> items) {

    public static ProyectosResponse ok(List<ProyectoSummary> items) {
        return new ProyectosResponse("ok", items);
    }

    public static ProyectosResponse unavailable() {
        return new ProyectosResponse("datos no disponibles", List.of());
    }
}

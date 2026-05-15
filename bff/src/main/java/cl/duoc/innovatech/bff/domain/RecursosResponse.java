package cl.duoc.innovatech.bff.domain;

import java.util.List;

public record RecursosResponse(String status, List<RecursoSummary> items) {

    public static RecursosResponse ok(List<RecursoSummary> items) {
        return new RecursosResponse("ok", items);
    }

    public static RecursosResponse unavailable() {
        return new RecursosResponse("datos no disponibles", List.of());
    }
}

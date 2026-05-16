package cl.duoc.innovatech.analitica.dto;

import java.util.Map;

public record KpiResponse(
        String status,
        int proyectosActivos,
        int proyectosAtrasados,
        int totalRecursosActivos,
        int capacidadSemanalTotalHoras,
        double promedioHorasPorRecurso,
        double porcentajeUtilizacion,
        Map<String, Long> recursosPorRol,
        Map<String, Long> proyectosPorEstado
) {

    public static KpiResponse unavailable() {
        return new KpiResponse("datos no disponibles", 0, 0, 0, 0, 0.0, 0.0, Map.of(), Map.of());
    }
}

package cl.duoc.innovatech.analytics.dto;

import java.time.Instant;
import java.util.List;

public record HistoryResponse(
        String status,
        List<Punto> utilization,
        List<Punto> activeProjects,
        Deltas deltas
) {

    public record Punto(Instant t, double v) {}

    /** Comparativa contra un periodo anterior (por defecto: ~30 dias atras). */
    public record Deltas(
            Double utilizationPercentage,
            Integer activeProjects,
            Integer activeResources
    ) {}

    public static HistoryResponse empty() {
        return new HistoryResponse("datos no disponibles", List.of(), List.of(), new Deltas(null, null, null));
    }
}

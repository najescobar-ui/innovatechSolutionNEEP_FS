package cl.duoc.innovatech.analitica.dto;

import java.time.Instant;
import java.util.List;

public record HistoricoResponse(
        String status,
        List<Punto> utilizacion,
        List<Punto> proyectosActivos,
        Deltas deltas
) {

    public record Punto(Instant t, double v) {}

    // Comparativa contra un periodo anterior (por defecto: ~30 dias atras).
    public record Deltas(
            Double porcentajeUtilizacion,
            Integer proyectosActivos,
            Integer recursosActivos
    ) {}

    public static HistoricoResponse empty() {
        return new HistoricoResponse("datos no disponibles", List.of(), List.of(), new Deltas(null, null, null));
    }
}

package cl.duoc.innovatech.analitica.web;

import cl.duoc.innovatech.analitica.dto.HistoricoResponse;
import cl.duoc.innovatech.analitica.dto.KpiResponse;
import cl.duoc.innovatech.analitica.entity.KpiSnapshot;
import cl.duoc.innovatech.analitica.service.KpiService;
import cl.duoc.innovatech.analitica.service.KpiSnapshotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/analitica")
public class AnaliticaController {

    // Ventana maxima permitida para consultas con rango (segun requerimiento UX).
    private static final long MAX_DIAS_RANGO = 30;

    private final KpiService kpis;
    private final KpiSnapshotService snapshots;

    public AnaliticaController(KpiService kpis, KpiSnapshotService snapshots) {
        this.kpis = kpis;
        this.snapshots = snapshots;
    }

    @GetMapping("/kpis")
    public KpiResponse kpis() {
        return kpis.calcular();
    }

    @GetMapping("/kpis/historico")
    public ResponseEntity<HistoricoResponse> historico(
            @RequestParam(defaultValue = "12") int puntos,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        List<KpiSnapshot> serie;

        if (desde != null && hasta != null) {
            if (hasta.isBefore(desde)) {
                return ResponseEntity.badRequest().build();
            }
            long dias = ChronoUnit.DAYS.between(desde, hasta);
            if (dias > MAX_DIAS_RANGO) {
                return ResponseEntity.badRequest().build();
            }
            // hasta inclusivo: tomamos hasta el final del dia
            var inicio = desde.atStartOfDay(ZoneOffset.UTC).toInstant();
            var fin = hasta.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            serie = snapshots.entreFechas(inicio, fin);
        } else {
            serie = snapshots.ultimas(puntos);
        }

        if (serie.isEmpty()) return ResponseEntity.ok(HistoricoResponse.empty());

        var ult = serie.get(serie.size() - 1);
        var hace30 = snapshots.ultimaAntes(Instant.now().minus(30, ChronoUnit.DAYS));

        return ResponseEntity.ok(new HistoricoResponse(
                "ok",
                puntos(serie, KpiSnapshot::getPorcentajeUtilizacion),
                puntos(serie, s -> (double) s.getProyectosActivos()),
                new HistoricoResponse.Deltas(
                        delta(ult.getPorcentajeUtilizacion(), hace30 != null ? hace30.getPorcentajeUtilizacion() : null),
                        deltaInt(ult.getProyectosActivos(), hace30 != null ? hace30.getProyectosActivos() : null),
                        deltaInt(ult.getTotalRecursosActivos(), hace30 != null ? hace30.getTotalRecursosActivos() : null)
                )
        ));
    }

    private List<HistoricoResponse.Punto> puntos(List<KpiSnapshot> serie, java.util.function.ToDoubleFunction<KpiSnapshot> f) {
        return serie.stream()
                .map(s -> new HistoricoResponse.Punto(s.getCapturedAt(), f.applyAsDouble(s)))
                .toList();
    }

    private Double delta(double actual, Double previo) {
        return previo == null ? null : Math.round((actual - previo) * 10000.0) / 10000.0;
    }

    private Integer deltaInt(int actual, Integer previo) {
        return previo == null ? null : actual - previo;
    }
}

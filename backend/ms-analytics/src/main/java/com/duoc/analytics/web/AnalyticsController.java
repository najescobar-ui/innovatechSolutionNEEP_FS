package com.duoc.analytics.web;

import com.duoc.analytics.dto.HistoryResponse;
import com.duoc.analytics.dto.KpiResponse;
import com.duoc.analytics.entity.KpiSnapshot;
import com.duoc.analytics.service.KpiService;
import com.duoc.analytics.service.KpiSnapshotService;
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
@RequestMapping("/analytics")
public class AnalyticsController {

    /** Ventana maxima permitida para consultas con rango (segun requerimiento UX). */
    private static final long MAX_RANGE_DAYS = 30;

    private final KpiService kpis;
    private final KpiSnapshotService snapshots;

    public AnalyticsController(KpiService kpis, KpiSnapshotService snapshots) {
        this.kpis = kpis;
        this.snapshots = snapshots;
    }

    @GetMapping("/kpis")
    public KpiResponse kpis() {
        return kpis.calculate();
    }

    @GetMapping("/kpis/history")
    public ResponseEntity<HistoryResponse> history(
            @RequestParam(defaultValue = "12") int points,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<KpiSnapshot> serie;

        if (from != null && to != null) {
            if (to.isBefore(from)) {
                return ResponseEntity.badRequest().build();
            }
            long dias = ChronoUnit.DAYS.between(from, to);
            if (dias > MAX_RANGE_DAYS) {
                return ResponseEntity.badRequest().build();
            }
            /* to inclusivo: tomamos hasta el final del dia */
            var inicio = from.atStartOfDay(ZoneOffset.UTC).toInstant();
            var fin = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            serie = snapshots.between(inicio, fin);
        } else {
            serie = snapshots.latest(points);
        }

        if (serie.isEmpty()) return ResponseEntity.ok(HistoryResponse.empty());

        var ult = serie.get(serie.size() - 1);
        var hace30 = snapshots.latestBefore(Instant.now().minus(30, ChronoUnit.DAYS));

        return ResponseEntity.ok(new HistoryResponse(
                "ok",
                points(serie, KpiSnapshot::getUtilizationPercentage),
                points(serie, s -> (double) s.getActiveProjects()),
                new HistoryResponse.Deltas(
                        delta(ult.getUtilizationPercentage(), hace30 != null ? hace30.getUtilizationPercentage() : null),
                        deltaInt(ult.getActiveProjects(), hace30 != null ? hace30.getActiveProjects() : null),
                        deltaInt(ult.getTotalActiveResources(), hace30 != null ? hace30.getTotalActiveResources() : null)
                )
        ));
    }

    private List<HistoryResponse.Punto> points(List<KpiSnapshot> serie, java.util.function.ToDoubleFunction<KpiSnapshot> f) {
        return serie.stream()
                .map(s -> new HistoryResponse.Punto(s.getCapturedAt(), f.applyAsDouble(s)))
                .toList();
    }

    private Double delta(double actual, Double previo) {
        return previo == null ? null : Math.round((actual - previo) * 10000.0) / 10000.0;
    }

    private Integer deltaInt(int actual, Integer previo) {
        return previo == null ? null : actual - previo;
    }
}

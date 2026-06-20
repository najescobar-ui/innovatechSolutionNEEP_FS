package com.duoc.analytics.service;

import com.duoc.analytics.dto.HistoryResponse;
import com.duoc.analytics.dto.KpiResponse;
import com.duoc.analytics.entity.KpiSnapshot;
import com.duoc.analytics.repository.KpiSnapshotRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

@Service
public class KpiSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(KpiSnapshotService.class);

    /** Ventana maxima permitida para consultas con rango (segun requerimiento UX). */
    private static final long MAX_RANGE_DAYS = 30;

    private final KpiService kpis;
    private final KpiSnapshotRepository repo;
    private final boolean enabled;
    private final int backfillWeeks;

    public KpiSnapshotService(
            KpiService kpis,
            KpiSnapshotRepository repo,
            @Value("${innovatech.snapshots.enabled:true}") boolean enabled,
            @Value("${innovatech.snapshots.backfill-weeks:12}") int backfillWeeks) {
        this.kpis = kpis;
        this.repo = repo;
        this.enabled = enabled;
        this.backfillWeeks = backfillWeeks;
    }

    /**
     * Si la tabla esta vacia, genera N puntos sinteticos con jitter sobre el valor actual
     * para que el sparkline tenga algo que mostrar desde el dia 1. A medida que el scheduler
     * corre, estos snapshots iniciales quedan sepultados por datos reales.
     */
    @PostConstruct
    public void backfillIfEmpty() {
        if (!enabled) return;
        if (repo.count() > 0) return;
        var current = kpis.calculate();
        if ("datos no disponibles".equals(current.status())) {
            log.info("backfill: KPI base no disponible aun, se omite");
            return;
        }
        log.info("backfill: generando {} snapshots semanales iniciales", backfillWeeks);
        var now = Instant.now();
        var points = new ArrayList<KpiSnapshot>();
        for (int i = backfillWeeks - 1; i >= 0; i--) {
            var t = now.minus(i * 7L, ChronoUnit.DAYS);
            /* jitter alrededor de los valores actuales (+/- 15%) */
            double jitter = 1.0 + (Math.random() - 0.5) * 0.30;
            points.add(snapshotFrom(current, t, jitter));
        }
        repo.saveAll(points);
    }

    @Scheduled(cron = "${innovatech.snapshots.cron:0 */5 * * * *}")
    public void capture() {
        if (!enabled) return;
        var current = kpis.calculate();
        if ("datos no disponibles".equals(current.status())) {
            log.debug("snapshot omitido: KPI no disponible");
            return;
        }
        var snap = snapshotFrom(current, Instant.now(), 1.0);
        repo.save(snap);
        log.debug("snapshot guardado: util={}%, activos={}", snap.getUtilizationPercentage() * 100, snap.getActiveProjects());
    }

    public List<KpiSnapshot> latest(int points) {
        /* Devuelve los ultimos `points` snapshots en orden cronologico ascendente. */
        var all = repo.findAll(
                org.springframework.data.domain.PageRequest.of(0, Math.max(1, points),
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "capturedAt")));
        var list = new ArrayList<>(all.getContent());
        java.util.Collections.reverse(list);
        return list;
    }

    public KpiSnapshot latestBefore(Instant point) {
        return repo.findLatestAtOrBefore(point).orElse(null);
    }

    public List<KpiSnapshot> between(Instant from, Instant to) {
        return repo.findByCapturedAtBetweenOrderByCapturedAtAsc(from, to);
    }

    /**
     * Serie historica de KPIs para el dashboard. Con un rango [from, to] (ambos
     * requeridos) valida coherencia y tope de {@link #MAX_RANGE_DAYS} dias; en su
     * defecto toma los ultimos `points` snapshots. Incluye los deltas contra ~30
     * dias atras. Lanza IllegalArgumentException si el rango es invalido (el
     * GlobalExceptionHandler la traduce a HTTP 400).
     */
    public HistoryResponse history(int points, LocalDate from, LocalDate to) {
        List<KpiSnapshot> serie;
        if (from != null && to != null) {
            if (to.isBefore(from)) {
                throw new IllegalArgumentException("Rango invalido: 'to' es anterior a 'from'");
            }
            if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
                throw new IllegalArgumentException("El rango supera el maximo de " + MAX_RANGE_DAYS + " dias");
            }
            /* to inclusivo: hasta el final del dia */
            var inicio = from.atStartOfDay(ZoneOffset.UTC).toInstant();
            var fin = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            serie = between(inicio, fin);
        } else {
            serie = latest(points);
        }

        if (serie.isEmpty()) return HistoryResponse.empty();

        var ult = serie.get(serie.size() - 1);
        var hace30 = latestBefore(Instant.now().minus(30, ChronoUnit.DAYS));

        return new HistoryResponse(
                "ok",
                puntos(serie, KpiSnapshot::getUtilizationPercentage),
                puntos(serie, s -> (double) s.getActiveProjects()),
                new HistoryResponse.Deltas(
                        delta(ult.getUtilizationPercentage(), hace30 != null ? hace30.getUtilizationPercentage() : null),
                        deltaInt(ult.getActiveProjects(), hace30 != null ? hace30.getActiveProjects() : null),
                        deltaInt(ult.getTotalActiveResources(), hace30 != null ? hace30.getTotalActiveResources() : null)
                )
        );
    }

    private List<HistoryResponse.Punto> puntos(List<KpiSnapshot> serie, ToDoubleFunction<KpiSnapshot> f) {
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

    private KpiSnapshot snapshotFrom(KpiResponse kpi, Instant at, double factor) {
        return new KpiSnapshot(
                at,
                (int) Math.round(kpi.activeProjects() * factor),
                kpi.delayedProjects(),
                (int) Math.round(kpi.totalActiveResources() * factor),
                (int) Math.round(kpi.totalWeeklyCapacityHours() * factor),
                kpi.avgHoursPerResource() * factor,
                Math.min(1.0, Math.max(0.0, kpi.utilizationPercentage() * factor))
        );
    }
}

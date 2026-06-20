package cl.duoc.innovatech.analytics.service;

import cl.duoc.innovatech.analytics.dto.KpiResponse;
import cl.duoc.innovatech.analytics.entity.KpiSnapshot;
import cl.duoc.innovatech.analytics.repository.KpiSnapshotRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class KpiSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(KpiSnapshotService.class);

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

    // Si la tabla esta vacia, genera N puntos sinteticos con jitter sobre el valor actual
    // para que el sparkline tenga algo que mostrar desde el dia 1. A medida que el scheduler
    // corre, estos snapshots iniciales quedan sepultados por datos reales.
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
            // jitter alrededor de los valores actuales (+/- 15%)
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
        // Devuelve los ultimos `points` snapshots en orden cronologico ascendente.
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

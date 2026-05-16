package cl.duoc.innovatech.analitica.service;

import cl.duoc.innovatech.analitica.dto.KpiResponse;
import cl.duoc.innovatech.analitica.entity.KpiSnapshot;
import cl.duoc.innovatech.analitica.repository.KpiSnapshotRepository;
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
    public void backfillSiVacio() {
        if (!enabled) return;
        if (repo.count() > 0) return;
        var actual = kpis.calcular();
        if ("datos no disponibles".equals(actual.status())) {
            log.info("backfill: KPI base no disponible aun, se omite");
            return;
        }
        log.info("backfill: generando {} snapshots semanales iniciales", backfillWeeks);
        var ahora = Instant.now();
        var puntos = new ArrayList<KpiSnapshot>();
        for (int i = backfillWeeks - 1; i >= 0; i--) {
            var t = ahora.minus(i * 7L, ChronoUnit.DAYS);
            // jitter alrededor de los valores actuales (+/- 15%)
            double jitter = 1.0 + (Math.random() - 0.5) * 0.30;
            puntos.add(snapshotDesde(actual, t, jitter));
        }
        repo.saveAll(puntos);
    }

    @Scheduled(cron = "${innovatech.snapshots.cron:0 */5 * * * *}")
    public void capturar() {
        if (!enabled) return;
        var actual = kpis.calcular();
        if ("datos no disponibles".equals(actual.status())) {
            log.debug("snapshot omitido: KPI no disponible");
            return;
        }
        var snap = snapshotDesde(actual, Instant.now(), 1.0);
        repo.save(snap);
        log.debug("snapshot guardado: util={}%, activos={}", snap.getPorcentajeUtilizacion() * 100, snap.getProyectosActivos());
    }

    public List<KpiSnapshot> ultimas(int puntos) {
        // Devuelve los ultimos `puntos` snapshots en orden cronologico ascendente.
        var all = repo.findAll(
                org.springframework.data.domain.PageRequest.of(0, Math.max(1, puntos),
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "capturedAt")));
        var lista = new ArrayList<>(all.getContent());
        java.util.Collections.reverse(lista);
        return lista;
    }

    public KpiSnapshot ultimaAntes(Instant punto) {
        return repo.findLatestAtOrBefore(punto).orElse(null);
    }

    public List<KpiSnapshot> entreFechas(Instant desde, Instant hasta) {
        return repo.findByCapturedAtBetweenOrderByCapturedAtAsc(desde, hasta);
    }

    private KpiSnapshot snapshotDesde(KpiResponse kpi, Instant at, double factor) {
        return new KpiSnapshot(
                at,
                (int) Math.round(kpi.proyectosActivos() * factor),
                kpi.proyectosAtrasados(),
                (int) Math.round(kpi.totalRecursosActivos() * factor),
                (int) Math.round(kpi.capacidadSemanalTotalHoras() * factor),
                kpi.promedioHorasPorRecurso() * factor,
                Math.min(1.0, Math.max(0.0, kpi.porcentajeUtilizacion() * factor))
        );
    }
}

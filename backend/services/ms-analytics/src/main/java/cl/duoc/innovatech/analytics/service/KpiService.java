package cl.duoc.innovatech.analytics.service;

import cl.duoc.innovatech.analytics.dto.KpiResponse;
import cl.duoc.innovatech.analytics.dto.ProjectView;
import cl.duoc.innovatech.analytics.dto.ResourceView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KpiService {

    private static final Logger log = LoggerFactory.getLogger(KpiService.class);

    // Estados que cuentan para "activos" y "atrasados".
    private static final Set<String> ACTIVE = Set.of("PLANNING", "IN_PROGRESS");
    private static final Set<String> FINISHED = Set.of("COMPLETED", "CANCELLED");

    // Tope teorico para calcular utilizacion: 40h/semana es la jornada completa.
    private static final double FULL_WEEK_HOURS = 40.0;

    private final ProjectsClient projects;
    private final ResourcesClient resources;

    public KpiService(ProjectsClient projects, ResourcesClient resources) {
        this.projects = projects;
        this.resources = resources;
    }

    public KpiResponse calculate() {
        List<ProjectView> ps;
        List<ResourceView> rs;
        try {
            ps = projects.list();
            rs = resources.list();
        } catch (IllegalStateException e) {
            log.warn("KPI no disponible: {}", e.getMessage());
            return KpiResponse.unavailable();
        }

        var hoy = LocalDate.now();
        int active = 0;
        int delayed = 0;
        for (var p : ps) {
            if (ACTIVE.contains(p.status())) active++;
            if (p.plannedEndDate() != null
                    && p.plannedEndDate().isBefore(hoy)
                    && !FINISHED.contains(p.status())) {
                delayed++;
            }
        }

        var activeResources = rs.stream().filter(r -> Boolean.TRUE.equals(r.active())).toList();
        int totalActive = activeResources.size();
        int capacity = activeResources.stream().mapToInt(ResourceView::weeklyHours).sum();
        double avg = totalActive == 0 ? 0.0 : (double) capacity / totalActive;

        // Utilizacion proxy: promedio de horas comprometidas vs jornada full (40h).
        // Cuando exista una tabla de asignaciones reales se reemplaza por la formula
        // "horas asignadas / capacidad total".
        double utilization = totalActive == 0 ? 0.0 : Math.min(1.0, avg / FULL_WEEK_HOURS);

        Map<String, Long> byRole = activeResources.stream()
                .collect(Collectors.groupingBy(ResourceView::role, Collectors.counting()));
        Map<String, Long> byStatus = ps.stream()
                .collect(Collectors.groupingBy(ProjectView::status, Collectors.counting()));

        return new KpiResponse(
                "ok",
                active,
                delayed,
                totalActive,
                capacity,
                Math.round(avg * 100.0) / 100.0,
                Math.round(utilization * 10000.0) / 10000.0,
                byRole,
                byStatus
        );
    }
}

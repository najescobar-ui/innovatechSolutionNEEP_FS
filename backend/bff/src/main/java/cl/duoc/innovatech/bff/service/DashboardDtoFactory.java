package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.DashboardDto;
import cl.duoc.innovatech.bff.domain.ProjectSummary;
import cl.duoc.innovatech.bff.domain.UserRole;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Antes era hardcoded. Ahora deriva valores de las fuentes reales:
//   - utilizacion / proyectos activos / atrasados: vienen de ms-analytics via KpisService
//   - listas de proyectos en curso / hitos: se calculan desde ProjectsService
// Si un upstream cae, el circuit breaker devuelve "datos no disponibles" y los
// numeros caen a 0 (mejor mostrar cero que un valor erroneo).
@Component
public class DashboardDtoFactory {

    private static final Set<String> ESTADOS_ACTIVOS = Set.of("PLANNING", "IN_PROGRESS");
    private static final Set<String> ESTADOS_TERMINADOS = Set.of("COMPLETED", "CANCELLED");
    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE;

    private final KpisService kpisService;
    private final ProjectsService projectsService;

    public DashboardDtoFactory(KpisService kpisService, ProjectsService projectsService) {
        this.kpisService = kpisService;
        this.projectsService = projectsService;
    }

    public DashboardDto create(UserRole role) {
        var kpi = kpisService.get();
        var projects = projectsService.list().items();

        return switch (role) {
            case PM  -> pmDashboard(kpi, projects);
            case DEV -> devDashboard(projects);
            case DIR -> dirDashboard(kpi);
        };
    }

    private DashboardDto.PMDashboard pmDashboard(Map<String, Object> kpi, List<ProjectSummary> projects) {
        int supervised = (int) projects.stream()
                .filter(p -> !ESTADOS_TERMINADOS.contains(p.status()))
                .count();
        int atRisk = intOf(kpi, "delayedProjects");

        // proximos 3 proyectos con fechaFin en el futuro, ordenados ascendente
        var hoy = LocalDate.now();
        var milestones = projects.stream()
                .filter(p -> p.plannedEndDate() != null && !p.plannedEndDate().isBefore(hoy))
                .filter(p -> !ESTADOS_TERMINADOS.contains(p.status()))
                .sorted(Comparator.comparing(ProjectSummary::plannedEndDate))
                .limit(3)
                .map(p -> p.name() + " — " + F.format(p.plannedEndDate()))
                .toList();

        return new DashboardDto.PMDashboard("PM", supervised, atRisk, milestones);
    }

    private DashboardDto.DevDashboard devDashboard(List<ProjectSummary> projects) {
        // No hay tabla de tareas/asignaciones aun, asi que esos campos quedan en 0
        // y se hidratan cuando exista la entidad. "OngoingProjects" si se puede.
        var ongoing = projects.stream()
                .filter(p -> "IN_PROGRESS".equals(p.status()))
                .map(ProjectSummary::name)
                .toList();
        return new DashboardDto.DevDashboard("DEV", 0, 0, ongoing);
    }

    private DashboardDto.DirDashboard dirDashboard(Map<String, Object> kpi) {
        int active = intOf(kpi, "activeProjects");
        double util = doubleOf(kpi, "utilizationPercentage");
        int alerts = intOf(kpi, "delayedProjects");
        return new DashboardDto.DirDashboard("DIR", active, util, alerts);
    }

    private static int intOf(Map<String, Object> kpi, String campo) {
        var raw = kpi.get(campo);
        return raw instanceof Number n ? n.intValue() : 0;
    }

    private static double doubleOf(Map<String, Object> kpi, String campo) {
        var raw = kpi.get(campo);
        return raw instanceof Number n ? n.doubleValue() : 0.0;
    }
}

package com.duoc.bff.service;

import com.duoc.bff.domain.DashboardDto;
import com.duoc.bff.domain.ProjectSummary;
import com.duoc.bff.domain.TaskSummary;
import com.duoc.bff.domain.UserRole;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Arma el dashboard segun rol con datos reales:
 *   - utilizacion / proyectos activos / atrasados y tareas atrasadas: de ms-analytics via KpisService
 *   - listas de proyectos en curso / hitos: de ProjectsService
 *   - tareas asignadas / pendientes del DEV: se resuelve el resource por email (JWT) y se cuentan sus tareas
 * Si un upstream cae, el circuit breaker degrada a "datos no disponibles" y los numeros caen a 0.
 */
@Component
public class DashboardDtoFactory {

    private static final Set<String> ESTADOS_TERMINADOS = Set.of("COMPLETED", "CANCELLED");
    private static final Set<String> TAREAS_PENDIENTES = Set.of("TODO", "IN_PROGRESS");
    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE;

    private final KpisService kpisService;
    private final ProjectsService projectsService;
    private final ResourcesService resourcesService;
    private final TasksService tasksService;

    public DashboardDtoFactory(KpisService kpisService, ProjectsService projectsService,
                               ResourcesService resourcesService, TasksService tasksService) {
        this.kpisService = kpisService;
        this.projectsService = projectsService;
        this.resourcesService = resourcesService;
        this.tasksService = tasksService;
    }

    public DashboardDto create(UserRole role, String email) {
        var kpi = kpisService.get();
        var projects = projectsService.list().items();

        return switch (role) {
            case PM  -> pmDashboard(kpi, projects);
            case DEV -> devDashboard(projects, email);
            case DIR -> dirDashboard(kpi);
        };
    }

    private DashboardDto.PMDashboard pmDashboard(Map<String, Object> kpi, List<ProjectSummary> projects) {
        int supervised = (int) projects.stream()
                .filter(p -> !ESTADOS_TERMINADOS.contains(p.status()))
                .count();
        int atRisk = intOf(kpi, "delayedTasks");

        /* proximos 3 proyectos con fechaFin en el futuro, ordenados ascendente */
        var today = LocalDate.now();
        var milestones = projects.stream()
                .filter(p -> p.plannedEndDate() != null && !p.plannedEndDate().isBefore(today))
                .filter(p -> !ESTADOS_TERMINADOS.contains(p.status()))
                .sorted(Comparator.comparing(ProjectSummary::plannedEndDate))
                .limit(3)
                .map(p -> p.name() + " — " + F.format(p.plannedEndDate()))
                .toList();

        return new DashboardDto.PMDashboard("PM", supervised, atRisk, milestones);
    }

    private DashboardDto.DevDashboard devDashboard(List<ProjectSummary> projects, String email) {
        var ongoing = projects.stream()
                .filter(p -> "IN_PROGRESS".equals(p.status()))
                .map(ProjectSummary::name)
                .toList();

        /*
         * Resolvemos el resource del usuario por su email (del JWT) y contamos sus
         * tareas. Si no hay email, no existe resource o el upstream cae, queda en 0.
         */
        int assigned = 0;
        int pending = 0;
        if (email != null && !email.isBlank()) {
            var resource = resourcesService.byEmail(email);
            if (resource.isPresent()) {
                List<TaskSummary> myTasks = tasksService.listForAssignee(resource.get().id());
                assigned = myTasks.size();
                pending = (int) myTasks.stream()
                        .filter(t -> TAREAS_PENDIENTES.contains(t.status()))
                        .count();
            }
        }

        return new DashboardDto.DevDashboard("DEV", assigned, pending, ongoing);
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

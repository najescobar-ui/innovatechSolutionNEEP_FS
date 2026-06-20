package cl.duoc.innovatech.analytics.service;

import cl.duoc.innovatech.analytics.dto.KpiResponse;
import cl.duoc.innovatech.analytics.dto.ProjectView;
import cl.duoc.innovatech.analytics.dto.ResourceView;
import cl.duoc.innovatech.analytics.dto.TaskView;
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

    /** Estados de proyecto que cuentan para "activos" y "atrasados". */
    private static final Set<String> ACTIVE = Set.of("PLANNING", "IN_PROGRESS");
    private static final Set<String> FINISHED = Set.of("COMPLETED", "CANCELLED");

    /** Estados de tarea que consumen capacidad (demanda). DONE no demanda. */
    private static final Set<String> TASK_ACTIVE = Set.of("TODO", "IN_PROGRESS", "BLOCKED");
    private static final String TASK_DONE = "DONE";

    private final ProjectsClient projects;
    private final ResourcesClient resources;
    private final TasksClient tasks;

    public KpiService(ProjectsClient projects, ResourcesClient resources, TasksClient tasks) {
        this.projects = projects;
        this.resources = resources;
        this.tasks = tasks;
    }

    public KpiResponse calculate() {
        List<ProjectView> ps;
        List<ResourceView> rs;
        List<TaskView> ts;
        try {
            ps = projects.list();
            rs = resources.list();
            ts = tasks.list();
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

        /*
         * Utilizacion REAL: demanda (horas estimadas de tareas activas asignadas a
         * recursos activos) sobre la capacidad total semanal. Acotada a 1.0 (100%).
         */
        var activeResourceIds = activeResources.stream()
                .map(ResourceView::id)
                .collect(Collectors.toSet());
        int demand = ts.stream()
                .filter(t -> t.status() != null && TASK_ACTIVE.contains(t.status()))
                .filter(t -> t.assigneeResourceId() != null && activeResourceIds.contains(t.assigneeResourceId()))
                .mapToInt(t -> t.estimatedHours() != null ? t.estimatedHours() : 0)
                .sum();
        double utilization = capacity == 0 ? 0.0 : Math.min(1.0, (double) demand / capacity);

        Map<String, Long> byRole = activeResources.stream()
                .collect(Collectors.groupingBy(ResourceView::role, Collectors.counting()));
        Map<String, Long> byStatus = ps.stream()
                .collect(Collectors.groupingBy(ProjectView::status, Collectors.counting()));

        int totalTasks = ts.size();
        Map<String, Long> tasksByStatus = ts.stream()
                .filter(t -> t.status() != null)
                .collect(Collectors.groupingBy(TaskView::status, Collectors.counting()));
        int delayedTasks = (int) ts.stream()
                .filter(t -> t.dueDate() != null && t.dueDate().isBefore(hoy))
                .filter(t -> !TASK_DONE.equals(t.status()))
                .count();

        return new KpiResponse(
                "ok",
                active,
                delayed,
                totalActive,
                capacity,
                Math.round(avg * 100.0) / 100.0,
                Math.round(utilization * 10000.0) / 10000.0,
                byRole,
                byStatus,
                totalTasks,
                delayedTasks,
                tasksByStatus
        );
    }
}

package cl.duoc.innovatech.analytics.dto;

import java.util.Map;

public record KpiResponse(
        String status,
        int activeProjects,
        int delayedProjects,
        int totalActiveResources,
        int totalWeeklyCapacityHours,
        double avgHoursPerResource,
        double utilizationPercentage,
        Map<String, Long> resourcesByRole,
        Map<String, Long> projectsByStatus,
        int totalTasks,
        int delayedTasks,
        Map<String, Long> tasksByStatus
) {

    public static KpiResponse unavailable() {
        return new KpiResponse("datos no disponibles", 0, 0, 0, 0, 0.0, 0.0,
                Map.of(), Map.of(), 0, 0, Map.of());
    }
}

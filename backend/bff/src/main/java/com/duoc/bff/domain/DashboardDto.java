package com.duoc.bff.domain;

import java.util.List;

/**
 * Sealed: cada rol tiene su forma. Agregar un rol nuevo obliga a declarar
 * el record y el switch del factory deja de compilar hasta cubrirlo.
 */
public sealed interface DashboardDto {

    String role();

    record PMDashboard(
            String role,
            int supervisedProjects,
            int tasksAtRisk,
            List<String> upcomingMilestones
    ) implements DashboardDto {}

    record DevDashboard(
            String role,
            int assignedTasks,
            int pendingTasks,
            List<String> ongoingProjects
    ) implements DashboardDto {}

    record DirDashboard(
            String role,
            int activeProjects,
            double utilizationPercentage,
            int globalAlerts
    ) implements DashboardDto {}
}

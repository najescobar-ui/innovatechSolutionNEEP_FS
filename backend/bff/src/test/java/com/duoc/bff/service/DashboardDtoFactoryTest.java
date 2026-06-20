package com.duoc.bff.service;

import com.duoc.bff.domain.DashboardDto;
import com.duoc.bff.domain.ProjectSummary;
import com.duoc.bff.domain.ProjectsResponse;
import com.duoc.bff.domain.ResourceSummary;
import com.duoc.bff.domain.TaskSummary;
import com.duoc.bff.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardDtoFactoryTest {

    @Mock
    KpisService kpis;

    @Mock
    ProjectsService projects;

    @Mock
    ResourcesService resources;

    @Mock
    TasksService tasks;

    @InjectMocks
    DashboardDtoFactory factory;

    private ProjectSummary p(long id, String name, String status, LocalDate fin) {
        return new ProjectSummary(id, name, "desc", status, LocalDate.now(), fin, "user-x");
    }

    private TaskSummary t(long id, String status) {
        return new TaskSummary(id, 1L, "Tarea " + id, "desc", status, 7L, 8, LocalDate.now().plusDays(5));
    }

    private List<ProjectSummary> dataset() {
        var today = LocalDate.now();
        return List.of(
                p(1L, "Activo 1",     "IN_PROGRESS", today.plusDays(30)),
                p(2L, "Planificado",  "PLANNING",    today.plusDays(60)),
                p(3L, "Completo",     "COMPLETED",   today.minusDays(10)),
                p(4L, "Cancelado",    "CANCELLED",   today.minusDays(50)),
                p(5L, "Activo prox.", "IN_PROGRESS", today.plusDays(7))
        );
    }

    @Test
    void createDir_usesRealKpisNotHardcoded() {
        when(kpis.get()).thenReturn(Map.of(
                "activeProjects", 12,
                "utilizationPercentage", 0.83,
                "delayedProjects", 2
        ));
        when(projects.list()).thenReturn(ProjectsResponse.ok(List.of()));

        var dto = factory.create(UserRole.DIR, null);

        assertThat(dto).isInstanceOf(DashboardDto.DirDashboard.class);
        var dir = (DashboardDto.DirDashboard) dto;
        assertThat(dir.role()).isEqualTo("DIR");
        assertThat(dir.activeProjects()).isEqualTo(12);
        assertThat(dir.utilizationPercentage()).isEqualTo(0.83);
        assertThat(dir.globalAlerts()).isEqualTo(2);
    }

    @Test
    void createDir_upstreamFailureFallsToZero() {
        when(kpis.get()).thenReturn(Map.of("status", "datos no disponibles"));
        when(projects.list()).thenReturn(ProjectsResponse.unavailable());

        var dto = (DashboardDto.DirDashboard) factory.create(UserRole.DIR, null);

        assertThat(dto.activeProjects()).isZero();
        assertThat(dto.utilizationPercentage()).isZero();
        assertThat(dto.globalAlerts()).isZero();
    }

    @Test
    void createPm_supervisedExcludesFinishedAndRealTasksAtRisk() {
        when(kpis.get()).thenReturn(Map.of("delayedTasks", 4));
        when(projects.list()).thenReturn(ProjectsResponse.ok(dataset()));

        var pm = (DashboardDto.PMDashboard) factory.create(UserRole.PM, null);

        /* 5 total - 1 completado - 1 cancelado = 3 supervisados */
        assertThat(pm.supervisedProjects()).isEqualTo(3);
        assertThat(pm.tasksAtRisk()).isEqualTo(4);   // viene de delayedTasks (KPI real)
        assertThat(pm.upcomingMilestones()).hasSizeLessThanOrEqualTo(3);
        assertThat(pm.upcomingMilestones().get(0)).contains("Activo prox.");
    }

    @Test
    void createDev_countsTasksOfResourceResolvedByEmail() {
        when(kpis.get()).thenReturn(Map.of());
        when(projects.list()).thenReturn(ProjectsResponse.ok(dataset()));
        when(resources.byEmail("dev@innovatech.cl")).thenReturn(Optional.of(
                new ResourceSummary(7L, "Dev Uno", "dev@innovatech.cl", "DEV", 40, "Java", true)));
        when(tasks.listForAssignee(7L)).thenReturn(List.of(
                t(1, "TODO"), t(2, "IN_PROGRESS"), t(3, "DONE"), t(4, "TODO")
        ));

        var dev = (DashboardDto.DevDashboard) factory.create(UserRole.DEV, "dev@innovatech.cl");

        assertThat(dev.ongoingProjects()).containsExactlyInAnyOrder("Activo 1", "Activo prox.");
        assertThat(dev.assignedTasks()).isEqualTo(4);
        assertThat(dev.pendingTasks()).isEqualTo(3);   // TODO + IN_PROGRESS, DONE no cuenta
    }

    @Test
    void createDev_withoutResourceStaysZero() {
        when(kpis.get()).thenReturn(Map.of());
        when(projects.list()).thenReturn(ProjectsResponse.ok(dataset()));
        when(resources.byEmail("nadie@x.cl")).thenReturn(Optional.empty());

        var dev = (DashboardDto.DevDashboard) factory.create(UserRole.DEV, "nadie@x.cl");

        assertThat(dev.assignedTasks()).isZero();
        assertThat(dev.pendingTasks()).isZero();
    }
}

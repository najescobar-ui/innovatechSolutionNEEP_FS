package com.duoc.analytics.service;

import com.duoc.analytics.dto.ProjectView;
import com.duoc.analytics.dto.ResourceView;
import com.duoc.analytics.dto.TaskView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpiServiceTest {

    @Mock
    ProjectsClient projects;

    @Mock
    ResourcesClient resources;

    @Mock
    TasksClient tasks;

    @InjectMocks
    KpiService service;

    @Test
    void calculate_countsActiveAndDelayed() {
        var today = LocalDate.now();
        when(projects.list()).thenReturn(List.of(
                new ProjectView(1L, "A", "IN_PROGRESS", today.plusDays(10)),  /* activo */
                new ProjectView(2L, "B", "PLANNING",    today.plusDays(30)),  /* activo */
                new ProjectView(3L, "C", "IN_PROGRESS", today.minusDays(5)),  /* activo Y atrasado */
                new ProjectView(4L, "D", "COMPLETED",   today.minusDays(20)), /* ni activo ni atrasado */
                new ProjectView(5L, "E", "CANCELLED",   today.minusDays(10))  /* ni activo ni atrasado */
        ));
        when(resources.list()).thenReturn(List.of());
        when(tasks.list()).thenReturn(List.of());

        var k = service.calculate();

        assertThat(k.status()).isEqualTo("ok");
        assertThat(k.activeProjects()).isEqualTo(3);
        assertThat(k.delayedProjects()).isEqualTo(1);
        assertThat(k.projectsByStatus())
                .containsEntry("IN_PROGRESS", 2L)
                .containsEntry("PLANNING", 1L)
                .containsEntry("COMPLETED", 1L)
                .containsEntry("CANCELLED", 1L);
    }

    @Test
    void calculate_realUtilizationIsDemandOverCapacity() {
        when(projects.list()).thenReturn(List.of());
        when(resources.list()).thenReturn(List.of(
                new ResourceView(1L, "DEV", 40, true),
                new ResourceView(2L, "DEV", 30, true),
                new ResourceView(3L, "QA",  20, true),
                new ResourceView(4L, "DEV", 40, false) /* inactivo: no aporta capacidad */
        ));
        // capacidad de activos = 40 + 30 + 20 = 90
        when(tasks.list()).thenReturn(List.of(
                new TaskView(1L, 1L, "TODO",        1L, 20, null),   /* cuenta: 20 */
                new TaskView(2L, 1L, "IN_PROGRESS", 2L, 10, null),   /* cuenta: 10 */
                new TaskView(3L, 1L, "BLOCKED",     1L,  5, null),   /* cuenta: 5  */
                new TaskView(4L, 1L, "DONE",        3L, 15, null),   /* DONE: no cuenta */
                new TaskView(5L, 1L, "TODO",        4L, 99, null)    /* recurso inactivo: no cuenta */
        ));

        var k = service.calculate();

        assertThat(k.totalActiveResources()).isEqualTo(3);
        assertThat(k.totalWeeklyCapacityHours()).isEqualTo(90);
        // demanda = 20 + 10 + 5 = 35 ; utilizacion = 35 / 90 = 0.3889
        assertThat(k.utilizationPercentage()).isCloseTo(0.3889, within(0.0001));
    }

    @Test
    void calculate_taskKpis() {
        var today = LocalDate.now();
        when(projects.list()).thenReturn(List.of());
        when(resources.list()).thenReturn(List.of(new ResourceView(1L, "DEV", 40, true)));
        when(tasks.list()).thenReturn(List.of(
                new TaskView(1L, 1L, "TODO",        1L, 8, today.minusDays(2)),  /* atrasada */
                new TaskView(2L, 1L, "IN_PROGRESS", 1L, 8, today.plusDays(5)),
                new TaskView(3L, 1L, "DONE",        1L, 8, today.minusDays(10)), /* vencida pero DONE: no atrasada */
                new TaskView(4L, 1L, "BLOCKED",     1L, 8, today.minusDays(1))   /* atrasada */
        ));

        var k = service.calculate();

        assertThat(k.totalTasks()).isEqualTo(4);
        assertThat(k.delayedTasks()).isEqualTo(2);
        assertThat(k.tasksByStatus())
                .containsEntry("TODO", 1L)
                .containsEntry("IN_PROGRESS", 1L)
                .containsEntry("DONE", 1L)
                .containsEntry("BLOCKED", 1L);
    }

    @Test
    void calculate_zeroUtilization() {
        when(projects.list()).thenReturn(List.of());
        when(resources.list()).thenReturn(List.of());
        when(tasks.list()).thenReturn(List.of());

        var k = service.calculate();

        assertThat(k.totalActiveResources()).isZero();
        assertThat(k.utilizationPercentage()).isZero();
        assertThat(k.totalTasks()).isZero();
    }

    @Test
    void calculate_unavailableWhenUpstreamFails() {
        when(projects.list()).thenThrow(new IllegalStateException("ms-projects caido"));

        var k = service.calculate();

        assertThat(k.status()).isEqualTo("datos no disponibles");
        assertThat(k.activeProjects()).isZero();
        assertThat(k.resourcesByRole()).isEmpty();
        assertThat(k.totalTasks()).isZero();
    }
}

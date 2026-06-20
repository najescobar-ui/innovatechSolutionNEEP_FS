package cl.duoc.innovatech.analytics.service;

import cl.duoc.innovatech.analytics.dto.ProjectView;
import cl.duoc.innovatech.analytics.dto.ResourceView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpiServiceTest {

    @Mock
    ProjectsClient projects;

    @Mock
    ResourcesClient resources;

    @InjectMocks
    KpiService service;

    @Test
    void calculate_cuentaActivosYAtrasados() {
        var hoy = LocalDate.now();
        when(projects.list()).thenReturn(List.of(
                new ProjectView(1L, "A", "IN_PROGRESS", hoy.plusDays(10)),  // activo
                new ProjectView(2L, "B", "PLANNING",    hoy.plusDays(30)),  // activo
                new ProjectView(3L, "C", "IN_PROGRESS", hoy.minusDays(5)),  // activo Y atrasado
                new ProjectView(4L, "D", "COMPLETED",   hoy.minusDays(20)), // ni activo ni atrasado
                new ProjectView(5L, "E", "CANCELLED",   hoy.minusDays(10))  // ni activo ni atrasado
        ));
        when(resources.list()).thenReturn(List.of());

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
    void calculate_promedioYUtilizacionConRecursosActivos() {
        when(projects.list()).thenReturn(List.of());
        when(resources.list()).thenReturn(List.of(
                new ResourceView(1L, "DEV", 40, true),
                new ResourceView(2L, "DEV", 30, true),
                new ResourceView(3L, "QA",  20, true),
                new ResourceView(4L, "DEV", 40, false) // inactivo: no cuenta
        ));

        var k = service.calculate();

        assertThat(k.totalActiveResources()).isEqualTo(3);
        assertThat(k.totalWeeklyCapacityHours()).isEqualTo(90); // 40 + 30 + 20
        assertThat(k.avgHoursPerResource()).isEqualTo(30.0);
        // utilizacion = promedio / 40 = 30 / 40 = 0.75
        assertThat(k.utilizationPercentage()).isEqualTo(0.75);
        assertThat(k.resourcesByRole())
                .containsEntry("DEV", 2L)
                .containsEntry("QA",  1L);
    }

    @Test
    void calculate_utilizacionConCero() {
        when(projects.list()).thenReturn(List.of());
        when(resources.list()).thenReturn(List.of());

        var k = service.calculate();

        assertThat(k.totalActiveResources()).isZero();
        assertThat(k.avgHoursPerResource()).isZero();
        assertThat(k.utilizationPercentage()).isZero();
    }

    @Test
    void calculate_unavailableSiUpstreamFalla() {
        when(projects.list()).thenThrow(new IllegalStateException("ms-projects caido"));

        var k = service.calculate();

        assertThat(k.status()).isEqualTo("datos no disponibles");
        assertThat(k.activeProjects()).isZero();
        assertThat(k.resourcesByRole()).isEmpty();
    }
}

package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.DashboardDto;
import cl.duoc.innovatech.bff.domain.ProjectSummary;
import cl.duoc.innovatech.bff.domain.ProjectsResponse;
import cl.duoc.innovatech.bff.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardDtoFactoryTest {

    @Mock
    KpisService kpis;

    @Mock
    ProjectsService projects;

    @InjectMocks
    DashboardDtoFactory factory;

    private ProjectSummary p(long id, String name, String status, LocalDate fin) {
        return new ProjectSummary(id, name, "desc", status, LocalDate.now(), fin, "user-x");
    }

    private List<ProjectSummary> dataset() {
        var hoy = LocalDate.now();
        return List.of(
                p(1L, "Activo 1",     "IN_PROGRESS", hoy.plusDays(30)),
                p(2L, "Planificado",  "PLANNING",    hoy.plusDays(60)),
                p(3L, "Completo",     "COMPLETED",   hoy.minusDays(10)),
                p(4L, "Cancelado",    "CANCELLED",   hoy.minusDays(50)),
                p(5L, "Activo prox.", "IN_PROGRESS", hoy.plusDays(7))
        );
    }

    @Test
    void crearDir_usaKpisRealesNoHardcoded() {
        when(kpis.get()).thenReturn(Map.of(
                "activeProjects", 12,
                "utilizationPercentage", 0.83,
                "delayedProjects", 2
        ));
        when(projects.list()).thenReturn(ProjectsResponse.ok(List.of()));

        var dto = factory.create(UserRole.DIR);

        assertThat(dto).isInstanceOf(DashboardDto.DirDashboard.class);
        var dir = (DashboardDto.DirDashboard) dto;
        assertThat(dir.role()).isEqualTo("DIR");
        assertThat(dir.activeProjects()).isEqualTo(12);
        assertThat(dir.utilizationPercentage()).isEqualTo(0.83);
        assertThat(dir.globalAlerts()).isEqualTo(2);
    }

    @Test
    void crearDir_falloUpstreamCaeAceros() {
        when(kpis.get()).thenReturn(Map.of("status", "datos no disponibles"));
        when(projects.list()).thenReturn(ProjectsResponse.unavailable());

        var dto = (DashboardDto.DirDashboard) factory.create(UserRole.DIR);

        assertThat(dto.activeProjects()).isZero();
        assertThat(dto.utilizationPercentage()).isZero();
        assertThat(dto.globalAlerts()).isZero();
    }

    @Test
    void crearPm_supervisadosExcluyeTerminados() {
        when(kpis.get()).thenReturn(Map.of("delayedProjects", 1));
        when(projects.list()).thenReturn(ProjectsResponse.ok(dataset()));

        var pm = (DashboardDto.PMDashboard) factory.create(UserRole.PM);

        /* 5 total - 1 completado - 1 cancelado = 3 supervisados */
        assertThat(pm.supervisedProjects()).isEqualTo(3);
        assertThat(pm.tasksAtRisk()).isEqualTo(1);
        /* hitos: top 3 con fechaFin futura, ordenados ascendente */
        assertThat(pm.upcomingMilestones()).hasSizeLessThanOrEqualTo(3);
        /* el primer hito debe ser el de fecha mas cercana (Activo prox., +7 dias) */
        assertThat(pm.upcomingMilestones().get(0)).contains("Activo prox.");
    }

    @Test
    void crearDev_listaSoloEnCurso() {
        when(kpis.get()).thenReturn(Map.of());
        when(projects.list()).thenReturn(ProjectsResponse.ok(dataset()));

        var dev = (DashboardDto.DevDashboard) factory.create(UserRole.DEV);

        assertThat(dev.ongoingProjects()).containsExactlyInAnyOrder("Activo 1", "Activo prox.");
        assertThat(dev.assignedTasks()).isZero();
        assertThat(dev.pendingTasks()).isZero();
    }
}

package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.DashboardDto;
import cl.duoc.innovatech.bff.domain.ProyectoSummary;
import cl.duoc.innovatech.bff.domain.ProyectosResponse;
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
    ProyectosService proyectos;

    @InjectMocks
    DashboardDtoFactory factory;

    private ProyectoSummary p(long id, String nombre, String estado, LocalDate fin) {
        return new ProyectoSummary(id, nombre, "desc", estado, LocalDate.now(), fin, "user-x");
    }

    private List<ProyectoSummary> dataset() {
        var hoy = LocalDate.now();
        return List.of(
                p(1L, "Activo 1",     "EN_CURSO",     hoy.plusDays(30)),
                p(2L, "Planificado",  "PLANIFICACION", hoy.plusDays(60)),
                p(3L, "Completo",     "COMPLETADO",   hoy.minusDays(10)),
                p(4L, "Cancelado",    "CANCELADO",    hoy.minusDays(50)),
                p(5L, "Activo prox.", "EN_CURSO",     hoy.plusDays(7))
        );
    }

    @Test
    void crearDir_usaKpisRealesNoHardcoded() {
        when(kpis.obtener()).thenReturn(Map.of(
                "proyectosActivos", 12,
                "porcentajeUtilizacion", 0.83,
                "proyectosAtrasados", 2
        ));
        when(proyectos.listar()).thenReturn(ProyectosResponse.ok(List.of()));

        var dto = factory.create(UserRole.DIR);

        assertThat(dto).isInstanceOf(DashboardDto.DirDashboard.class);
        var dir = (DashboardDto.DirDashboard) dto;
        assertThat(dir.role()).isEqualTo("DIR");
        assertThat(dir.proyectosActivos()).isEqualTo(12);
        assertThat(dir.porcentajeUtilizacion()).isEqualTo(0.83);
        assertThat(dir.alertasGlobales()).isEqualTo(2);
    }

    @Test
    void crearDir_falloUpstreamCaeAceros() {
        when(kpis.obtener()).thenReturn(Map.of("status", "datos no disponibles"));
        when(proyectos.listar()).thenReturn(ProyectosResponse.unavailable());

        var dto = (DashboardDto.DirDashboard) factory.create(UserRole.DIR);

        assertThat(dto.proyectosActivos()).isZero();
        assertThat(dto.porcentajeUtilizacion()).isZero();
        assertThat(dto.alertasGlobales()).isZero();
    }

    @Test
    void crearPm_supervisadosExcluyeTerminados() {
        when(kpis.obtener()).thenReturn(Map.of("proyectosAtrasados", 1));
        when(proyectos.listar()).thenReturn(ProyectosResponse.ok(dataset()));

        var pm = (DashboardDto.PMDashboard) factory.create(UserRole.PM);

        // 5 total - 1 completado - 1 cancelado = 3 supervisados
        assertThat(pm.proyectosSupervisados()).isEqualTo(3);
        assertThat(pm.tareasEnRiesgo()).isEqualTo(1);
        // hitos: top 3 con fechaFin futura, ordenados ascendente
        assertThat(pm.proximosHitos()).hasSizeLessThanOrEqualTo(3);
        // el primer hito debe ser el de fecha mas cercana (Activo prox., +7 dias)
        assertThat(pm.proximosHitos().get(0)).contains("Activo prox.");
    }

    @Test
    void crearDev_listaSoloEnCurso() {
        when(kpis.obtener()).thenReturn(Map.of());
        when(proyectos.listar()).thenReturn(ProyectosResponse.ok(dataset()));

        var dev = (DashboardDto.DevDashboard) factory.create(UserRole.DEV);

        assertThat(dev.proyectosEnCurso()).containsExactlyInAnyOrder("Activo 1", "Activo prox.");
        assertThat(dev.tareasAsignadas()).isZero();
        assertThat(dev.tareasPendientes()).isZero();
    }
}

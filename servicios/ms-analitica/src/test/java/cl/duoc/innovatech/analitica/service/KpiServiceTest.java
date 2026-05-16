package cl.duoc.innovatech.analitica.service;

import cl.duoc.innovatech.analitica.dto.ProyectoView;
import cl.duoc.innovatech.analitica.dto.RecursoView;
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
    ProyectosClient proyectos;

    @Mock
    RecursosClient recursos;

    @InjectMocks
    KpiService service;

    @Test
    void calcular_cuentaActivosYAtrasados() {
        var hoy = LocalDate.now();
        when(proyectos.listar()).thenReturn(List.of(
                new ProyectoView(1L, "A", "EN_CURSO",     hoy.plusDays(10)),  // activo
                new ProyectoView(2L, "B", "PLANIFICACION", hoy.plusDays(30)), // activo
                new ProyectoView(3L, "C", "EN_CURSO",     hoy.minusDays(5)),  // activo Y atrasado
                new ProyectoView(4L, "D", "COMPLETADO",   hoy.minusDays(20)), // ni activo ni atrasado
                new ProyectoView(5L, "E", "CANCELADO",    hoy.minusDays(10))  // ni activo ni atrasado
        ));
        when(recursos.listar()).thenReturn(List.of());

        var k = service.calcular();

        assertThat(k.status()).isEqualTo("ok");
        assertThat(k.proyectosActivos()).isEqualTo(3);
        assertThat(k.proyectosAtrasados()).isEqualTo(1);
        assertThat(k.proyectosPorEstado())
                .containsEntry("EN_CURSO", 2L)
                .containsEntry("PLANIFICACION", 1L)
                .containsEntry("COMPLETADO", 1L)
                .containsEntry("CANCELADO", 1L);
    }

    @Test
    void calcular_promedioYUtilizacionConRecursosActivos() {
        when(proyectos.listar()).thenReturn(List.of());
        when(recursos.listar()).thenReturn(List.of(
                new RecursoView(1L, "DEV", 40, true),
                new RecursoView(2L, "DEV", 30, true),
                new RecursoView(3L, "QA",  20, true),
                new RecursoView(4L, "DEV", 40, false) // inactivo: no cuenta
        ));

        var k = service.calcular();

        assertThat(k.totalRecursosActivos()).isEqualTo(3);
        assertThat(k.capacidadSemanalTotalHoras()).isEqualTo(90); // 40 + 30 + 20
        assertThat(k.promedioHorasPorRecurso()).isEqualTo(30.0);
        // utilizacion = promedio / 40 = 30 / 40 = 0.75
        assertThat(k.porcentajeUtilizacion()).isEqualTo(0.75);
        assertThat(k.recursosPorRol())
                .containsEntry("DEV", 2L)
                .containsEntry("QA",  1L);
    }

    @Test
    void calcular_utilizacionConCero() {
        when(proyectos.listar()).thenReturn(List.of());
        when(recursos.listar()).thenReturn(List.of());

        var k = service.calcular();

        assertThat(k.totalRecursosActivos()).isZero();
        assertThat(k.promedioHorasPorRecurso()).isZero();
        assertThat(k.porcentajeUtilizacion()).isZero();
    }

    @Test
    void calcular_unavailableSiUpstreamFalla() {
        when(proyectos.listar()).thenThrow(new IllegalStateException("ms-proyectos caido"));

        var k = service.calcular();

        assertThat(k.status()).isEqualTo("datos no disponibles");
        assertThat(k.proyectosActivos()).isZero();
        assertThat(k.recursosPorRol()).isEmpty();
    }
}

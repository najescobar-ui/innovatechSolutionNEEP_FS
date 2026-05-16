package cl.duoc.innovatech.proyectos.service;

import cl.duoc.innovatech.proyectos.dto.ActualizarProyectoRequest;
import cl.duoc.innovatech.proyectos.dto.CrearProyectoRequest;
import cl.duoc.innovatech.proyectos.entity.EstadoProyecto;
import cl.duoc.innovatech.proyectos.entity.Proyecto;
import cl.duoc.innovatech.proyectos.repository.ProyectoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    ProyectoRepository repo;

    @InjectMocks
    ProyectoService service;

    private Proyecto proyectoBase() {
        var p = new Proyecto();
        p.setId(1L);
        p.setNombre("Migracion Acme");
        p.setDescripcion("Migrar core legacy");
        p.setEstado(EstadoProyecto.PLANIFICACION);
        p.setFechaInicio(LocalDate.of(2026, 6, 1));
        p.setFechaFinPlanificada(LocalDate.of(2026, 9, 1));
        p.setResponsableId("user-1");
        return p;
    }

    @Test
    void create_mapeaCamposYGuarda() {
        var req = new CrearProyectoRequest(
                "Nuevo", "desc", EstadoProyecto.PLANIFICACION,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 1), "user-x"
        );
        when(repo.save(any(Proyecto.class))).thenAnswer(inv -> {
            var p = (Proyecto) inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        var dto = service.create(req);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.nombre()).isEqualTo("Nuevo");
        assertThat(dto.estado()).isEqualTo(EstadoProyecto.PLANIFICACION);
        assertThat(dto.responsableId()).isEqualTo("user-x");
    }

    @Test
    void delete_retornaTrueSiExiste() {
        when(repo.existsById(1L)).thenReturn(true);

        boolean borrado = service.delete(1L);

        assertThat(borrado).isTrue();
        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    void delete_retornaFalseSiNoExiste() {
        when(repo.existsById(99L)).thenReturn(false);

        boolean borrado = service.delete(99L);

        assertThat(borrado).isFalse();
        verify(repo, never()).deleteById(any());
    }

    @Test
    void patch_actualizaSoloEstadoCuandoResponsableEsNull() {
        var p = proyectoBase();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any(Proyecto.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new ActualizarProyectoRequest(EstadoProyecto.EN_CURSO, null);
        var dtoOpt = service.patch(1L, req);

        assertThat(dtoOpt).isPresent();
        assertThat(dtoOpt.get().estado()).isEqualTo(EstadoProyecto.EN_CURSO);
        assertThat(dtoOpt.get().responsableId()).isEqualTo("user-1"); // no cambio
    }

    @Test
    void patch_responsableVacioLimpiaEnNull() {
        var p = proyectoBase();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any(Proyecto.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new ActualizarProyectoRequest(null, "   ");
        var dtoOpt = service.patch(1L, req);

        assertThat(dtoOpt).isPresent();
        assertThat(dtoOpt.get().responsableId()).isNull();
        assertThat(dtoOpt.get().estado()).isEqualTo(EstadoProyecto.PLANIFICACION); // no cambio
    }

    @Test
    void patch_retornaEmptySiNoExiste() {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        var dtoOpt = service.patch(404L, new ActualizarProyectoRequest(EstadoProyecto.EN_CURSO, null));

        assertThat(dtoOpt).isEmpty();
        verify(repo, never()).save(any());
    }
}

package cl.duoc.innovatech.recursos.service;

import cl.duoc.innovatech.recursos.dto.ActualizarRecursoRequest;
import cl.duoc.innovatech.recursos.dto.CrearRecursoRequest;
import cl.duoc.innovatech.recursos.entity.Recurso;
import cl.duoc.innovatech.recursos.entity.RolRecurso;
import cl.duoc.innovatech.recursos.repository.RecursoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecursoServiceTest {

    @Mock
    RecursoRepository repo;

    @InjectMocks
    RecursoService service;

    private Recurso ana() {
        var r = new Recurso();
        r.setId(1L);
        r.setNombre("Ana Perez");
        r.setEmail("ana@innovatech.cl");
        r.setRol(RolRecurso.DEV);
        r.setHorasSemanales(40);
        r.setCompetencias("Java, Spring");
        r.setActivo(true);
        return r;
    }

    @Test
    void crear_seteaActivoTruePorDefecto() {
        when(repo.existsByEmail("nuevo@x.cl")).thenReturn(false);
        when(repo.save(any(Recurso.class))).thenAnswer(inv -> {
            var r = (Recurso) inv.getArgument(0);
            r.setId(50L);
            return r;
        });

        var dto = service.crear(new CrearRecursoRequest(
                "Nuevo", "nuevo@x.cl", RolRecurso.QA, 30, "selenium"
        ));

        assertThat(dto.id()).isEqualTo(50L);
        assertThat(dto.activo()).isTrue();
        assertThat(dto.rol()).isEqualTo(RolRecurso.QA);
    }

    @Test
    void crear_fallaSiEmailYaExiste() {
        when(repo.existsByEmail("dup@x.cl")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(new CrearRecursoRequest(
                "Dup", "dup@x.cl", RolRecurso.DEV, 40, null
        ))).isInstanceOf(IllegalArgumentException.class)
           .hasMessageContaining("Email ya registrado");

        verify(repo, never()).save(any());
    }

    @Test
    void eliminar_retornaTrueSiExiste() {
        when(repo.existsById(1L)).thenReturn(true);

        assertThat(service.eliminar(1L)).isTrue();
        verify(repo).deleteById(1L);
    }

    @Test
    void eliminar_retornaFalseSiNoExiste() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThat(service.eliminar(99L)).isFalse();
        verify(repo, never()).deleteById(any());
    }

    @Test
    void actualizar_marcaInactivo() {
        when(repo.findById(1L)).thenReturn(Optional.of(ana()));
        when(repo.save(any(Recurso.class))).thenAnswer(inv -> inv.getArgument(0));

        var opt = service.actualizar(1L, new ActualizarRecursoRequest(false));

        assertThat(opt).isPresent();
        assertThat(opt.get().activo()).isFalse();
    }

    @Test
    void actualizar_retornaEmptySiNoExiste() {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        var opt = service.actualizar(404L, new ActualizarRecursoRequest(false));

        assertThat(opt).isEmpty();
        verify(repo, never()).save(any());
    }
}

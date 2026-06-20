package cl.duoc.innovatech.resources.service;

import cl.duoc.innovatech.resources.dto.UpdateResourceRequest;
import cl.duoc.innovatech.resources.dto.CreateResourceRequest;
import cl.duoc.innovatech.resources.entity.Resource;
import cl.duoc.innovatech.resources.entity.ResourceRole;
import cl.duoc.innovatech.resources.repository.ResourceRepository;
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
class ResourceServiceTest {

    @Mock
    ResourceRepository repo;

    @InjectMocks
    ResourceService service;

    private Resource ana() {
        var r = new Resource();
        r.setId(1L);
        r.setName("Ana Perez");
        r.setEmail("ana@innovatech.cl");
        r.setRole(ResourceRole.DEV);
        r.setWeeklyHours(40);
        r.setSkills("Java, Spring");
        r.setActive(true);
        return r;
    }

    @Test
    void crear_seteaActivoTruePorDefecto() {
        when(repo.existsByEmail("nuevo@x.cl")).thenReturn(false);
        when(repo.save(any(Resource.class))).thenAnswer(inv -> {
            var r = (Resource) inv.getArgument(0);
            r.setId(50L);
            return r;
        });

        var dto = service.create(new CreateResourceRequest(
                "Nuevo", "nuevo@x.cl", ResourceRole.QA, 30, "selenium"
        ));

        assertThat(dto.id()).isEqualTo(50L);
        assertThat(dto.active()).isTrue();
        assertThat(dto.role()).isEqualTo(ResourceRole.QA);
    }

    @Test
    void crear_fallaSiEmailYaExiste() {
        when(repo.existsByEmail("dup@x.cl")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateResourceRequest(
                "Dup", "dup@x.cl", ResourceRole.DEV, 40, null
        ))).isInstanceOf(IllegalArgumentException.class)
           .hasMessageContaining("Email ya registrado");

        verify(repo, never()).save(any());
    }

    @Test
    void eliminar_retornaTrueSiExiste() {
        when(repo.existsById(1L)).thenReturn(true);

        assertThat(service.delete(1L)).isTrue();
        verify(repo).deleteById(1L);
    }

    @Test
    void eliminar_retornaFalseSiNoExiste() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThat(service.delete(99L)).isFalse();
        verify(repo, never()).deleteById(any());
    }

    @Test
    void actualizar_marcaInactivo() {
        when(repo.findById(1L)).thenReturn(Optional.of(ana()));
        when(repo.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        var opt = service.update(1L, new UpdateResourceRequest(false));

        assertThat(opt).isPresent();
        assertThat(opt.get().active()).isFalse();
    }

    @Test
    void actualizar_retornaEmptySiNoExiste() {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        var opt = service.update(404L, new UpdateResourceRequest(false));

        assertThat(opt).isEmpty();
        verify(repo, never()).save(any());
    }

    @Test
    void buscarPorEmail_encontrado() {
        when(repo.findByEmail("ana@innovatech.cl")).thenReturn(Optional.of(ana()));

        var opt = service.findByEmail("ana@innovatech.cl");

        assertThat(opt).isPresent();
        assertThat(opt.get().id()).isEqualTo(1L);
        assertThat(opt.get().role()).isEqualTo(ResourceRole.DEV);
    }

    @Test
    void buscarPorEmail_noEncontrado() {
        when(repo.findByEmail("nadie@x.cl")).thenReturn(Optional.empty());

        assertThat(service.findByEmail("nadie@x.cl")).isEmpty();
    }
}

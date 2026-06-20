package cl.duoc.innovatech.projects.service;

import cl.duoc.innovatech.projects.dto.UpdateProjectRequest;
import cl.duoc.innovatech.projects.dto.CreateProjectRequest;
import cl.duoc.innovatech.projects.entity.ProjectStatus;
import cl.duoc.innovatech.projects.entity.Project;
import cl.duoc.innovatech.projects.repository.ProjectRepository;
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
class ProjectServiceTest {

    @Mock
    ProjectRepository repo;

    @InjectMocks
    ProjectService service;

    private Project baseProject() {
        var p = new Project();
        p.setId(1L);
        p.setName("Migracion Acme");
        p.setDescription("Migrar core legacy");
        p.setStatus(ProjectStatus.PLANNING);
        p.setStartDate(LocalDate.of(2026, 6, 1));
        p.setPlannedEndDate(LocalDate.of(2026, 9, 1));
        p.setOwnerId("user-1");
        return p;
    }

    @Test
    void create_mapsFieldsAndSaves() {
        var req = new CreateProjectRequest(
                "Nuevo", "desc", ProjectStatus.PLANNING,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 1), "user-x"
        );
        when(repo.save(any(Project.class))).thenAnswer(inv -> {
            var p = (Project) inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        var dto = service.create(req);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.name()).isEqualTo("Nuevo");
        assertThat(dto.status()).isEqualTo(ProjectStatus.PLANNING);
        assertThat(dto.ownerId()).isEqualTo("user-x");
    }

    @Test
    void delete_returnsTrueWhenExists() {
        when(repo.existsById(1L)).thenReturn(true);

        boolean deleted = service.delete(1L);

        assertThat(deleted).isTrue();
        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    void delete_returnsFalseWhenMissing() {
        when(repo.existsById(99L)).thenReturn(false);

        boolean deleted = service.delete(99L);

        assertThat(deleted).isFalse();
        verify(repo, never()).deleteById(any());
    }

    @Test
    void patch_updatesOnlyStatusWhenOwnerIsNull() {
        var p = baseProject();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new UpdateProjectRequest(ProjectStatus.IN_PROGRESS, null);
        var dtoOpt = service.patch(1L, req);

        assertThat(dtoOpt).isPresent();
        assertThat(dtoOpt.get().status()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(dtoOpt.get().ownerId()).isEqualTo("user-1"); /* no cambio */
    }

    @Test
    void patch_blankOwnerClearsToNull() {
        var p = baseProject();
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new UpdateProjectRequest(null, "   ");
        var dtoOpt = service.patch(1L, req);

        assertThat(dtoOpt).isPresent();
        assertThat(dtoOpt.get().ownerId()).isNull();
        assertThat(dtoOpt.get().status()).isEqualTo(ProjectStatus.PLANNING); /* no cambio */
    }

    @Test
    void patch_returnsEmptyWhenMissing() {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        var dtoOpt = service.patch(404L, new UpdateProjectRequest(ProjectStatus.IN_PROGRESS, null));

        assertThat(dtoOpt).isEmpty();
        verify(repo, never()).save(any());
    }
}

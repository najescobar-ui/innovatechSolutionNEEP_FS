package com.duoc.projects.service;

import com.duoc.projects.dto.CreateTaskRequest;
import com.duoc.projects.dto.UpdateTaskRequest;
import com.duoc.projects.entity.Task;
import com.duoc.projects.entity.TaskStatus;
import com.duoc.projects.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository repo;

    @InjectMocks
    TaskService service;

    private Task task(long id, long projectId, TaskStatus status, Long assignee) {
        return new Task(id, projectId, "Tarea " + id, "desc", status, assignee, 10, LocalDate.of(2026, 7, 1));
    }

    @Test
    void create_mapsFieldsAndDefaultsStatusAndHours() {
        var req = new CreateTaskRequest(1L, "Nueva", "desc", null, 3L, null, LocalDate.of(2026, 8, 1));
        when(repo.save(any(Task.class))).thenAnswer(inv -> {
            var t = (Task) inv.getArgument(0);
            t.setId(99L);
            return t;
        });

        var dto = service.create(req);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.projectId()).isEqualTo(1L);
        assertThat(dto.assigneeResourceId()).isEqualTo(3L);
        assertThat(dto.status()).isEqualTo(TaskStatus.TODO);   // default
        assertThat(dto.estimatedHours()).isZero();             // default
    }

    @Test
    void list_filtersByAssigneeAndStatus() {
        when(repo.findAll()).thenReturn(List.of(
                task(1, 1, TaskStatus.TODO, 5L),
                task(2, 1, TaskStatus.IN_PROGRESS, 5L),
                task(3, 2, TaskStatus.TODO, 6L),
                task(4, 1, TaskStatus.TODO, 5L)
        ));

        var soloAssignee5 = service.list(null, 5L, null);
        assertThat(soloAssignee5).hasSize(3);

        var assignee5yTodo = service.list(null, 5L, TaskStatus.TODO);
        assertThat(assignee5yTodo).hasSize(2);

        var project2 = service.list(2L, null, null);
        assertThat(project2).hasSize(1);
    }

    @Test
    void patch_updatesOnlyProvidedFields() {
        var t = task(1, 1, TaskStatus.TODO, 5L);
        when(repo.findById(1L)).thenReturn(Optional.of(t));
        when(repo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        var dtoOpt = service.patch(1L, new UpdateTaskRequest(null, null, TaskStatus.DONE, null, null, null));

        assertThat(dtoOpt).isPresent();
        assertThat(dtoOpt.get().status()).isEqualTo(TaskStatus.DONE);
        assertThat(dtoOpt.get().assigneeResourceId()).isEqualTo(5L);  // sin cambio
        assertThat(dtoOpt.get().estimatedHours()).isEqualTo(10);      // sin cambio
    }

    @Test
    void patch_returnsEmptyWhenMissing() {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        var dtoOpt = service.patch(404L, new UpdateTaskRequest(null, null, TaskStatus.DONE, null, null, null));

        assertThat(dtoOpt).isEmpty();
        verify(repo, never()).save(any());
    }

    @Test
    void delete_returnsTrueWhenExists() {
        when(repo.existsById(1L)).thenReturn(true);
        assertThat(service.delete(1L)).isTrue();
        verify(repo).deleteById(1L);
    }

    @Test
    void delete_returnsFalseWhenMissing() {
        when(repo.existsById(99L)).thenReturn(false);
        assertThat(service.delete(99L)).isFalse();
        verify(repo, never()).deleteById(any());
    }
}

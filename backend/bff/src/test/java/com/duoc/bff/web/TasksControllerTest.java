package com.duoc.bff.web;

import com.duoc.bff.domain.CreateTaskRequest;
import com.duoc.bff.domain.TaskSummary;
import com.duoc.bff.domain.TasksResponse;
import com.duoc.bff.domain.UpdateTaskRequest;
import com.duoc.bff.service.TasksService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TasksControllerTest {

    @Mock TasksService service;
    @InjectMocks TasksController controller;

    private TaskSummary sample() {
        return new TaskSummary(1L, 1L, "T", "d", "TODO", 7L, 8, null);
    }

    @Test
    void list_delegates() {
        when(service.list(null, null, "TODO")).thenReturn(TasksResponse.ok(List.of(sample())));
        assertThat(controller.list(null, null, "TODO").items()).hasSize(1);
    }

    @Test
    void create_returns201() {
        var req = new CreateTaskRequest(1L, "T", "d", "TODO", 7L, 8, null);
        when(service.create(req)).thenReturn(sample());
        var resp = controller.create(req);
        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        assertThat(resp.getBody().id()).isEqualTo(1L);
    }

    @Test
    void delete_returns204() {
        var resp = controller.delete(2L);
        assertThat(resp.getStatusCode().value()).isEqualTo(204);
        verify(service).delete(2L);
    }

    @Test
    void update_delegates() {
        var req = new UpdateTaskRequest("T2", null, "DONE", null, null, null);
        when(service.update(2L, req)).thenReturn(sample());
        assertThat(controller.update(2L, req).id()).isEqualTo(1L);
    }
}

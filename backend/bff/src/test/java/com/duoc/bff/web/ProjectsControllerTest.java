package com.duoc.bff.web;

import com.duoc.bff.domain.CreateProjectRequest;
import com.duoc.bff.domain.ProjectSummary;
import com.duoc.bff.domain.ProjectsResponse;
import com.duoc.bff.domain.UpdateProjectRequest;
import com.duoc.bff.service.ProjectsService;
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
class ProjectsControllerTest {

    @Mock ProjectsService service;
    @InjectMocks ProjectsController controller;

    @Test
    void list_delegates() {
        when(service.list()).thenReturn(ProjectsResponse.ok(List.of()));
        assertThat(controller.list().status()).isEqualTo("ok");
    }

    @Test
    void create_returns201() {
        var req = new CreateProjectRequest("P", "d", "PLANNING", null, null, null);
        when(service.create(req)).thenReturn(new ProjectSummary(1L, "P", "d", "PLANNING", null, null, null));
        var resp = controller.create(req);
        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        assertThat(resp.getBody().id()).isEqualTo(1L);
    }

    @Test
    void delete_returns204() {
        var resp = controller.delete(3L);
        assertThat(resp.getStatusCode().value()).isEqualTo(204);
        verify(service).delete(3L);
    }

    @Test
    void update_delegates() {
        var req = new UpdateProjectRequest("DONE", null);
        when(service.update(3L, req)).thenReturn(new ProjectSummary(3L, "P", "d", "DONE", null, null, null));
        assertThat(controller.update(3L, req).status()).isEqualTo("DONE");
    }
}

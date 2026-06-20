package com.duoc.bff.web;

import com.duoc.bff.domain.CreateResourceRequest;
import com.duoc.bff.domain.ResourceSummary;
import com.duoc.bff.domain.ResourcesResponse;
import com.duoc.bff.domain.UpdateResourceRequest;
import com.duoc.bff.service.ResourcesService;
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
class ResourcesControllerTest {

    @Mock ResourcesService service;
    @InjectMocks ResourcesController controller;

    private ResourceSummary sample() {
        return new ResourceSummary(1L, "Ana", "ana@x.cl", "DEV", 40, "java", true);
    }

    @Test
    void list_delegates() {
        when(service.list()).thenReturn(ResourcesResponse.ok(List.of(sample())));
        assertThat(controller.list().items()).hasSize(1);
    }

    @Test
    void create_delegates() {
        var req = new CreateResourceRequest("Ana", "ana@x.cl", "DEV", 40, "java");
        when(service.create(req)).thenReturn(sample());
        assertThat(controller.create(req).id()).isEqualTo(1L);
    }

    @Test
    void delete_returns204() {
        var resp = controller.delete(2L);
        assertThat(resp.getStatusCode().value()).isEqualTo(204);
        verify(service).delete(2L);
    }

    @Test
    void update_delegates() {
        var req = new UpdateResourceRequest(false);
        when(service.update(2L, req)).thenReturn(sample());
        assertThat(controller.update(2L, req).id()).isEqualTo(1L);
    }
}

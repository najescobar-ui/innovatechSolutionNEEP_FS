package com.duoc.bff.service;

import com.duoc.bff.domain.CreateProjectRequest;
import com.duoc.bff.domain.ProjectSummary;
import com.duoc.bff.domain.UpdateProjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsServiceTest {

    @Mock ProjectsClient client;
    @Mock CircuitBreakerFactory<?, ?> breakers;
    @Mock CircuitBreaker cb;

    ProjectsService service;

    @BeforeEach
    void setup() {
        service = new ProjectsService(client, breakers);
    }

    @SuppressWarnings("unchecked")
    private void happyPath() {
        when(breakers.create(anyString())).thenReturn(cb);
        when(cb.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());
    }

    @SuppressWarnings("unchecked")
    private void fallbackPath() {
        when(breakers.create(anyString())).thenReturn(cb);
        when(cb.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(i -> ((Function<Throwable, ?>) i.getArgument(1)).apply(new RuntimeException("down")));
    }

    @Test
    void list_ok() {
        happyPath();
        when(client.list()).thenReturn(List.of(
                new ProjectSummary(1L, "P", "d", "IN_PROGRESS", null, null, null)));
        var r = service.list();
        assertThat(r.status()).isEqualTo("ok");
        assertThat(r.items()).hasSize(1);
    }

    @Test
    void list_fallbackWhenDown() {
        fallbackPath();
        var r = service.list();
        assertThat(r.status()).isEqualTo("datos no disponibles");
        assertThat(r.items()).isEmpty();
    }

    @Test
    void create_delete_update_delegateToClient() {
        var req = new CreateProjectRequest("P", "d", "PLANNING", null, null, null);
        when(client.create(req)).thenReturn(new ProjectSummary(9L, "P", "d", "PLANNING", null, null, null));
        assertThat(service.create(req).id()).isEqualTo(9L);

        service.delete(5L);
        verify(client).delete(5L);

        var upd = new UpdateProjectRequest("DONE", null);
        service.update(5L, upd);
        verify(client).update(5L, upd);
    }
}

package com.duoc.bff.service;

import com.duoc.bff.domain.CreateTaskRequest;
import com.duoc.bff.domain.TaskSummary;
import com.duoc.bff.domain.UpdateTaskRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TasksServiceTest {

    @Mock TasksClient client;
    @Mock CircuitBreakerFactory<?, ?> breakers;
    @Mock CircuitBreaker cb;

    TasksService service;

    @BeforeEach
    void setup() {
        service = new TasksService(client, breakers);
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

    private TaskSummary sample() {
        return new TaskSummary(1L, 1L, "T", "d", "TODO", 7L, 8, null);
    }

    @Test
    void list_ok() {
        happyPath();
        when(client.list(null, null, "TODO")).thenReturn(List.of(sample()));
        assertThat(service.list(null, null, "TODO").items()).hasSize(1);
    }

    @Test
    void list_fallback() {
        fallbackPath();
        assertThat(service.list(null, null, null).status()).isEqualTo("datos no disponibles");
    }

    @Test
    void listForAssignee_ok() {
        happyPath();
        when(client.list(isNull(), eq(7L), isNull())).thenReturn(List.of(sample()));
        assertThat(service.listForAssignee(7L)).hasSize(1);
    }

    @Test
    void listForAssignee_fallbackEmpty() {
        fallbackPath();
        assertThat(service.listForAssignee(7L)).isEmpty();
    }

    @Test
    void create_delete_update_delegate() {
        var req = new CreateTaskRequest(1L, "T", "d", "TODO", 7L, 8, null);
        when(client.create(req)).thenReturn(sample());
        assertThat(service.create(req).id()).isEqualTo(1L);
        service.delete(2L);
        verify(client).delete(2L);
        var upd = new UpdateTaskRequest("T2", null, "DONE", null, null, null);
        service.update(2L, upd);
        verify(client).update(2L, upd);
    }
}

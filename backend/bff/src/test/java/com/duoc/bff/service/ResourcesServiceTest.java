package com.duoc.bff.service;

import com.duoc.bff.domain.CreateResourceRequest;
import com.duoc.bff.domain.ResourceSummary;
import com.duoc.bff.domain.UpdateResourceRequest;
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
class ResourcesServiceTest {

    @Mock ResourcesClient client;
    @Mock CircuitBreakerFactory<?, ?> breakers;
    @Mock CircuitBreaker cb;

    ResourcesService service;

    @BeforeEach
    void setup() {
        service = new ResourcesService(client, breakers);
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

    private ResourceSummary sample() {
        return new ResourceSummary(1L, "Ana", "ana@x.cl", "DEV", 40, "java", true);
    }

    @Test
    void list_ok() {
        happyPath();
        when(client.list()).thenReturn(List.of(sample()));
        assertThat(service.list().items()).hasSize(1);
    }

    @Test
    void list_fallback() {
        fallbackPath();
        assertThat(service.list().status()).isEqualTo("datos no disponibles");
    }

    @Test
    void byEmail_found() {
        happyPath();
        when(client.byEmail("ana@x.cl")).thenReturn(sample());
        assertThat(service.byEmail("ana@x.cl")).isPresent();
    }

    @Test
    void byEmail_emptyWhenNull() {
        happyPath();
        when(client.byEmail("nadie@x.cl")).thenReturn(null);
        assertThat(service.byEmail("nadie@x.cl")).isEmpty();
    }

    @Test
    void byEmail_fallbackEmpty() {
        fallbackPath();
        assertThat(service.byEmail("x@x.cl")).isEmpty();
    }

    @Test
    void create_delete_update_delegate() {
        var req = new CreateResourceRequest("Ana", "ana@x.cl", "DEV", 40, "java");
        when(client.create(req)).thenReturn(sample());
        assertThat(service.create(req).id()).isEqualTo(1L);
        service.delete(3L);
        verify(client).delete(3L);
        var upd = new UpdateResourceRequest(false);
        service.update(3L, upd);
        verify(client).update(3L, upd);
    }
}

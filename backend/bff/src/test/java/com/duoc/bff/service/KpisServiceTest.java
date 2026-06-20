package com.duoc.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpisServiceTest {

    @Mock KpisClient client;
    @Mock CircuitBreakerFactory<?, ?> breakers;
    @Mock CircuitBreaker cb;

    KpisService service;

    @BeforeEach
    void setup() {
        service = new KpisService(client, breakers);
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
    void get_ok() {
        happyPath();
        when(client.get()).thenReturn(Map.of("status", "ok", "utilizationPercentage", 0.5));
        assertThat(service.get()).containsEntry("status", "ok");
    }

    @Test
    void get_fallback() {
        fallbackPath();
        assertThat(service.get()).containsEntry("status", "datos no disponibles");
    }

    @Test
    void history_ok() {
        happyPath();
        when(client.history(12, null, null)).thenReturn(Map.of("status", "ok"));
        assertThat(service.history(12, null, null)).containsEntry("status", "ok");
    }

    @Test
    void history_fallback() {
        fallbackPath();
        assertThat(service.history(12, null, null)).containsEntry("status", "datos no disponibles");
    }

    @Test
    void utilizationPercentage_readsValue() {
        happyPath();
        when(client.get()).thenReturn(Map.of("utilizationPercentage", 0.73));
        assertThat(service.utilizationPercentage()).isEqualTo(0.73);
    }

    @Test
    void utilizationPercentage_zeroWhenAbsent() {
        happyPath();
        when(client.get()).thenReturn(Map.of("status", "ok"));
        assertThat(service.utilizationPercentage()).isZero();
    }
}

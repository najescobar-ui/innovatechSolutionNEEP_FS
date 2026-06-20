package com.duoc.analytics.web;

import com.duoc.analytics.dto.KpiResponse;
import com.duoc.analytics.entity.KpiSnapshot;
import com.duoc.analytics.service.KpiService;
import com.duoc.analytics.service.KpiSnapshotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock KpiService kpis;
    @Mock KpiSnapshotService snapshots;
    @InjectMocks AnalyticsController controller;

    private KpiResponse okKpi() {
        return new KpiResponse("ok", 3, 1, 5, 200, 40.0, 0.5,
                Map.of("DEV", 3L), Map.of("IN_PROGRESS", 2L), 10, 2, Map.of("TODO", 5L));
    }

    private KpiSnapshot snap(int daysAgo, double util, int active) {
        return new KpiSnapshot(Instant.now().minusSeconds(daysAgo * 86400L), active, 1, 5, 200, 40.0, util);
    }

    @Test
    void kpis_returnsCalculated() {
        when(kpis.calculate()).thenReturn(okKpi());
        assertThat(controller.kpis().status()).isEqualTo("ok");
    }

    @Test
    void history_defaultUsesLatestPoints() {
        when(snapshots.latest(12)).thenReturn(List.of(snap(10, 0.4, 3), snap(0, 0.5, 4)));
        when(snapshots.latestBefore(any())).thenReturn(snap(30, 0.3, 2));

        var resp = controller.history(12, null, null);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody().status()).isEqualTo("ok");
        assertThat(resp.getBody().utilization()).hasSize(2);
    }

    @Test
    void history_emptySerieReturnsEmpty() {
        when(snapshots.latest(12)).thenReturn(List.of());
        var resp = controller.history(12, null, null);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody().status()).isEqualTo("datos no disponibles");
    }

    @Test
    void history_toBeforeFrom_badRequest() {
        var resp = controller.history(12, LocalDate.now(), LocalDate.now().minusDays(3));
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void history_rangeOver30Days_badRequest() {
        var resp = controller.history(12, LocalDate.now().minusDays(40), LocalDate.now());
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void history_withValidRange_usesBetween() {
        when(snapshots.between(any(), any())).thenReturn(List.of(snap(5, 0.4, 3)));
        when(snapshots.latestBefore(any())).thenReturn(null);

        var resp = controller.history(12, LocalDate.now().minusDays(10), LocalDate.now());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody().status()).isEqualTo("ok");
    }
}

package com.duoc.analytics.web;

import com.duoc.analytics.dto.HistoryResponse;
import com.duoc.analytics.dto.KpiResponse;
import com.duoc.analytics.service.KpiService;
import com.duoc.analytics.service.KpiSnapshotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El controller solo mapea HTTP; aqui se verifica que delega en los services. */
@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock KpiService kpis;
    @Mock KpiSnapshotService snapshots;
    @InjectMocks AnalyticsController controller;

    @Test
    void kpis_returnsCalculated() {
        var kpi = new KpiResponse("ok", 3, 1, 5, 200, 40.0, 0.5,
                Map.of("DEV", 3L), Map.of("IN_PROGRESS", 2L), 10, 2, Map.of("TODO", 5L));
        when(kpis.calculate()).thenReturn(kpi);

        assertThat(controller.kpis().status()).isEqualTo("ok");
    }

    @Test
    void history_delegatesToService() {
        var expected = new HistoryResponse("ok", List.of(), List.of(),
                new HistoryResponse.Deltas(null, null, null));
        when(snapshots.history(12, null, null)).thenReturn(expected);

        var resp = controller.history(12, null, null);

        assertThat(resp).isSameAs(expected);
        verify(snapshots).history(12, null, null);
    }
}

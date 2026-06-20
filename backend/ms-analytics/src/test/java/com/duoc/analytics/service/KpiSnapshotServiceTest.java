package com.duoc.analytics.service;

import com.duoc.analytics.dto.KpiResponse;
import com.duoc.analytics.entity.KpiSnapshot;
import com.duoc.analytics.repository.KpiSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpiSnapshotServiceTest {

    @Mock KpiService kpis;
    @Mock KpiSnapshotRepository repo;

    private KpiSnapshotService svc(boolean enabled, int weeks) {
        return new KpiSnapshotService(kpis, repo, enabled, weeks);
    }

    private KpiResponse okKpi() {
        return new KpiResponse("ok", 3, 1, 5, 200, 40.0, 0.5,
                Map.of("DEV", 3L), Map.of("IN_PROGRESS", 2L), 10, 2, Map.of("TODO", 5L));
    }

    @Test
    void backfill_whenEmptyGeneratesSnapshots() {
        when(repo.count()).thenReturn(0L);
        when(kpis.calculate()).thenReturn(okKpi());

        svc(true, 4).backfillIfEmpty();

        verify(repo).saveAll(any());
    }

    @Test
    void backfill_whenNotEmptySkips() {
        when(repo.count()).thenReturn(8L);
        svc(true, 4).backfillIfEmpty();
        verify(repo, never()).saveAll(any());
    }

    @Test
    void backfill_whenDisabledSkips() {
        svc(false, 4).backfillIfEmpty();
        verify(repo, never()).count();
        verify(repo, never()).saveAll(any());
    }

    @Test
    void backfill_whenKpiUnavailableSkips() {
        when(repo.count()).thenReturn(0L);
        when(kpis.calculate()).thenReturn(KpiResponse.unavailable());
        svc(true, 4).backfillIfEmpty();
        verify(repo, never()).saveAll(any());
    }

    @Test
    void capture_savesWhenEnabledAndAvailable() {
        when(kpis.calculate()).thenReturn(okKpi());
        svc(true, 4).capture();
        verify(repo).save(any(KpiSnapshot.class));
    }

    @Test
    void capture_disabledSkips() {
        svc(false, 4).capture();
        verify(repo, never()).save(any());
    }

    @Test
    void latest_returnsChronologicalAscending() {
        var older = new KpiSnapshot(Instant.now().minusSeconds(7200), 2, 0, 5, 200, 40.0, 0.4);
        var newer = new KpiSnapshot(Instant.now(), 3, 0, 5, 200, 40.0, 0.5);
        // repo devuelve DESC; el service invierte a ASC
        when(repo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(newer, older)));

        var result = svc(true, 4).latest(2);

        assertThat(result).containsExactly(older, newer);
    }

    @Test
    void betweenAndLatestBefore_delegate() {
        var s = new KpiSnapshot(Instant.now(), 3, 0, 5, 200, 40.0, 0.5);
        when(repo.findByCapturedAtBetweenOrderByCapturedAtAsc(any(), any())).thenReturn(List.of(s));
        when(repo.findLatestAtOrBefore(any())).thenReturn(Optional.of(s));

        var service = svc(true, 4);
        assertThat(service.between(Instant.now().minusSeconds(100), Instant.now())).hasSize(1);
        assertThat(service.latestBefore(Instant.now())).isEqualTo(s);
    }
}

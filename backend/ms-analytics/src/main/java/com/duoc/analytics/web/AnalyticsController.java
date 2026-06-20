package com.duoc.analytics.web;

import com.duoc.analytics.dto.HistoryResponse;
import com.duoc.analytics.dto.KpiResponse;
import com.duoc.analytics.service.KpiService;
import com.duoc.analytics.service.KpiSnapshotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Endpoints de analitica. La logica vive en los services; aqui solo se mapea HTTP. */
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final KpiService kpis;
    private final KpiSnapshotService snapshots;

    public AnalyticsController(KpiService kpis, KpiSnapshotService snapshots) {
        this.kpis = kpis;
        this.snapshots = snapshots;
    }

    @GetMapping("/kpis")
    public KpiResponse kpis() {
        return kpis.calculate();
    }

    @GetMapping("/kpis/history")
    public HistoryResponse history(
            @RequestParam(defaultValue = "12") int points,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return snapshots.history(points, from, to);
    }
}

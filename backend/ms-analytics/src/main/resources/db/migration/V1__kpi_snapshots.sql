CREATE TABLE kpi_snapshots (
    id BIGSERIAL PRIMARY KEY,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    active_projects INTEGER NOT NULL,
    delayed_projects INTEGER NOT NULL,
    total_active_resources INTEGER NOT NULL,
    weekly_capacity_hours INTEGER NOT NULL,
    avg_hours_per_resource DOUBLE PRECISION NOT NULL,
    utilization_percentage DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_kpi_snapshots_captured_at ON kpi_snapshots (captured_at);

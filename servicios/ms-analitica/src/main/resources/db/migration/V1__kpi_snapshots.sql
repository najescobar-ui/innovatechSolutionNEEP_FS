CREATE TABLE kpi_snapshots (
    id BIGSERIAL PRIMARY KEY,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    proyectos_activos INTEGER NOT NULL,
    proyectos_atrasados INTEGER NOT NULL,
    total_recursos_activos INTEGER NOT NULL,
    capacidad_semanal_horas INTEGER NOT NULL,
    promedio_horas_por_recurso DOUBLE PRECISION NOT NULL,
    porcentaje_utilizacion DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_kpi_snapshots_captured_at ON kpi_snapshots (captured_at);

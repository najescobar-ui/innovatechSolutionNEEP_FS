package cl.duoc.innovatech.analitica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// Snapshot puntual de los KPIs agregados. Sirve para series temporales (sparkline,
// deltas vs periodo anterior) sin tener que recalcular history desde cero.
@Entity
@Table(name = "kpi_snapshots")
public class KpiSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "proyectos_activos", nullable = false)
    private int proyectosActivos;

    @Column(name = "proyectos_atrasados", nullable = false)
    private int proyectosAtrasados;

    @Column(name = "total_recursos_activos", nullable = false)
    private int totalRecursosActivos;

    @Column(name = "capacidad_semanal_horas", nullable = false)
    private int capacidadSemanalHoras;

    @Column(name = "promedio_horas_por_recurso", nullable = false)
    private double promedioHorasPorRecurso;

    @Column(name = "porcentaje_utilizacion", nullable = false)
    private double porcentajeUtilizacion;

    public KpiSnapshot() {}

    public KpiSnapshot(Instant capturedAt, int proyectosActivos, int proyectosAtrasados,
                       int totalRecursosActivos, int capacidadSemanalHoras,
                       double promedioHorasPorRecurso, double porcentajeUtilizacion) {
        this.capturedAt = capturedAt;
        this.proyectosActivos = proyectosActivos;
        this.proyectosAtrasados = proyectosAtrasados;
        this.totalRecursosActivos = totalRecursosActivos;
        this.capacidadSemanalHoras = capacidadSemanalHoras;
        this.promedioHorasPorRecurso = promedioHorasPorRecurso;
        this.porcentajeUtilizacion = porcentajeUtilizacion;
    }

    public Long getId() { return id; }
    public Instant getCapturedAt() { return capturedAt; }
    public int getProyectosActivos() { return proyectosActivos; }
    public int getProyectosAtrasados() { return proyectosAtrasados; }
    public int getTotalRecursosActivos() { return totalRecursosActivos; }
    public int getCapacidadSemanalHoras() { return capacidadSemanalHoras; }
    public double getPromedioHorasPorRecurso() { return promedioHorasPorRecurso; }
    public double getPorcentajeUtilizacion() { return porcentajeUtilizacion; }
}

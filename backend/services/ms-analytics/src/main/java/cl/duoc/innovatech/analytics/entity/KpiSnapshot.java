package cl.duoc.innovatech.analytics.entity;

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

    @Column(name = "active_projects", nullable = false)
    private int activeProjects;

    @Column(name = "delayed_projects", nullable = false)
    private int delayedProjects;

    @Column(name = "total_active_resources", nullable = false)
    private int totalActiveResources;

    @Column(name = "weekly_capacity_hours", nullable = false)
    private int weeklyCapacityHours;

    @Column(name = "avg_hours_per_resource", nullable = false)
    private double avgHoursPerResource;

    @Column(name = "utilization_percentage", nullable = false)
    private double utilizationPercentage;

    public KpiSnapshot() {}

    public KpiSnapshot(Instant capturedAt, int activeProjects, int delayedProjects,
                       int totalActiveResources, int weeklyCapacityHours,
                       double avgHoursPerResource, double utilizationPercentage) {
        this.capturedAt = capturedAt;
        this.activeProjects = activeProjects;
        this.delayedProjects = delayedProjects;
        this.totalActiveResources = totalActiveResources;
        this.weeklyCapacityHours = weeklyCapacityHours;
        this.avgHoursPerResource = avgHoursPerResource;
        this.utilizationPercentage = utilizationPercentage;
    }

    public Long getId() { return id; }
    public Instant getCapturedAt() { return capturedAt; }
    public int getActiveProjects() { return activeProjects; }
    public int getDelayedProjects() { return delayedProjects; }
    public int getTotalActiveResources() { return totalActiveResources; }
    public int getWeeklyCapacityHours() { return weeklyCapacityHours; }
    public double getAvgHoursPerResource() { return avgHoursPerResource; }
    public double getUtilizationPercentage() { return utilizationPercentage; }
}

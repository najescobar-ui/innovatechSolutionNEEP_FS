package cl.duoc.innovatech.bff.domain;

import java.util.List;

// Sealed interface — DashboardDtoFactory returns exactly one of the three
// concrete shapes below, chosen by the caller's role. Adding a new role
// means: new permitted record here + new branch in the factory.
// Nothing else needs to change (Open/Closed).
public sealed interface DashboardDto {

    String role();

    record PMDashboard(
            String role,
            int proyectosSupervisados,
            int tareasEnRiesgo,
            List<String> proximosHitos
    ) implements DashboardDto {}

    record DevDashboard(
            String role,
            int tareasAsignadas,
            int tareasPendientes,
            List<String> proyectosEnCurso
    ) implements DashboardDto {}

    // Director view exposes only aggregated KPIs, never individual data
    // (privacy decision in internal docs §5).
    record DirDashboard(
            String role,
            int proyectosActivos,
            double porcentajeUtilizacion,
            int alertasGlobales
    ) implements DashboardDto {}
}

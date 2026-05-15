package cl.duoc.innovatech.bff.domain;

import java.util.List;

// Sealed: cada rol tiene su forma. Agregar un rol nuevo obliga a declarar
// el record y el switch del factory deja de compilar hasta cubrirlo.
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

    record DirDashboard(
            String role,
            int proyectosActivos,
            double porcentajeUtilizacion,
            int alertasGlobales
    ) implements DashboardDto {}
}

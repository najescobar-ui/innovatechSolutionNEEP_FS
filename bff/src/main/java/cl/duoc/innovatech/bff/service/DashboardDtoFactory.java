package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.DashboardDto;
import cl.duoc.innovatech.bff.domain.ProyectoSummary;
import cl.duoc.innovatech.bff.domain.UserRole;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Antes era hardcoded. Ahora deriva valores de las fuentes reales:
//   - utilizacion / proyectos activos / atrasados: vienen de ms-analitica via KpisService
//   - listas de proyectos en curso / hitos: se calculan desde ProyectosService
// Si un upstream cae, el circuit breaker devuelve "datos no disponibles" y los
// numeros caen a 0 (mejor mostrar cero que un valor erroneo).
@Component
public class DashboardDtoFactory {

    private static final Set<String> ESTADOS_ACTIVOS = Set.of("PLANIFICACION", "EN_CURSO");
    private static final Set<String> ESTADOS_TERMINADOS = Set.of("COMPLETADO", "CANCELADO");
    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE;

    private final KpisService kpisService;
    private final ProyectosService proyectosService;

    public DashboardDtoFactory(KpisService kpisService, ProyectosService proyectosService) {
        this.kpisService = kpisService;
        this.proyectosService = proyectosService;
    }

    public DashboardDto create(UserRole role) {
        var kpi = kpisService.obtener();
        var proyectos = proyectosService.listar().items();

        return switch (role) {
            case PM  -> pmDashboard(kpi, proyectos);
            case DEV -> devDashboard(proyectos);
            case DIR -> dirDashboard(kpi);
        };
    }

    private DashboardDto.PMDashboard pmDashboard(Map<String, Object> kpi, List<ProyectoSummary> proyectos) {
        int supervisados = (int) proyectos.stream()
                .filter(p -> !ESTADOS_TERMINADOS.contains(p.estado()))
                .count();
        int enRiesgo = intDe(kpi, "proyectosAtrasados");

        // proximos 3 proyectos con fechaFin en el futuro, ordenados ascendente
        var hoy = LocalDate.now();
        var hitos = proyectos.stream()
                .filter(p -> p.fechaFinPlanificada() != null && !p.fechaFinPlanificada().isBefore(hoy))
                .filter(p -> !ESTADOS_TERMINADOS.contains(p.estado()))
                .sorted(Comparator.comparing(ProyectoSummary::fechaFinPlanificada))
                .limit(3)
                .map(p -> p.nombre() + " — " + F.format(p.fechaFinPlanificada()))
                .toList();

        return new DashboardDto.PMDashboard("PM", supervisados, enRiesgo, hitos);
    }

    private DashboardDto.DevDashboard devDashboard(List<ProyectoSummary> proyectos) {
        // No hay tabla de tareas/asignaciones aun, asi que esos campos quedan en 0
        // y se hidratan cuando exista la entidad. "ProyectosEnCurso" si se puede.
        var enCurso = proyectos.stream()
                .filter(p -> "EN_CURSO".equals(p.estado()))
                .map(ProyectoSummary::nombre)
                .toList();
        return new DashboardDto.DevDashboard("DEV", 0, 0, enCurso);
    }

    private DashboardDto.DirDashboard dirDashboard(Map<String, Object> kpi) {
        int activos = intDe(kpi, "proyectosActivos");
        double util = doubleDe(kpi, "porcentajeUtilizacion");
        int alertas = intDe(kpi, "proyectosAtrasados");
        return new DashboardDto.DirDashboard("DIR", activos, util, alertas);
    }

    private static int intDe(Map<String, Object> kpi, String campo) {
        var raw = kpi.get(campo);
        return raw instanceof Number n ? n.intValue() : 0;
    }

    private static double doubleDe(Map<String, Object> kpi, String campo) {
        var raw = kpi.get(campo);
        return raw instanceof Number n ? n.doubleValue() : 0.0;
    }
}

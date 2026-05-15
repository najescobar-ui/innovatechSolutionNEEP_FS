package cl.duoc.innovatech.analitica.service;

import cl.duoc.innovatech.analitica.dto.KpiResponse;
import cl.duoc.innovatech.analitica.dto.ProyectoView;
import cl.duoc.innovatech.analitica.dto.RecursoView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KpiService {

    private static final Logger log = LoggerFactory.getLogger(KpiService.class);

    // Estados que cuentan para "activos" y "atrasados".
    private static final Set<String> ACTIVOS = Set.of("PLANIFICACION", "EN_CURSO");
    private static final Set<String> TERMINADOS = Set.of("COMPLETADO", "CANCELADO");

    private final ProyectosClient proyectos;
    private final RecursosClient recursos;

    public KpiService(ProyectosClient proyectos, RecursosClient recursos) {
        this.proyectos = proyectos;
        this.recursos = recursos;
    }

    public KpiResponse calcular() {
        List<ProyectoView> ps;
        List<RecursoView> rs;
        try {
            ps = proyectos.listar();
            rs = recursos.listar();
        } catch (IllegalStateException e) {
            log.warn("KPI no disponible: {}", e.getMessage());
            return KpiResponse.unavailable();
        }

        var hoy = LocalDate.now();
        int activos = 0;
        int atrasados = 0;
        for (var p : ps) {
            if (ACTIVOS.contains(p.estado())) activos++;
            if (p.fechaFinPlanificada() != null
                    && p.fechaFinPlanificada().isBefore(hoy)
                    && !TERMINADOS.contains(p.estado())) {
                atrasados++;
            }
        }

        var recursosActivos = rs.stream().filter(r -> Boolean.TRUE.equals(r.activo())).toList();
        int totalActivos = recursosActivos.size();
        int capacidad = recursosActivos.stream().mapToInt(RecursoView::horasSemanales).sum();
        double promedio = totalActivos == 0 ? 0.0 : (double) capacidad / totalActivos;

        Map<String, Long> porRol = recursosActivos.stream()
                .collect(Collectors.groupingBy(RecursoView::rol, Collectors.counting()));
        Map<String, Long> porEstado = ps.stream()
                .collect(Collectors.groupingBy(ProyectoView::estado, Collectors.counting()));

        return new KpiResponse(
                "ok",
                activos,
                atrasados,
                totalActivos,
                capacidad,
                Math.round(promedio * 100.0) / 100.0,
                porRol,
                porEstado
        );
    }
}

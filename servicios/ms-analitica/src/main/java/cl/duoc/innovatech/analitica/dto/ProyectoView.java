package cl.duoc.innovatech.analitica.dto;

import java.time.LocalDate;

// Lo que necesitamos de ms-proyectos. No importamos su entity, solo
// el shape JSON que devuelve GET /proyectos.
public record ProyectoView(
        Long id,
        String nombre,
        String estado,
        LocalDate fechaFinPlanificada
) {}

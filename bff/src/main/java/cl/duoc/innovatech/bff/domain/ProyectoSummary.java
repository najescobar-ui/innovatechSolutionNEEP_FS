package cl.duoc.innovatech.bff.domain;

import java.time.LocalDate;

public record ProyectoSummary(
        Long id,
        String nombre,
        String descripcion,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaFinPlanificada,
        String responsableId
) {}

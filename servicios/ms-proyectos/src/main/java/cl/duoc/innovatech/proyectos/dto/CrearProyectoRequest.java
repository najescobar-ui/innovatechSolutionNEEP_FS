package cl.duoc.innovatech.proyectos.dto;

import cl.duoc.innovatech.proyectos.entity.EstadoProyecto;

import java.time.LocalDate;

public record CrearProyectoRequest(
        String nombre,
        String descripcion,
        EstadoProyecto estado,
        LocalDate fechaInicio,
        LocalDate fechaFinPlanificada,
        String responsableId
) {
}

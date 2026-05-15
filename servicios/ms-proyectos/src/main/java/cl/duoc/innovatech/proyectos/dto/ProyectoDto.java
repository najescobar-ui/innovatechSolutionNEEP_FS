package cl.duoc.innovatech.proyectos.dto;

import cl.duoc.innovatech.proyectos.entity.EstadoProyecto;
import cl.duoc.innovatech.proyectos.entity.Proyecto;

import java.time.LocalDate;

public record ProyectoDto(
        Long id,
        String nombre,
        String descripcion,
        EstadoProyecto estado,
        LocalDate fechaInicio,
        LocalDate fechaFinPlanificada,
        String responsableId
) {
    public static ProyectoDto fromEntity(Proyecto p) {
        return new ProyectoDto(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getEstado(),
                p.getFechaInicio(),
                p.getFechaFinPlanificada(),
                p.getResponsableId()
        );
    }
}

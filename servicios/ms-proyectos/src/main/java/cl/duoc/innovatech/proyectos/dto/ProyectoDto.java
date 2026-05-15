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
    // Static factory used by the service layer; keeps mapping next to the
    // shape it produces, avoids a separate mapper class while there is
    // only one entity.
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

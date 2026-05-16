package cl.duoc.innovatech.proyectos.dto;

import cl.duoc.innovatech.proyectos.entity.EstadoProyecto;

// Patch parcial: solo se aplica lo que viene no-null.
public record ActualizarProyectoRequest(
        EstadoProyecto estado,
        String responsableId
) {}

package cl.duoc.innovatech.bff.domain;

import java.time.LocalDate;

// Local mirror of the relevant ms-proyectos fields. We deliberately do NOT
// import the entity from ms-proyectos — each microservice owns its model
// and the BFF only knows the wire format.
public record ProyectoSummary(
        Long id,
        String nombre,
        String descripcion,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaFinPlanificada,
        String responsableId
) {}

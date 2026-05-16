package cl.duoc.innovatech.bff.domain;

import java.time.LocalDate;

// Shape espejo del CrearProyectoRequest de ms-proyectos. Si en ms-proyectos
// cambia algo, ajustar aca para no romper el passthrough.
public record CrearProyectoRequest(
        String nombre,
        String descripcion,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaFinPlanificada,
        String responsableId
) {}

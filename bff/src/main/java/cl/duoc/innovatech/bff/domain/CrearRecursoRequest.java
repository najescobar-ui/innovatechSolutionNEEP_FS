package cl.duoc.innovatech.bff.domain;

// Shape espejo del CrearRecursoRequest de ms-recursos.
public record CrearRecursoRequest(
        String nombre,
        String email,
        String rol,
        Integer horasSemanales,
        String competencias
) {}

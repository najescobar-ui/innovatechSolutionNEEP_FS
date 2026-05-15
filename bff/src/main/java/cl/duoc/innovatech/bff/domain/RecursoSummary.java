package cl.duoc.innovatech.bff.domain;

public record RecursoSummary(
        Long id,
        String nombre,
        String email,
        String rol,
        Integer horasSemanales,
        String competencias,
        Boolean activo
) {}

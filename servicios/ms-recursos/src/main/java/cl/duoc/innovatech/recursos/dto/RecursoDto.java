package cl.duoc.innovatech.recursos.dto;

import cl.duoc.innovatech.recursos.entity.RolRecurso;

// Read DTO. Never expose the JPA entity directly (internal docs §7).
public record RecursoDto(
        Long id,
        String nombre,
        String email,
        RolRecurso rol,
        Integer horasSemanales,
        String competencias,
        Boolean activo
) {}

package cl.duoc.innovatech.recursos.dto;

import cl.duoc.innovatech.recursos.entity.RolRecurso;

public record RecursoDto(
        Long id,
        String nombre,
        String email,
        RolRecurso rol,
        Integer horasSemanales,
        String competencias,
        Boolean activo
) {}

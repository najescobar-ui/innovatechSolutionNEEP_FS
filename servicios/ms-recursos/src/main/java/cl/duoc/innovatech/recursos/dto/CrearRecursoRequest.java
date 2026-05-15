package cl.duoc.innovatech.recursos.dto;

import cl.duoc.innovatech.recursos.entity.RolRecurso;

// Input contract for POST /recursos. Kept separate from the read DTO so
// auto-generated fields (id) never appear on the request shape.
public record CrearRecursoRequest(
        String nombre,
        String email,
        RolRecurso rol,
        Integer horasSemanales,
        String competencias
) {}

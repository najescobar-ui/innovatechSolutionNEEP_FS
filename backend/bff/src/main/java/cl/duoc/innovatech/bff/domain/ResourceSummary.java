package cl.duoc.innovatech.bff.domain;

public record ResourceSummary(
        Long id,
        String name,
        String email,
        String role,
        Integer weeklyHours,
        String skills,
        Boolean active
) {}

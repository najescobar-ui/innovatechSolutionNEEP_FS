package cl.duoc.innovatech.resources.dto;

import cl.duoc.innovatech.resources.entity.ResourceRole;

public record CreateResourceRequest(
        String name,
        String email,
        ResourceRole role,
        Integer weeklyHours,
        String skills
) {}

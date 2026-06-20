package com.duoc.resources.dto;

import com.duoc.resources.entity.ResourceRole;

public record ResourceDto(
        Long id,
        String name,
        String email,
        ResourceRole role,
        Integer weeklyHours,
        String skills,
        Boolean active
) {}

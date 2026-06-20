package com.duoc.resources.dto;

import com.duoc.resources.entity.ResourceRole;

public record CreateResourceRequest(
        String name,
        String email,
        ResourceRole role,
        Integer weeklyHours,
        String skills
) {}

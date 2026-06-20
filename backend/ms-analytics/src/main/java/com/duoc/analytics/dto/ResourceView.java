package com.duoc.analytics.dto;

public record ResourceView(
        Long id,
        String role,
        Integer weeklyHours,
        Boolean active
) {}

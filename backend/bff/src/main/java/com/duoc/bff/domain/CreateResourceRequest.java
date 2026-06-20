package com.duoc.bff.domain;

/** Shape espejo del CreateResourceRequest de ms-resources. */
public record CreateResourceRequest(
        String name,
        String email,
        String role,
        Integer weeklyHours,
        String skills
) {}

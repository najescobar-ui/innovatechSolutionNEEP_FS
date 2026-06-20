package com.duoc.bff.domain;

import java.time.LocalDate;

/** Shape espejo del CreateTaskRequest de ms-projects (status como String para passthrough). */
public record CreateTaskRequest(
        Long projectId,
        String title,
        String description,
        String status,
        Long assigneeResourceId,
        Integer estimatedHours,
        LocalDate dueDate
) {}

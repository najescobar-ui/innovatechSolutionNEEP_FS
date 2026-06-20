package com.duoc.projects.dto;

import com.duoc.projects.entity.TaskStatus;

import java.time.LocalDate;

/** Patch parcial: solo se aplica lo que viene no-null. */
public record UpdateTaskRequest(
        String title,
        String description,
        TaskStatus status,
        Long assigneeResourceId,
        Integer estimatedHours,
        LocalDate dueDate
) {
}

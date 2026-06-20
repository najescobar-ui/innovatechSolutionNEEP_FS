package com.duoc.projects.dto;

import com.duoc.projects.entity.TaskStatus;

import java.time.LocalDate;

public record CreateTaskRequest(
        Long projectId,
        String title,
        String description,
        TaskStatus status,
        Long assigneeResourceId,
        Integer estimatedHours,
        LocalDate dueDate
) {
}

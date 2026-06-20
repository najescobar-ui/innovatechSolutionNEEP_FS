package com.duoc.projects.dto;

import com.duoc.projects.entity.Task;
import com.duoc.projects.entity.TaskStatus;

import java.time.LocalDate;

public record TaskDto(
        Long id,
        Long projectId,
        String title,
        String description,
        TaskStatus status,
        Long assigneeResourceId,
        int estimatedHours,
        LocalDate dueDate
) {
    public static TaskDto fromEntity(Task t) {
        return new TaskDto(
                t.getId(),
                t.getProjectId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getAssigneeResourceId(),
                t.getEstimatedHours(),
                t.getDueDate()
        );
    }
}

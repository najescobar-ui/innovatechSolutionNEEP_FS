package cl.duoc.innovatech.projects.dto;

import cl.duoc.innovatech.projects.entity.TaskStatus;

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

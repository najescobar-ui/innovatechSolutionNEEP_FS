package cl.duoc.innovatech.projects.dto;

import cl.duoc.innovatech.projects.entity.Task;
import cl.duoc.innovatech.projects.entity.TaskStatus;

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

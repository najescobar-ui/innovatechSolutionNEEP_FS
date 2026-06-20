package cl.duoc.innovatech.bff.domain;

import java.time.LocalDate;

/** Patch parcial de tarea (passthrough a ms-projects). */
public record UpdateTaskRequest(
        String title,
        String description,
        String status,
        Long assigneeResourceId,
        Integer estimatedHours,
        LocalDate dueDate
) {}

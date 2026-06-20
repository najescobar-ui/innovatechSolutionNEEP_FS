package cl.duoc.innovatech.bff.domain;

import java.time.LocalDate;

public record TaskSummary(
        Long id,
        Long projectId,
        String title,
        String description,
        String status,
        Long assigneeResourceId,
        Integer estimatedHours,
        LocalDate dueDate
) {}

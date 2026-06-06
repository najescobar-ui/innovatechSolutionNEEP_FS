package cl.duoc.innovatech.projects.dto;

import cl.duoc.innovatech.projects.entity.ProjectStatus;

import java.time.LocalDate;

public record CreateProjectRequest(
        String name,
        String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate plannedEndDate,
        String ownerId
) {
}

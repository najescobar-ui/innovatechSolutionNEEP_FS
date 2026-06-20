package com.duoc.projects.dto;

import com.duoc.projects.entity.ProjectStatus;
import com.duoc.projects.entity.Project;

import java.time.LocalDate;

public record ProjectDto(
        Long id,
        String name,
        String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate plannedEndDate,
        String ownerId
) {
    public static ProjectDto fromEntity(Project p) {
        return new ProjectDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getStatus(),
                p.getStartDate(),
                p.getPlannedEndDate(),
                p.getOwnerId()
        );
    }
}

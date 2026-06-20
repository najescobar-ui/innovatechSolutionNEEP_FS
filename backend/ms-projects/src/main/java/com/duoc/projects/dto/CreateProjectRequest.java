package com.duoc.projects.dto;

import com.duoc.projects.entity.ProjectStatus;

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

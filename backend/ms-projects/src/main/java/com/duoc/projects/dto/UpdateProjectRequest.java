package com.duoc.projects.dto;

import com.duoc.projects.entity.ProjectStatus;

/** Patch parcial: solo se aplica lo que viene no-null. */
public record UpdateProjectRequest(
        ProjectStatus status,
        String ownerId
) {}

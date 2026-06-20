package cl.duoc.innovatech.projects.dto;

import cl.duoc.innovatech.projects.entity.ProjectStatus;

/** Patch parcial: solo se aplica lo que viene no-null. */
public record UpdateProjectRequest(
        ProjectStatus status,
        String ownerId
) {}

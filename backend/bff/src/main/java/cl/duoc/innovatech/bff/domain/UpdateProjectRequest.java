package cl.duoc.innovatech.bff.domain;

public record UpdateProjectRequest(
        String status,
        String ownerId
) {}

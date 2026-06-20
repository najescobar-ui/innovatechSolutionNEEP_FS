package com.duoc.bff.domain;

public record UpdateProjectRequest(
        String status,
        String ownerId
) {}

package com.duoc.bff.domain;

import java.util.List;

public record ProjectsResponse(String status, List<ProjectSummary> items) {

    public static ProjectsResponse ok(List<ProjectSummary> items) {
        return new ProjectsResponse("ok", items);
    }

    public static ProjectsResponse unavailable() {
        return new ProjectsResponse("datos no disponibles", List.of());
    }
}

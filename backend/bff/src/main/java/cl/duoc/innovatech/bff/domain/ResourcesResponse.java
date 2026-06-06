package cl.duoc.innovatech.bff.domain;

import java.util.List;

public record ResourcesResponse(String status, List<ResourceSummary> items) {

    public static ResourcesResponse ok(List<ResourceSummary> items) {
        return new ResourcesResponse("ok", items);
    }

    public static ResourcesResponse unavailable() {
        return new ResourcesResponse("datos no disponibles", List.of());
    }
}

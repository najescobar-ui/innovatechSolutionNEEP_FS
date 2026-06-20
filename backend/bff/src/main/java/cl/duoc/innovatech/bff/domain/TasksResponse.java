package cl.duoc.innovatech.bff.domain;

import java.util.List;

public record TasksResponse(String status, List<TaskSummary> items) {

    public static TasksResponse ok(List<TaskSummary> items) {
        return new TasksResponse("ok", items);
    }

    public static TasksResponse unavailable() {
        return new TasksResponse("datos no disponibles", List.of());
    }
}

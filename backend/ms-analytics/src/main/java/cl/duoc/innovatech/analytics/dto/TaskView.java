package cl.duoc.innovatech.analytics.dto;

import java.time.LocalDate;

/**
 * Lo que necesitamos de ms-projects para analitica de tareas. status llega como
 * String (no importamos el enum de ms-projects, solo el shape JSON de GET /tasks).
 */
public record TaskView(
        Long id,
        Long projectId,
        String status,
        Long assigneeResourceId,
        Integer estimatedHours,
        LocalDate dueDate
) {}

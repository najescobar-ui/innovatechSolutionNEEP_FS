package cl.duoc.innovatech.analytics.dto;

import java.time.LocalDate;

/**
 * Lo que necesitamos de ms-projects. No importamos su entity, solo
 * el shape JSON que devuelve GET /projects.
 */
public record ProjectView(
        Long id,
        String name,
        String status,
        LocalDate plannedEndDate
) {}

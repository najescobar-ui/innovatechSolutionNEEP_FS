package cl.duoc.innovatech.bff.domain;

import java.time.LocalDate;

// Shape espejo del CreateProjectRequest de ms-projects. Si en ms-projects
// cambia algo, ajustar aca para no romper el passthrough.
public record CreateProjectRequest(
        String name,
        String description,
        String status,
        LocalDate startDate,
        LocalDate plannedEndDate,
        String ownerId
) {}

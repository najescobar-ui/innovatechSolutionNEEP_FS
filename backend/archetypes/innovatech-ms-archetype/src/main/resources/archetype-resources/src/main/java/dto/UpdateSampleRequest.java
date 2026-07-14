package ${package}.dto;

public record UpdateSampleRequest(
        String description,
        Boolean active
) {}

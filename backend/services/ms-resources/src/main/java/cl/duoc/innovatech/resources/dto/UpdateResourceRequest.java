package cl.duoc.innovatech.resources.dto;

// Patch parcial: por ahora solo permite cambiar el flag activo.
public record UpdateResourceRequest(
        Boolean active
) {}

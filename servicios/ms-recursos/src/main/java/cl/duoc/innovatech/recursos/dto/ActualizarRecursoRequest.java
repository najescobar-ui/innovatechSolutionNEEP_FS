package cl.duoc.innovatech.recursos.dto;

// Patch parcial: por ahora solo permite cambiar el flag activo.
public record ActualizarRecursoRequest(
        Boolean activo
) {}

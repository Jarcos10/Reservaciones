package org.example.reservaciones.features.cuarto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "UpdateDisponibilidadDTO",
        description = "Solicitud para habilitar o deshabilitar una habitación sin eliminarla del catálogo."
)
public record UpdateDisponibilidadDTO(
        @Schema(description = "Indica si la habitación debe quedar disponible para nuevas reservaciones. true = disponible, false = fuera de servicio.", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El estatus para la disponibilidad es obligatorio")
        Boolean disponible
) {
}

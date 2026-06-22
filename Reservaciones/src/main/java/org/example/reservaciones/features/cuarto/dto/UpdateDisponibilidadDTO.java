package org.example.reservaciones.features.cuarto.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO que se utilizará para actualizar la disponibilidad de una habitación (Se usa el método PATCH)
 * @param disponible
 */
public record UpdateDisponibilidadDTO(
        @NotNull(message = "El estatus para la disponibilidad es obligatoria (Esta o no disponible (falso o verdadero))")
        Boolean disponible
) {
}

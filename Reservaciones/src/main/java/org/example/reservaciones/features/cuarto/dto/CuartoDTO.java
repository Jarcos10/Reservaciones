package org.example.reservaciones.features.cuarto.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        name = "CuartoDTO",
        description = "Representación pública de una habitación. Se usa como respuesta para mostrar información del catálogo y disponibilidad sin exponer relaciones internas de base de datos."
)
public record CuartoDTO(
        @Schema(description = "Identificador interno de la habitación.", example = "1")
        Long id,

        @Schema(description = "Tipo o categoría comercial de la habitación.", example = "Habitación doble")
        String tipo,

        @Schema(description = "Número visible de la habitación dentro del hotel.", example = "204")
        int numero,

        @Schema(description = "Precio por noche de la habitación.", example = "1450.00")
        BigDecimal precio,

        @Schema(description = "Cantidad de camas disponibles en la habitación.", example = "2")
        int numeroCamas,

        @Schema(description = "Indica si la habitación está habilitada para nuevas reservaciones.", example = "true")
        boolean disponible
) {
}

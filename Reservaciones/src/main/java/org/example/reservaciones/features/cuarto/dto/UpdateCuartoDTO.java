package org.example.reservaciones.features.cuarto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(
        name = "UpdateCuartoDTO",
        description = "Datos editables de una habitación ya registrada. Se usa para actualizar el catálogo administrativo sin cambiar el identificador interno."
)
public record UpdateCuartoDTO(
        @Schema(description = "Nuevo tipo o categoría comercial de la habitación.", example = "Suite ejecutiva", minLength = 4, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El tipo de cuarto es obligatorio")
        @Size(min = 4, max = 50, message = "El tipo debe estar entre 4 y 50 caracteres")
        String tipo,

        @Schema(description = "Nuevo número visible de la habitación. Debe ser positivo y único.", example = "301", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El número del cuarto es obligatorio")
        @Positive(message = "El número asignado al cuarto debe ser un valor positivo")
        Integer numero,

        @Schema(description = "Nuevo precio por noche de la habitación.", example = "2100.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El precio del cuarto es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El precio tiene un formato inválido (máximo 8 enteros y 2 decimales)")
        BigDecimal precio,

        @Schema(description = "Nueva cantidad de camas disponibles en la habitación.", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 1, message = "El cuarto debe tener al menos una cama")
        int numeroCamas
) {
}

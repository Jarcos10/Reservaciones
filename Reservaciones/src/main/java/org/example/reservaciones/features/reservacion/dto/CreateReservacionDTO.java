package org.example.reservaciones.features.reservacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(
        name = "CreateReservacionDTO",
        description = "Datos solicitados al cliente para crear una reservación. El backend valida fechas, disponibilidad del cuarto y reglas básicas de captura."
)
public record CreateReservacionDTO(
        @Schema(description = "Identificador del cuarto seleccionado por el cliente.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El id del cuarto es obligatorio")
        Long idCuarto,

        @Schema(description = "Nombre completo del cliente responsable de la reservación.", example = "María Fernanda López", minLength = 3, maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        String nombreCliente,

        @Schema(description = "Correo electrónico de contacto del cliente. Se usa para identificar la reservación y futuras notificaciones.", example = "maria.lopez@example.com", maxLength = 120)
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120, message = "El correo no debe exceder 120 caracteres")
        String correoCliente,

        @Schema(description = "Teléfono de contacto del cliente.", example = "5551234567", maxLength = 30)
        @Size(max = 30, message = "El teléfono no debe exceder 30 caracteres")
        String telefonoCliente,

        @Schema(description = "Fecha de entrada al hotel. Debe ser la fecha actual o una fecha futura, en formato ISO yyyy-MM-dd.", example = "2026-07-10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La fecha de entrada es obligatoria")
        @FutureOrPresent(message = "La fecha de entrada no puede ser anterior a la fecha actual")
        LocalDate fechaEntrada,

        @Schema(description = "Fecha de salida del hotel. Debe ser posterior a la fecha de entrada.", example = "2026-07-13", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @Schema(description = "Número de huéspedes contemplados en la estancia.", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 1, message = "Debe haber al menos un huésped")
        int numeroHuespedes,

        @Schema(description = "Comentarios adicionales del cliente, como hora aproximada de llegada o solicitudes especiales.", example = "Llegada aproximada a las 18:00 horas.", maxLength = 500)
        @Size(max = 500, message = "Las observaciones no deben exceder 500 caracteres")
        String observaciones
) {
}

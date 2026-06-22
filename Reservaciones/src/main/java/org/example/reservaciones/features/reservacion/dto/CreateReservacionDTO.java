package org.example.reservaciones.features.reservacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateReservacionDTO(
        @NotNull(message = "El id del cuarto es obligatorio")
        Long idCuarto,

        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        String nombreCliente,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120, message = "El correo no debe exceder 120 caracteres")
        String correoCliente,

        @Size(max = 30, message = "El teléfono no debe exceder 30 caracteres")
        String telefonoCliente,

        @NotNull(message = "La fecha de entrada es obligatoria")
        @FutureOrPresent(message = "La fecha de entrada no puede ser anterior a la fecha actual")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @Min(value = 1, message = "Debe haber al menos un huésped")
        int numeroHuespedes,

        @Size(max = 500, message = "Las observaciones no deben exceder 500 caracteres")
        String observaciones
) {
}

package org.example.reservaciones.features.reservacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.reservaciones.core.domain.EstadoReservacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(
        name = "ReservacionDTO",
        description = "Respuesta con la información operativa de una reservación. Resume datos del cliente, habitación, fechas, importes, estado e indicador de identificación adjunta."
)
public record ReservacionDTO(
        @Schema(description = "Identificador interno de la reservación.", example = "1")
        Long idReservacion,

        @Schema(description = "Identificador de la habitación reservada.", example = "1")
        Long idCuarto,

        @Schema(description = "Número visible de la habitación reservada.", example = "204")
        int numeroCuarto,

        @Schema(description = "Tipo o categoría de la habitación reservada.", example = "Habitación doble")
        String tipoCuarto,

        @Schema(description = "Nombre completo del cliente responsable de la reservación.", example = "María Fernanda López")
        String nombreCliente,

        @Schema(description = "Correo electrónico del cliente.", example = "maria.lopez@example.com")
        String correoCliente,

        @Schema(description = "Teléfono de contacto del cliente.", example = "5551234567")
        String telefonoCliente,

        @Schema(description = "Fecha de entrada al hotel.", example = "2026-07-10")
        LocalDate fechaEntrada,

        @Schema(description = "Fecha de salida del hotel.", example = "2026-07-13")
        LocalDate fechaSalida,

        @Schema(description = "Cantidad de huéspedes registrados para la estancia.", example = "2")
        int numeroHuespedes,

        @Schema(description = "Precio por noche tomado del cuarto al momento de crear la reservación.", example = "1450.00")
        BigDecimal precioNoche,

        @Schema(description = "Importe total calculado para la reservación.", example = "4350.00")
        BigDecimal total,

        @Schema(description = "Estado actual de la reservación dentro del flujo administrativo.", example = "PENDIENTE", implementation = EstadoReservacion.class)
        EstadoReservacion estado,

        @Schema(description = "Observaciones o solicitudes especiales del cliente.", example = "Llegada aproximada a las 18:00 horas.")
        String observaciones,

        @Schema(description = "Fecha y hora en la que se creó la reservación.", example = "2026-06-24T17:35:20")
        LocalDateTime fechaCreacion,

        @Schema(description = "Indica si la reservación cuenta con archivo de identificación asociado.", example = "true")
        boolean tieneIdentificacion
) {
}

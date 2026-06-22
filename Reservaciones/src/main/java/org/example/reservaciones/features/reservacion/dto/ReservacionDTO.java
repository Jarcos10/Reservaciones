package org.example.reservaciones.features.reservacion.dto;

import org.example.reservaciones.core.domain.EstadoReservacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservacionDTO(
        Long idReservacion,
        Long idCuarto,
        int numeroCuarto,
        String tipoCuarto,
        String nombreCliente,
        String correoCliente,
        String telefonoCliente,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        int numeroHuespedes,
        BigDecimal precioNoche,
        BigDecimal total,
        EstadoReservacion estado,
        String observaciones,
        LocalDateTime fechaCreacion,
        boolean tieneIdentificacion
) {
}

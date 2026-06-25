package org.example.reservaciones.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "EstadoReservacion",
        description = "Estado operativo de una reservación dentro del flujo administrativo: PENDIENTE cuando está recién creada, CONFIRMADA cuando fue validada, CANCELADA cuando se anuló y FINALIZADA cuando la estancia concluyó."
)
public enum EstadoReservacion {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    FINALIZADA
}

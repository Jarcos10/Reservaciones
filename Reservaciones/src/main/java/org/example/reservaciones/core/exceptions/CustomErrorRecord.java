package org.example.reservaciones.core.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "ErrorResponse",
        description = "Formato estándar de error usado por el manejador global de excepciones. Permite identificar cuándo ocurrió el error, qué mensaje debe mostrarse y el detalle técnico o de validación relacionado."
)
public record CustomErrorRecord(
        @Schema(description = "Fecha y hora en la que se generó el error.", example = "2026-06-24T17:28:52.860")
        LocalDateTime dateTime,

        @Schema(description = "Mensaje principal del error que puede mostrarse al usuario o al administrador.", example = "No existe una habitación con el identificador proporcionado")
        String messaege,

        @Schema(description = "Detalle adicional del error, como ruta invocada o campos inválidos.", example = "uri=/api/v1/cuartos/99")
        String details
) {
}

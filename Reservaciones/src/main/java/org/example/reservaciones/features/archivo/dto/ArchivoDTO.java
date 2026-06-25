package org.example.reservaciones.features.archivo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.reservaciones.core.domain.TipoDocumento;

@Schema(
        name = "ArchivoDTO",
        description = "Metadatos de un archivo asociado a una reservación. No expone el contenido binario del archivo; solo información necesaria para administración."
)
public record ArchivoDTO(
        @Schema(description = "Identificador interno del archivo.", example = "1")
        Long idArchivo,

        @Schema(description = "Nombre original del archivo cargado por el usuario.", example = "identificacion_cliente.pdf")
        String nombreArchivo,

        @Schema(description = "Nombre interno con el que se almacenó el archivo en el servidor para evitar colisiones.", example = "identificacion-1-550e8400-e29b-41d4-a716-446655440000.pdf")
        String nombreGuardado,

        @Schema(description = "Tipo MIME del archivo almacenado.", example = "application/pdf")
        String tipoArchivo,

        @Schema(description = "Clasificación funcional del documento dentro del sistema.", example = "IDENTIFICACION", implementation = TipoDocumento.class)
        TipoDocumento tipoDocumento,

        @Schema(description = "Tamaño del archivo en bytes.", example = "245760")
        Long tamanoBytes,

        @Schema(description = "Identificador de la reservación a la que pertenece el archivo.", example = "1")
        Long idReservacion
) {
}

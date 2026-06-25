package org.example.reservaciones.features.archivo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        name = "RespuestaArchivoDTO",
        description = "Respuesta generada después de cargar correctamente un archivo de identificación. Resume el resultado y los metadatos principales del archivo guardado."
)
public class RespuestaDTO {

    @Schema(description = "Mensaje descriptivo del resultado de la operación.", example = "Identificación subida correctamente")
    private String mensaje;

    @Schema(description = "Identificador interno del archivo guardado.", example = "1")
    private Long idArchivo;

    @Schema(description = "Nombre original del archivo enviado por el usuario.", example = "identificacion_cliente.pdf")
    private String nombreArchivo;

    @Schema(description = "Nombre físico con el que se guardó el archivo en el servidor.", example = "identificacion-1-550e8400-e29b-41d4-a716-446655440000.pdf")
    private String nombreGuardado;

    @Schema(description = "Tipo MIME detectado o asignado para el archivo.", example = "application/pdf")
    private String tipoArchivo;

    @Schema(description = "Tamaño del archivo en bytes.", example = "245760")
    private Long tamanoBytes;
}

package org.example.reservaciones.features.archivo.dto;

import lombok.Data;

@Data
public class RespuestaDTO {
    private String mensaje;
    private Long idArchivo;
    private String nombreArchivo;
    private String nombreGuardado;
    private String tipoArchivo;
    private Long tamanoBytes;
}

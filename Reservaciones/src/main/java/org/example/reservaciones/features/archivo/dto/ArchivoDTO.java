package org.example.reservaciones.features.archivo.dto;

import org.example.reservaciones.core.domain.TipoDocumento;

public record ArchivoDTO(
        Long idArchivo,
        String nombreArchivo,
        String nombreGuardado,
        String tipoArchivo,
        TipoDocumento tipoDocumento,
        Long tamanoBytes,
        Long idReservacion
) {
}

package org.example.reservaciones.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "TipoDocumento",
        description = "Clasificación funcional de los documentos almacenados para una reservación. Actualmente se maneja IDENTIFICACION para documentos de identidad del cliente."
)
public enum TipoDocumento {
    IDENTIFICACION
}

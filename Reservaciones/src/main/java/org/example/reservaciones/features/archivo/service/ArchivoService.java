package org.example.reservaciones.features.archivo.service;

import org.example.reservaciones.core.domain.Archivo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ArchivoService {

    Archivo guardarIdentificacionDeReservacion(Long idReservacion, MultipartFile archivo) throws IOException;

    Archivo obtenerIdentificacionDeReservacion(Long idReservacion);

    Resource cargarIdentificacionDeReservacion(Long idReservacion);
}

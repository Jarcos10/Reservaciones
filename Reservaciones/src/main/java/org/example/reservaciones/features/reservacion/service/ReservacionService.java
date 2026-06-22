package org.example.reservaciones.features.reservacion.service;

import org.example.reservaciones.features.reservacion.dto.CreateReservacionDTO;
import org.example.reservaciones.features.reservacion.dto.ReservacionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ReservacionService {

    ReservacionDTO createReservacion(CreateReservacionDTO dto);

    ReservacionDTO createReservacionConIdentificacion(CreateReservacionDTO dto, MultipartFile archivo) throws IOException;

    List<ReservacionDTO> readAllReservaciones();

    List<ReservacionDTO> readReservacionesByCuarto(Long idCuarto);

    ReservacionDTO readById(Long id);

    ReservacionDTO confirmarReservacion(Long id);

    ReservacionDTO cancelarReservacion(Long id);

    ReservacionDTO finalizarReservacion(Long id);

    void deleteReservacion(Long id);
}

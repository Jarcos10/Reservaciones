package org.example.reservaciones.features.reservacion.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.reservaciones.core.domain.Cuarto;
import org.example.reservaciones.core.domain.EstadoReservacion;
import org.example.reservaciones.core.domain.Reservacion;
import org.example.reservaciones.core.domain.TipoDocumento;
import org.example.reservaciones.core.exceptions.BussinesValidationException;
import org.example.reservaciones.core.exceptions.EntityNotFoundException;
import org.example.reservaciones.features.archivo.repository.ArchivoRepository;
import org.example.reservaciones.features.archivo.service.ArchivoService;
import org.example.reservaciones.features.cuarto.repository.CuartoRepository;
import org.example.reservaciones.features.reservacion.dto.CreateReservacionDTO;
import org.example.reservaciones.features.reservacion.dto.ReservacionDTO;
import org.example.reservaciones.features.reservacion.repository.ReservacionRepository;
import org.example.reservaciones.features.reservacion.service.ReservacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservacionServiceImpl implements ReservacionService {

    private final ReservacionRepository reservacionRepository;
    private final CuartoRepository cuartoRepository;
    private final ArchivoRepository archivoRepository;
    private final ArchivoService archivoService;

    @Override
    @Transactional
    public ReservacionDTO createReservacion(CreateReservacionDTO dto) {
        Reservacion reservacion = construirReservacion(dto);
        return mapearADTO(reservacionRepository.save(reservacion));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservacionDTO createReservacionConIdentificacion(CreateReservacionDTO dto, MultipartFile archivo) throws IOException {
        Reservacion reservacion = reservacionRepository.save(construirReservacion(dto));
        archivoService.guardarIdentificacionDeReservacion(reservacion.getIdReservacion(), archivo);
        return mapearADTO(reservacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservacionDTO> readAllReservaciones() {
        return reservacionRepository.findAll().stream().map(this::mapearADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservacionDTO> readReservacionesByCuarto(Long idCuarto) {
        if (!cuartoRepository.existsById(idCuarto)) {
            throw new EntityNotFoundException("El cuarto " + idCuarto + " no existe");
        }

        return reservacionRepository.findByCuartoId(idCuarto).stream().map(this::mapearADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservacionDTO readById(Long id) {
        return reservacionRepository.findById(id)
                .map(this::mapearADTO)
                .orElseThrow(() -> new EntityNotFoundException("La reservación " + id + " no existe"));
    }

    @Override
    @Transactional
    public ReservacionDTO confirmarReservacion(Long id) {
        Reservacion reservacion = obtenerReservacion(id);
        reservacion.setEstado(EstadoReservacion.CONFIRMADA);
        return mapearADTO(reservacionRepository.save(reservacion));
    }

    @Override
    @Transactional
    public ReservacionDTO cancelarReservacion(Long id) {
        Reservacion reservacion = obtenerReservacion(id);
        reservacion.setEstado(EstadoReservacion.CANCELADA);
        return mapearADTO(reservacionRepository.save(reservacion));
    }

    @Override
    @Transactional
    public ReservacionDTO finalizarReservacion(Long id) {
        Reservacion reservacion = obtenerReservacion(id);
        reservacion.setEstado(EstadoReservacion.FINALIZADA);
        return mapearADTO(reservacionRepository.save(reservacion));
    }

    @Override
    @Transactional
    public void deleteReservacion(Long id) {
        if (!reservacionRepository.existsById(id)) {
            throw new EntityNotFoundException("La reservación " + id + " no existe");
        }

        reservacionRepository.deleteById(id);
    }

    private Reservacion obtenerReservacion(Long id) {
        return reservacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("La reservación " + id + " no existe"));
    }

    private Reservacion construirReservacion(CreateReservacionDTO dto) {
        if (!dto.fechaEntrada().isBefore(dto.fechaSalida())) {
            throw new BussinesValidationException("La fecha de entrada debe ser menor a la fecha de salida");
        }

        Cuarto cuarto = cuartoRepository.findById(dto.idCuarto())
                .orElseThrow(() -> new EntityNotFoundException("El cuarto " + dto.idCuarto() + " no existe"));

        if (!cuarto.isDisponible()) {
            throw new BussinesValidationException("El cuarto seleccionado no está disponible para reservaciones");
        }

        boolean existeTraslape = reservacionRepository.existeTraslapeDeFechas(
                cuarto.getId(),
                dto.fechaEntrada(),
                dto.fechaSalida(),
                List.of(EstadoReservacion.PENDIENTE, EstadoReservacion.CONFIRMADA)
        );

        if (existeTraslape) {
            throw new BussinesValidationException("El cuarto ya tiene una reservación activa en esas fechas");
        }

        long noches = ChronoUnit.DAYS.between(dto.fechaEntrada(), dto.fechaSalida());
        BigDecimal precioNoche = cuarto.getPrecio();
        BigDecimal total = precioNoche.multiply(BigDecimal.valueOf(noches));

        return Reservacion.builder()
                .cuarto(cuarto)
                .nombreCliente(dto.nombreCliente())
                .correoCliente(dto.correoCliente())
                .telefonoCliente(dto.telefonoCliente())
                .fechaEntrada(dto.fechaEntrada())
                .fechaSalida(dto.fechaSalida())
                .numeroHuespedes(dto.numeroHuespedes())
                .precioNoche(precioNoche)
                .total(total)
                .estado(EstadoReservacion.PENDIENTE)
                .observaciones(dto.observaciones())
                .build();
    }

    private ReservacionDTO mapearADTO(Reservacion reservacion) {
        boolean tieneIdentificacion = reservacion.getIdReservacion() != null
                && archivoRepository.existsByReservacionIdReservacionAndTipoDocumento(
                reservacion.getIdReservacion(),
                TipoDocumento.IDENTIFICACION
        );

        return new ReservacionDTO(
                reservacion.getIdReservacion(),
                reservacion.getCuarto().getId(),
                reservacion.getCuarto().getNumero(),
                reservacion.getCuarto().getTipo(),
                reservacion.getNombreCliente(),
                reservacion.getCorreoCliente(),
                reservacion.getTelefonoCliente(),
                reservacion.getFechaEntrada(),
                reservacion.getFechaSalida(),
                reservacion.getNumeroHuespedes(),
                reservacion.getPrecioNoche(),
                reservacion.getTotal(),
                reservacion.getEstado(),
                reservacion.getObservaciones(),
                reservacion.getFechaCreacion(),
                tieneIdentificacion
        );
    }
}

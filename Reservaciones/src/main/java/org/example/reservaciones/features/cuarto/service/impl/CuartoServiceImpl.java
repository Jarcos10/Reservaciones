package org.example.reservaciones.features.cuarto.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.reservaciones.core.domain.Cuarto;
import org.example.reservaciones.core.exceptions.BussinesValidationException;
import org.example.reservaciones.core.exceptions.EntityNotFoundException;
import org.example.reservaciones.features.cuarto.dto.CreateCuartoDTO;
import org.example.reservaciones.features.cuarto.dto.CuartoDTO;
import org.example.reservaciones.features.cuarto.dto.UpdateCuartoDTO;
import org.example.reservaciones.features.cuarto.dto.UpdateDisponibilidadDTO;
import org.example.reservaciones.features.cuarto.repository.CuartoRepository;
import org.example.reservaciones.features.cuarto.service.CuartoService;
import org.example.reservaciones.features.reservacion.repository.ReservacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuartoServiceImpl implements CuartoService {

    private final CuartoRepository cuartoRepository;
    private final ReservacionRepository reservacionRepository;

    @Override
    @Transactional
    public CuartoDTO createCuarto(CreateCuartoDTO dto) {
        if (cuartoRepository.findByNumero(dto.numero()).isPresent()) {
            throw new BussinesValidationException("El número de cuarto " + dto.numero() + " ya está registrado");
        }

        Cuarto cuarto = mapearAEntidad(dto);
        cuarto.setDisponible(true);

        return mapearADTO(cuartoRepository.save(cuarto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuartoDTO> readAllCuartos() {
        return cuartoRepository.findAll().stream().map(this::mapearADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuartoDTO> readAvailableCuartos(LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (fechaEntrada == null || fechaSalida == null) {
            throw new BussinesValidationException("Debes indicar la fecha de entrada y la fecha de salida");
        }
        if (!fechaEntrada.isBefore(fechaSalida)) {
            throw new BussinesValidationException("La fecha de entrada debe ser menor a la fecha de salida");
        }
        return cuartoRepository.findCuartosDisponibles(fechaEntrada, fechaSalida)
                .stream()
                .map(this::mapearADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CuartoDTO readById(Long id) {
        return cuartoRepository.findById(id)
                .map(this::mapearADTO)
                .orElseThrow(() -> new EntityNotFoundException("El cuarto " + id + " no existe"));
    }

    @Override
    @Transactional
    public CuartoDTO updateCuarto(Long id, UpdateCuartoDTO dto) {
        Cuarto cuarto = cuartoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El cuarto " + id + " no existe"));

        if (dto.numero() != null && dto.numero() != cuarto.getNumero()) {
            if (cuartoRepository.findByNumero(dto.numero()).isPresent()) {
                throw new BussinesValidationException("El número " + dto.numero() + " ya está registrado");
            }
            cuarto.setNumero(dto.numero());
        }

        cuarto.setTipo(dto.tipo());
        cuarto.setPrecio(dto.precio());
        cuarto.setNumeroCamas(dto.numeroCamas());

        return mapearADTO(cuartoRepository.save(cuarto));
    }

    @Override
    @Transactional
    public CuartoDTO updateDisponibilidad(Long id, UpdateDisponibilidadDTO dto) {
        Cuarto cuarto = cuartoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El cuarto " + id + " no existe"));

        cuarto.setDisponible(dto.disponible());
        return mapearADTO(cuartoRepository.save(cuarto));
    }

    @Override
    @Transactional
    public void deleteCuarto(Long id) {
        Cuarto cuarto = cuartoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El cuarto " + id + " no existe"));

        if (!reservacionRepository.findByCuartoId(id).isEmpty()) {
            throw new BussinesValidationException(
                    "No se puede eliminar el cuarto " + cuarto.getNumero() +
                    " porque tiene reservaciones registradas. Puedes deshabilitarlo si ya no debe reservarse."
            );
        }

        cuartoRepository.delete(cuarto);
    }

    private Cuarto mapearAEntidad(CreateCuartoDTO dto) {
        if (dto == null) {
            return null;
        }
        Cuarto entidad = new Cuarto();
        entidad.setTipo(dto.tipo());
        entidad.setNumero(dto.numero());
        entidad.setNumeroCamas(dto.numeroCamas());
        entidad.setPrecio(dto.precio());
        return entidad;
    }

    private CuartoDTO mapearADTO(Cuarto entidad) {
        if (entidad == null) {
            return null;
        }
        return new CuartoDTO(
                entidad.getId(),
                entidad.getTipo(),
                entidad.getNumero(),
                entidad.getPrecio(),
                entidad.getNumeroCamas(),
                entidad.isDisponible()
        );
    }
}

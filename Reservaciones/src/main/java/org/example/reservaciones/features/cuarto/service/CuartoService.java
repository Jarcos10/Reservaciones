package org.example.reservaciones.features.cuarto.service;

import org.example.reservaciones.features.cuarto.dto.CreateCuartoDTO;
import org.example.reservaciones.features.cuarto.dto.CuartoDTO;
import org.example.reservaciones.features.cuarto.dto.UpdateCuartoDTO;
import org.example.reservaciones.features.cuarto.dto.UpdateDisponibilidadDTO;

import java.time.LocalDate;
import java.util.List;

public interface CuartoService {

    CuartoDTO createCuarto(CreateCuartoDTO dto);
    List<CuartoDTO> readAllCuartos();
    List<CuartoDTO> readAvailableCuartos(LocalDate fechaEntrada, LocalDate fechaSalida);
    CuartoDTO readById(Long id);
    CuartoDTO updateCuarto(Long id, UpdateCuartoDTO dto);
    CuartoDTO updateDisponibilidad(Long id, UpdateDisponibilidadDTO dto);
    void deleteCuarto(Long id);
}

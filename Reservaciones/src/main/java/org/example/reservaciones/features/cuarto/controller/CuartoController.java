package org.example.reservaciones.features.cuarto.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reservaciones.features.cuarto.dto.CreateCuartoDTO;
import org.example.reservaciones.features.cuarto.dto.CuartoDTO;
import org.example.reservaciones.features.cuarto.dto.UpdateCuartoDTO;
import org.example.reservaciones.features.cuarto.dto.UpdateDisponibilidadDTO;
import org.example.reservaciones.features.cuarto.service.CuartoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cuartos")
@RequiredArgsConstructor
@Tag(name= "Cuarto", description = "Operaciones para las habitaciones")
public class CuartoController {

    private final CuartoService cuartoService;

    @PostMapping
    public ResponseEntity<CuartoDTO> createCuarto(@Valid @RequestBody CreateCuartoDTO dto){
        return new ResponseEntity<>(cuartoService.createCuarto(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CuartoDTO>> findAllCuartos(){
        return ResponseEntity.ok(cuartoService.readAllCuartos());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<CuartoDTO>> findAvailableCuartos(
            @RequestParam LocalDate fechaEntrada,
            @RequestParam LocalDate fechaSalida
    ) {
        return ResponseEntity.ok(cuartoService.readAvailableCuartos(fechaEntrada, fechaSalida));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuartoDTO> findCuarto(@PathVariable Long id){
        return ResponseEntity.ok(cuartoService.readById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuartoDTO> updateCuarto(@Valid @RequestBody UpdateCuartoDTO dto, @PathVariable Long id){
        return ResponseEntity.ok(cuartoService.updateCuarto(id, dto));
    }

    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<CuartoDTO> updateCuartoDisponibilidad(@PathVariable Long id, @Valid @RequestBody UpdateDisponibilidadDTO dto){
        return ResponseEntity.ok(cuartoService.updateDisponibilidad(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCuarto (@PathVariable Long id){
        cuartoService.deleteCuarto(id);
        return ResponseEntity.noContent().build();
    }
}

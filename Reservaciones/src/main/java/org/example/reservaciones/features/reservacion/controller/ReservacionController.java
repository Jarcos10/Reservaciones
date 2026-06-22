package org.example.reservaciones.features.reservacion.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reservaciones.features.reservacion.dto.CreateReservacionDTO;
import org.example.reservaciones.features.reservacion.dto.ReservacionDTO;
import org.example.reservaciones.features.reservacion.service.ReservacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservaciones")
@RequiredArgsConstructor
@Tag(name = "Reservaciones", description = "Operaciones para reservar habitaciones")
public class ReservacionController {

    private final ReservacionService reservacionService;

    @PostMapping
    public ResponseEntity<ReservacionDTO> createReservacion(@Valid @RequestBody CreateReservacionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservacionService.createReservacion(dto));
    }

    @PostMapping(value = "/con-identificacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReservacionDTO> createReservacionConIdentificacion(
            @Valid @RequestPart("datos") CreateReservacionDTO dto,
            @RequestPart("archivo") MultipartFile archivo
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservacionService.createReservacionConIdentificacion(dto, archivo));
    }

    @GetMapping
    public ResponseEntity<List<ReservacionDTO>> findAllReservaciones() {
        return ResponseEntity.ok(reservacionService.readAllReservaciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservacionDTO> findReservacion(@PathVariable Long id) {
        return ResponseEntity.ok(reservacionService.readById(id));
    }

    @GetMapping("/cuarto/{idCuarto}")
    public ResponseEntity<List<ReservacionDTO>> findReservacionesByCuarto(@PathVariable Long idCuarto) {
        return ResponseEntity.ok(reservacionService.readReservacionesByCuarto(idCuarto));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ReservacionDTO> confirmarReservacion(@PathVariable Long id) {
        return ResponseEntity.ok(reservacionService.confirmarReservacion(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ReservacionDTO> cancelarReservacion(@PathVariable Long id) {
        return ResponseEntity.ok(reservacionService.cancelarReservacion(id));
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ReservacionDTO> finalizarReservacion(@PathVariable Long id) {
        return ResponseEntity.ok(reservacionService.finalizarReservacion(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservacion(@PathVariable Long id) {
        reservacionService.deleteReservacion(id);
        return ResponseEntity.noContent().build();
    }
}

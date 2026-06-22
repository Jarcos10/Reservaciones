package org.example.reservaciones.features.archivo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.reservaciones.core.domain.Archivo;
import org.example.reservaciones.features.archivo.dto.ArchivoDTO;
import org.example.reservaciones.features.archivo.dto.RespuestaDTO;
import org.example.reservaciones.features.archivo.service.ArchivoService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/archivos")
@RequiredArgsConstructor
@Tag(name = "Archivos", description = "Operaciones para archivos de reservaciones")
public class ArchivoController {

    private final ArchivoService archivoService;

    @PostMapping("/reservaciones/{idReservacion}/identificacion")
    public ResponseEntity<RespuestaDTO> subirIdentificacion(
            @PathVariable Long idReservacion,
            @RequestParam("archivo") MultipartFile archivo
    ) throws IOException {
        Archivo archivoGuardado = archivoService.guardarIdentificacionDeReservacion(idReservacion, archivo);

        RespuestaDTO respuestaDTO = new RespuestaDTO();
        respuestaDTO.setMensaje("Identificación subida correctamente");
        respuestaDTO.setIdArchivo(archivoGuardado.getIdArchivo());
        respuestaDTO.setNombreArchivo(archivoGuardado.getNombreArchivo());
        respuestaDTO.setNombreGuardado(archivoGuardado.getNombreGuardado());
        respuestaDTO.setTipoArchivo(archivoGuardado.getTipoArchivo());
        respuestaDTO.setTamanoBytes(archivoGuardado.getTamanoBytes());

        return ResponseEntity.ok(respuestaDTO);
    }

    @GetMapping("/reservaciones/{idReservacion}/identificacion/info")
    public ResponseEntity<ArchivoDTO> obtenerInfoIdentificacion(@PathVariable Long idReservacion) {
        Archivo archivo = archivoService.obtenerIdentificacionDeReservacion(idReservacion);
        return ResponseEntity.ok(mapearADTO(archivo));
    }

    @GetMapping("/reservaciones/{idReservacion}/identificacion")
    public ResponseEntity<Resource> descargarIdentificacion(@PathVariable Long idReservacion) {
        Archivo archivo = archivoService.obtenerIdentificacionDeReservacion(idReservacion);
        Resource recurso = archivoService.cargarIdentificacionDeReservacion(idReservacion);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(archivo.getNombreArchivo())
                                .build()
                                .toString()
                )
                .body(recurso);
    }

    private ArchivoDTO mapearADTO(Archivo archivo) {
        Long idReservacion = archivo.getReservacion() != null
                ? archivo.getReservacion().getIdReservacion()
                : null;

        return new ArchivoDTO(
                archivo.getIdArchivo(),
                archivo.getNombreArchivo(),
                archivo.getNombreGuardado(),
                archivo.getTipoArchivo(),
                archivo.getTipoDocumento(),
                archivo.getTamanoBytes(),
                idReservacion
        );
    }
}

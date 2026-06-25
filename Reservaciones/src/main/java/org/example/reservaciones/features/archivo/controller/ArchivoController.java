package org.example.reservaciones.features.archivo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.reservaciones.core.domain.Archivo;
import org.example.reservaciones.core.exceptions.CustomErrorRecord;
import org.example.reservaciones.features.archivo.dto.ArchivoDTO;
import org.example.reservaciones.features.archivo.dto.RespuestaDTO;
import org.example.reservaciones.features.archivo.service.ArchivoService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/archivos")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
@Tag(
        name = "Archivos de reservación",
        description = "Módulo complementario para almacenar y consultar archivos asociados a reservaciones. Actualmente administra la identificación del cliente en formato PDF, guardando el archivo en el servidor y sus metadatos en base de datos."
)
public class ArchivoController {

    private final ArchivoService archivoService;

    @Operation(
            summary = "Subir identificación de una reservación",
            description = "Carga el archivo de identificación del cliente para una reservación existente. Puede ser utilizado por USUARIO o ADMIN. El archivo se almacena físicamente en el servidor y la base de datos conserva únicamente metadatos como nombre, tipo, tamaño y relación con la reservación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Identificación subida correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RespuestaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Archivo inválido, vacío, con formato no permitido o mayor al tamaño máximo configurado.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))),
            @ApiResponse(responseCode = "401", description = "No se enviaron credenciales válidas mediante HTTP Basic.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol USUARIO ni ADMIN.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "No existe la reservación a la que se desea asociar el archivo.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class)))
    })
    @PostMapping("/reservaciones/{idReservacion}/identificacion")
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMIN')")
    public ResponseEntity<RespuestaDTO> subirIdentificacion(
            @Parameter(description = "Identificador de la reservación a la que se asociará la identificación.", example = "1", required = true)
            @PathVariable Long idReservacion,

            @Parameter(
                    description = "Archivo PDF de identificación del cliente. Debe enviarse con el nombre de campo 'archivo'.",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))
            )
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

    @Operation(
            summary = "Consultar metadatos de la identificación",
            description = "Obtiene los datos descriptivos del archivo de identificación asociado a una reservación, sin descargar el archivo. Requiere rol ADMIN porque expone información documental del cliente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadatos de identificación obtenidos correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ArchivoDTO.class))),
            @ApiResponse(responseCode = "401", description = "No se enviaron credenciales válidas mediante HTTP Basic.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "La reservación no existe o no tiene identificación asociada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class)))
    })
    @GetMapping("/reservaciones/{idReservacion}/identificacion/info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArchivoDTO> obtenerInfoIdentificacion(
            @Parameter(description = "Identificador de la reservación de la que se consultará la identificación.", example = "1", required = true)
            @PathVariable Long idReservacion
    ) {
        Archivo archivo = archivoService.obtenerIdentificacionDeReservacion(idReservacion);
        return ResponseEntity.ok(mapearADTO(archivo));
    }

    @Operation(
            summary = "Descargar identificación de una reservación",
            description = "Descarga el archivo PDF de identificación asociado a una reservación. Requiere rol ADMIN porque permite acceder al documento del cliente. La respuesta se entrega como archivo adjunto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo PDF descargado correctamente.", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "No se enviaron credenciales válidas mediante HTTP Basic.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "La reservación no existe o no tiene identificación asociada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class)))
    })
    @GetMapping("/reservaciones/{idReservacion}/identificacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> descargarIdentificacion(
            @Parameter(description = "Identificador de la reservación de la que se descargará la identificación.", example = "1", required = true)
            @PathVariable Long idReservacion
    ) {
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

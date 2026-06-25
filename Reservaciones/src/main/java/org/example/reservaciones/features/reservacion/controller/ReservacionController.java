package org.example.reservaciones.features.reservacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reservaciones.core.exceptions.CustomErrorRecord;
import org.example.reservaciones.features.reservacion.dto.CreateReservacionDTO;
import org.example.reservaciones.features.reservacion.dto.ReservacionDTO;
import org.example.reservaciones.features.reservacion.service.ReservacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservaciones")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
@Tag(
        name = "Reservaciones",
        description = "Módulo que administra el ciclo de vida de una reservación: creación por parte del usuario, consulta administrativa y cambios de estado como confirmar, cancelar o finalizar. Todas las operaciones requieren autenticación HTTP Basic."
)
public class ReservacionController {

    private final ReservacionService reservacionService;

    @Operation(
            summary = "Crear una reservación",
            description = "Registra una nueva reservación para un cuarto disponible en un rango de fechas. El rol USUARIO puede crear reservaciones propias y el rol ADMIN puede registrar reservaciones desde administración. El sistema calcula el precio por noche y el total con base en el cuarto seleccionado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservación creada correctamente en estado PENDIENTE.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReservacionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o regla de negocio incumplida, por ejemplo fechas incorrectas, cuarto no disponible o número de huéspedes inválido.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se enviaron credenciales válidas mediante HTTP Basic.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no tiene rol USUARIO ni ADMIN.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe el cuarto indicado para la reservación.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMIN')")
    public ResponseEntity<ReservacionDTO> createReservacion(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información del cliente, fechas de estancia y cuarto seleccionado para crear la reservación.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateReservacionDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "idCuarto": 1,
                                      "nombreCliente": "María Fernanda López",
                                      "correoCliente": "maria.lopez@example.com",
                                      "telefonoCliente": "5551234567",
                                      "fechaEntrada": "2026-07-10",
                                      "fechaSalida": "2026-07-13",
                                      "numeroHuespedes": 2,
                                      "observaciones": "Llegada aproximada a las 18:00 horas."
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody CreateReservacionDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservacionService.createReservacion(dto));
    }

    @Operation(
            summary = "Crear una reservación con identificación",
            description = "Registra la reservación y recibe en la misma operación el archivo PDF de identificación del cliente. Se utiliza cuando el flujo de reservación requiere adjuntar identificación desde el formulario público o desde recepción."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservación creada correctamente y archivo de identificación almacenado en el servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReservacionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de reservación inválidos, archivo vacío, archivo con tipo no permitido o tamaño superior al configurado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se enviaron credenciales válidas mediante HTTP Basic.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no tiene rol USUARIO ni ADMIN.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe el cuarto indicado para la reservación.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            )
    })
    @PostMapping(value = "/con-identificacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMIN')")
    public ResponseEntity<ReservacionDTO> createReservacionConIdentificacion(
            @Parameter(
                    description = "Objeto JSON con los datos de la reservación. Debe enviarse como parte multipart con nombre 'datos'.",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateReservacionDTO.class))
            )
            @Valid @RequestPart("datos") CreateReservacionDTO dto,

            @Parameter(
                    description = "Archivo PDF de identificación del cliente. Debe enviarse como parte multipart con nombre 'archivo'.",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("archivo") MultipartFile archivo
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservacionService.createReservacionConIdentificacion(dto, archivo));
    }

    @Operation(
            summary = "Consultar todas las reservaciones",
            description = "Obtiene el listado administrativo de reservaciones registradas en el sistema. Incluye datos del cliente, habitación, fechas, importe total, estado actual e indicador de identificación adjunta. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de reservaciones obtenido correctamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ReservacionDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se enviaron credenciales válidas mediante HTTP Basic.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no tiene rol ADMIN.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservacionDTO>> findAllReservaciones() {
        return ResponseEntity.ok(reservacionService.readAllReservaciones());
    }

    @Operation(
            summary = "Consultar una reservación por identificador",
            description = "Obtiene el detalle de una reservación específica. Está pensado para uso administrativo cuando se requiere revisar la información completa de una estancia o dar seguimiento al estado de la reservación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservación encontrada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReservacionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se enviaron credenciales válidas mediante HTTP Basic.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no tiene rol ADMIN.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe una reservación con el identificador proporcionado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservacionDTO> findReservacion(
            @Parameter(description = "Identificador interno de la reservación.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(reservacionService.readById(id));
    }

    @Operation(
            summary = "Consultar reservaciones de una habitación",
            description = "Obtiene las reservaciones asociadas a un cuarto específico. Sirve para revisar ocupación, historial de uso o posibles bloqueos de disponibilidad de una habitación. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservaciones de la habitación obtenidas correctamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ReservacionDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se enviaron credenciales válidas mediante HTTP Basic.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no tiene rol ADMIN.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @GetMapping("/cuarto/{idCuarto}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservacionDTO>> findReservacionesByCuarto(
            @Parameter(description = "Identificador de la habitación de la que se consultarán las reservaciones.", example = "1", required = true)
            @PathVariable Long idCuarto
    ) {
        return ResponseEntity.ok(reservacionService.readReservacionesByCuarto(idCuarto));
    }

    @Operation(
            summary = "Confirmar una reservación",
            description = "Cambia el estado de una reservación PENDIENTE a CONFIRMADA. Esta acción representa la validación administrativa de la reservación después de revisar disponibilidad, datos del cliente o documentación requerida."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservación confirmada correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReservacionDTO.class))),
            @ApiResponse(responseCode = "400", description = "La reservación no puede confirmarse por su estado actual.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))),
            @ApiResponse(responseCode = "401", description = "No se enviaron credenciales válidas mediante HTTP Basic.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "No existe la reservación indicada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class)))
    })
    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservacionDTO> confirmarReservacion(
            @Parameter(description = "Identificador de la reservación que se desea confirmar.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(reservacionService.confirmarReservacion(id));
    }

    @Operation(
            summary = "Cancelar una reservación",
            description = "Cambia el estado de una reservación a CANCELADA. Esta acción se usa cuando el cliente desiste de la estancia o cuando administración decide invalidar la reservación por una regla de negocio."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservación cancelada correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReservacionDTO.class))),
            @ApiResponse(responseCode = "400", description = "La reservación no puede cancelarse por su estado actual.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))),
            @ApiResponse(responseCode = "401", description = "No se enviaron credenciales válidas mediante HTTP Basic.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "No existe la reservación indicada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class)))
    })
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservacionDTO> cancelarReservacion(
            @Parameter(description = "Identificador de la reservación que se desea cancelar.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(reservacionService.cancelarReservacion(id));
    }

    @Operation(
            summary = "Finalizar una reservación",
            description = "Cambia el estado de una reservación a FINALIZADA. Se utiliza cuando la estancia concluyó y administración desea cerrar el ciclo operativo de la reservación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservación finalizada correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReservacionDTO.class))),
            @ApiResponse(responseCode = "400", description = "La reservación no puede finalizarse por su estado actual.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))),
            @ApiResponse(responseCode = "401", description = "No se enviaron credenciales válidas mediante HTTP Basic.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "No existe la reservación indicada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class)))
    })
    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservacionDTO> finalizarReservacion(
            @Parameter(description = "Identificador de la reservación que se desea finalizar.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(reservacionService.finalizarReservacion(id));
    }

    @Operation(
            summary = "Eliminar una reservación",
            description = "Elimina una reservación del sistema. Se recomienda usar esta operación solo para correcciones administrativas; para el flujo normal se debe preferir cancelar o finalizar la reservación para conservar el historial de estados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservación eliminada correctamente. No se regresa contenido."),
            @ApiResponse(responseCode = "401", description = "No se enviaron credenciales válidas mediante HTTP Basic.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "No existe la reservación indicada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReservacion(
            @Parameter(description = "Identificador de la reservación que se desea eliminar.", example = "1", required = true)
            @PathVariable Long id
    ) {
        reservacionService.deleteReservacion(id);
        return ResponseEntity.noContent().build();
    }
}

package org.example.reservaciones.features.cuarto.controller;

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
import org.example.reservaciones.features.cuarto.dto.CreateCuartoDTO;
import org.example.reservaciones.features.cuarto.dto.CuartoDTO;
import org.example.reservaciones.features.cuarto.dto.UpdateCuartoDTO;
import org.example.reservaciones.features.cuarto.dto.UpdateDisponibilidadDTO;
import org.example.reservaciones.features.cuarto.service.CuartoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cuartos")
@RequiredArgsConstructor
@Tag(
        name = "Habitaciones / Cuartos",
        description = "Módulo responsable del catálogo de habitaciones. Permite consultar cuartos disponibles para los clientes y administrar altas, cambios, eliminación y disponibilidad desde el panel de administrador."
)
public class CuartoController {

    private final CuartoService cuartoService;

    @Operation(
            summary = "Registrar una nueva habitación",
            description = "Crea un cuarto dentro del catálogo del hotel. Esta operación está restringida al rol ADMIN porque modifica la oferta de habitaciones disponible para las reservaciones.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Habitación registrada correctamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CuartoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La solicitud contiene campos inválidos, por ejemplo precio menor o igual a cero, número de cuarto no positivo o tipo demasiado corto.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
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
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuartoDTO> createCuarto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos necesarios para registrar una habitación. El número debe ser único y el precio debe ser mayor a cero.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateCuartoDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "tipo": "Habitación doble",
                                      "numero": 204,
                                      "precio": 1450.00,
                                      "numeroCamas": 2
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody CreateCuartoDTO dto
    ) {
        return new ResponseEntity<>(cuartoService.createCuarto(dto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Consultar todas las habitaciones",
            description = "Obtiene el catálogo completo de habitaciones registradas. Este endpoint es público para que la interfaz pueda mostrar la oferta general del hotel sin solicitar autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de habitaciones obtenido correctamente. Puede regresar una lista vacía si aún no existen habitaciones registradas.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CuartoDTO.class)))
            )
    })
    @GetMapping
    public ResponseEntity<List<CuartoDTO>> findAllCuartos() {
        return ResponseEntity.ok(cuartoService.readAllCuartos());
    }

    @Operation(
            summary = "Buscar habitaciones disponibles por fechas",
            description = "Devuelve únicamente las habitaciones habilitadas que no tienen reservaciones traslapadas con el rango de fechas solicitado. Se utiliza durante el proceso público de reservación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Habitaciones disponibles encontradas para el rango solicitado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CuartoDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Las fechas enviadas no tienen formato válido o la fecha de salida no es posterior a la fecha de entrada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            )
    })
    @GetMapping("/disponibles")
    public ResponseEntity<List<CuartoDTO>> findAvailableCuartos(
            @Parameter(description = "Fecha de entrada de la estancia. Debe enviarse en formato ISO yyyy-MM-dd.", example = "2026-07-10", required = true)
            @RequestParam LocalDate fechaEntrada,

            @Parameter(description = "Fecha de salida de la estancia. Debe ser posterior a la fecha de entrada y enviarse en formato ISO yyyy-MM-dd.", example = "2026-07-13", required = true)
            @RequestParam LocalDate fechaSalida
    ) {
        return ResponseEntity.ok(cuartoService.readAvailableCuartos(fechaEntrada, fechaSalida));
    }

    @Operation(
            summary = "Consultar una habitación por identificador",
            description = "Obtiene el detalle de una habitación específica usando su identificador interno. Es útil para mostrar el detalle de un cuarto seleccionado antes de reservar."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Habitación encontrada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CuartoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe una habitación con el identificador proporcionado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CuartoDTO> findCuarto(
            @Parameter(description = "Identificador interno de la habitación.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(cuartoService.readById(id));
    }

    @Operation(
            summary = "Actualizar datos de una habitación",
            description = "Modifica la información principal de un cuarto existente: tipo, número, precio por noche y número de camas. Requiere rol ADMIN porque afecta el catálogo operativo del hotel.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Habitación actualizada correctamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CuartoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos en el cuerpo de la solicitud.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
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
                    description = "No existe la habitación que se intenta actualizar.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuartoDTO> updateCuarto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevos datos de la habitación. Se reemplazan los campos principales del cuarto indicado por el id de la ruta.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateCuartoDTO.class))
            )
            @Valid @RequestBody UpdateCuartoDTO dto,

            @Parameter(description = "Identificador de la habitación que se desea actualizar.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(cuartoService.updateCuarto(id, dto));
    }

    @Operation(
            summary = "Cambiar disponibilidad de una habitación",
            description = "Habilita o deshabilita una habitación sin eliminarla del sistema. Es útil cuando el cuarto está en mantenimiento, limpieza profunda o fuera de servicio temporalmente. Requiere rol ADMIN.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidad actualizada correctamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CuartoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El campo disponible no fue enviado o no tiene un valor booleano válido.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
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
                    description = "No existe la habitación indicada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            )
    })
    @PatchMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuartoDTO> updateCuartoDisponibilidad(
            @Parameter(description = "Identificador de la habitación a habilitar o deshabilitar.", example = "1", required = true)
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo estado de disponibilidad. true significa disponible para reservar; false significa fuera de servicio.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateDisponibilidadDTO.class),
                            examples = @ExampleObject(value = "{ \"disponible\": false }")
                    )
            )
            @Valid @RequestBody UpdateDisponibilidadDTO dto
    ) {
        return ResponseEntity.ok(cuartoService.updateDisponibilidad(id, dto));
    }

    @Operation(
            summary = "Eliminar una habitación",
            description = "Elimina una habitación del catálogo. Debe usarse únicamente cuando el cuarto fue creado por error o ya no forma parte del hotel. Requiere rol ADMIN y puede fallar si existen reservaciones asociadas.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Habitación eliminada correctamente. No se regresa contenido."),
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
                    description = "No existe la habitación indicada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCuarto(
            @Parameter(description = "Identificador de la habitación que se desea eliminar.", example = "1", required = true)
            @PathVariable Long id
    ) {
        cuartoService.deleteCuarto(id);
        return ResponseEntity.noContent().build();
    }
}

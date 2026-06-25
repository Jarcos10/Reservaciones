package org.example.reservaciones.core.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reservaciones.core.domain.Usuario;
import org.example.reservaciones.core.exceptions.CustomErrorRecord;
import org.example.reservaciones.core.security.dto.RegisterRequestDTO;
import org.example.reservaciones.core.security.dto.UsuarioDTO;
import org.example.reservaciones.core.security.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Autenticación y seguridad",
        description = "Módulo de seguridad basado en Spring Security con HTTP Basic. No utiliza JWT: cada petición protegida debe enviar el encabezado Authorization con las credenciales codificadas en Base64. El sistema maneja los roles ADMIN y USUARIO para limitar las operaciones permitidas."
)
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Validar credenciales de usuario",
            description = "Endpoint de prueba para comprobar el inicio de sesión con HTTP Basic. No genera token ni crea sesión persistente; si las credenciales son correctas, devuelve la información del usuario autenticado y su rol.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Credenciales válidas. Se devuelve el usuario autenticado con su rol asignado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UsuarioDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales ausentes o incorrectas. El correo y la contraseña deben enviarse mediante HTTP Basic.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario existe, pero está inactivo o no tiene autorización para acceder.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @GetMapping("/login")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDTO> login(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(authService.mapearUsuario(usuario));
    }

    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Crea una cuenta de usuario para el sistema de reservaciones. Por seguridad, el registro público asigna el rol USUARIO de forma automática; el rol ADMIN debe ser creado por el inicializador del sistema o por administración interna.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado correctamente con rol USUARIO y contraseña cifrada con BCrypt.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UsuarioDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos, por ejemplo correo con formato incorrecto, contraseña demasiado corta o nombre incompleto.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El correo ya se encuentra registrado en el sistema.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomErrorRecord.class))
            )
    })
    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registrarUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de alta del usuario. La contraseña se almacena cifrada y nunca se devuelve en las respuestas.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequestDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "nombre": "Carlos Ramírez",
                                      "correo": "carlos.ramirez@example.com",
                                      "password": "Usuario12345"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody RegisterRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarUsuario(dto));
    }

    @Operation(
            summary = "Consultar usuario autenticado",
            description = "Devuelve la información del usuario que envió las credenciales HTTP Basic. Es útil para que el frontend conozca el rol actual y decida qué vistas mostrar: administración para ADMIN o flujo de reservación para USUARIO.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Información del usuario autenticado obtenida correctamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UsuarioDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se enviaron credenciales válidas mediante HTTP Basic.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDTO> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(authService.mapearUsuario(usuario));
    }
}

package org.example.reservaciones.core.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.reservaciones.core.domain.RolUsuario;

@Schema(
        name = "UsuarioDTO",
        description = "Información segura del usuario autenticado. No incluye la contraseña y permite al frontend conocer el rol para mostrar u ocultar vistas."
)
public record UsuarioDTO(
        @Schema(description = "Identificador interno del usuario.", example = "1")
        Long idUsuario,

        @Schema(description = "Nombre completo del usuario.", example = "Administrador del sistema")
        String nombre,

        @Schema(description = "Correo utilizado como nombre de usuario para autenticación HTTP Basic.", example = "admin@reservahotel.com")
        String correo,

        @Schema(description = "Rol asignado al usuario. ADMIN puede administrar el sistema; USUARIO puede gestionar el flujo de reservación.", example = "ADMIN", implementation = RolUsuario.class)
        RolUsuario rol,

        @Schema(description = "Indica si la cuenta está habilitada para autenticarse.", example = "true")
        boolean activo
) {
}

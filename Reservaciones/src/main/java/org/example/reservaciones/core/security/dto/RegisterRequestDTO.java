package org.example.reservaciones.core.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "RegisterRequestDTO",
        description = "Datos necesarios para registrar una cuenta de usuario. El sistema asigna rol USUARIO por defecto y cifra la contraseña antes de guardarla."
)
public record RegisterRequestDTO(
        @Schema(description = "Nombre completo del usuario.", example = "Carlos Ramírez", minLength = 3, maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        String nombre,

        @Schema(description = "Correo electrónico que funcionará como nombre de usuario para HTTP Basic.", example = "carlos.ramirez@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo,

        @Schema(description = "Contraseña del usuario. Se almacena cifrada con BCrypt y no se devuelve en ninguna respuesta.", example = "Usuario12345", minLength = 6, maxLength = 60, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 60, message = "La contraseña debe tener entre 6 y 60 caracteres")
        String password
) {
}

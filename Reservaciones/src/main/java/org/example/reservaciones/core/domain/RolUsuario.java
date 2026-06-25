package org.example.reservaciones.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RolUsuario",
        description = "Rol de autorización asignado a una cuenta del sistema. ADMIN administra habitaciones, reservaciones y archivos; USUARIO puede realizar operaciones del flujo de reservación."
)
public enum RolUsuario {
    ADMIN,
    USUARIO
}

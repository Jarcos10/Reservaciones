package org.example.reservaciones.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo")
        }
)
@Schema(
        name = "Usuario",
        description = "Entidad de seguridad usada por Spring Security para autenticar usuarios mediante HTTP Basic y autorizar acciones con roles ADMIN o USUARIO."
)
public class Usuario implements UserDetails, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    @Schema(description = "Identificador interno del usuario.", example = "1")
    private Long idUsuario;

    @Column(name = "nombre", nullable = false, length = 120)
    @Schema(description = "Nombre completo del usuario.", example = "Administrador del sistema")
    private String nombre;

    @Column(name = "correo", nullable = false, length = 150)
    @Schema(description = "Correo utilizado como nombre de usuario para HTTP Basic.", example = "admin@reservahotel.com")
    private String correo;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    @Schema(description = "Contraseña cifrada con BCrypt. No se expone en respuestas.", hidden = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 30)
    @Schema(description = "Rol de autorización del usuario.", example = "ADMIN", implementation = RolUsuario.class)
    private RolUsuario rol;

    @Column(name = "activo", nullable = false)
    @Schema(description = "Indica si la cuenta puede autenticarse.", example = "true")
    private boolean activo;

    @Column(name = "fecha_creacion", nullable = false)
    @Schema(description = "Fecha y hora en que se creó el usuario.", example = "2026-06-24T17:35:20")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();

        if (this.rol == null) {
            this.rol = RolUsuario.USUARIO;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}

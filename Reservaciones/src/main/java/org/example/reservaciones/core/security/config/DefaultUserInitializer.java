package org.example.reservaciones.core.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservaciones.core.domain.RolUsuario;
import org.example.reservaciones.core.domain.Usuario;
import org.example.reservaciones.core.security.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultUserInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.default-admin.nombre:Administrador}")
    private String adminNombre;

    @Value("${app.security.default-admin.correo:admin@reservahotel.com}")
    private String adminCorreo;

    @Value("${app.security.default-admin.password:Admin12345}")
    private String adminPassword;

    @Value("${app.security.default-user.nombre:Usuario}")
    private String usuarioNombre;

    @Value("${app.security.default-user.correo:usuario@reservahotel.com}")
    private String usuarioCorreo;

    @Value("${app.security.default-user.password:Usuario12345}")
    private String usuarioPassword;

    @Override
    public void run(String... args) {
        crearUsuarioSiNoExiste(adminNombre, adminCorreo, adminPassword, RolUsuario.ADMIN);
        crearUsuarioSiNoExiste(usuarioNombre, usuarioCorreo, usuarioPassword, RolUsuario.USUARIO);
    }

    private void crearUsuarioSiNoExiste(String nombre, String correo, String password, RolUsuario rol) {
        if (usuarioRepository.existsByCorreo(correo)) {
            return;
        }

        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .correo(correo)
                .password(passwordEncoder.encode(password))
                .rol(rol)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuario inicial {} creado con correo: {}", rol, correo);
    }
}

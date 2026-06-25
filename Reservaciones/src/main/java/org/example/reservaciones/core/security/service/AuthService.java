package org.example.reservaciones.core.security.service;

import lombok.RequiredArgsConstructor;
import org.example.reservaciones.core.domain.RolUsuario;
import org.example.reservaciones.core.domain.Usuario;
import org.example.reservaciones.core.exceptions.BussinesValidationException;
import org.example.reservaciones.core.security.dto.RegisterRequestDTO;
import org.example.reservaciones.core.security.dto.UsuarioDTO;
import org.example.reservaciones.core.security.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioDTO registrarUsuario(RegisterRequestDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.correo())) {
            throw new BussinesValidationException("Ya existe un usuario registrado con ese correo");
        }

        Usuario usuario = Usuario.builder()
                .nombre(dto.nombre())
                .correo(dto.correo())
                .password(passwordEncoder.encode(dto.password()))
                .rol(RolUsuario.USUARIO)
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        return mapearUsuario(usuario);
    }

    public UsuarioDTO mapearUsuario(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo()
        );
    }
}

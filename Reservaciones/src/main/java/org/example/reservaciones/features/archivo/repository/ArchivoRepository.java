package org.example.reservaciones.features.archivo.repository;

import org.example.reservaciones.core.domain.Archivo;
import org.example.reservaciones.core.domain.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArchivoRepository extends JpaRepository<Archivo, Long> {

    Optional<Archivo> findByReservacionIdReservacionAndTipoDocumento(
            Long idReservacion,
            TipoDocumento tipoDocumento
    );

    boolean existsByReservacionIdReservacionAndTipoDocumento(
            Long idReservacion,
            TipoDocumento tipoDocumento
    );
}

package org.example.reservaciones.features.reservacion.repository;

import org.example.reservaciones.core.domain.EstadoReservacion;
import org.example.reservaciones.core.domain.Reservacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservacionRepository extends JpaRepository<Reservacion, Long> {

    List<Reservacion> findByCuartoId(Long idCuarto);

    @Query("""
            SELECT COUNT(r) > 0
            FROM Reservacion r
            WHERE r.cuarto.id = :idCuarto
              AND r.estado IN :estados
              AND r.fechaEntrada < :fechaSalida
              AND r.fechaSalida > :fechaEntrada
            """)
    boolean existeTraslapeDeFechas(
            Long idCuarto,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            Collection<EstadoReservacion> estados
    );
}

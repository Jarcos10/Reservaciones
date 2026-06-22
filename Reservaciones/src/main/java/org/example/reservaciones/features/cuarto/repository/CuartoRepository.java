package org.example.reservaciones.features.cuarto.repository;

import org.example.reservaciones.core.domain.Cuarto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CuartoRepository extends JpaRepository<Cuarto, Long> {
    Optional<Cuarto> findByNumero(int numero);

    @Query("""
            SELECT c
            FROM Cuarto c
            WHERE c.disponible = true
              AND c.id NOT IN (
                    SELECT r.cuarto.id
                    FROM Reservacion r
                    WHERE r.estado IN (org.example.reservaciones.core.domain.EstadoReservacion.PENDIENTE,
                                       org.example.reservaciones.core.domain.EstadoReservacion.CONFIRMADA)
                      AND r.fechaEntrada < :fechaSalida
                      AND r.fechaSalida > :fechaEntrada
              )
            ORDER BY c.numero ASC
            """)
    List<Cuarto> findCuartosDisponibles(LocalDate fechaEntrada, LocalDate fechaSalida);
}

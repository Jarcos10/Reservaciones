package org.example.reservaciones.features.cuarto.dto;

import java.math.BigDecimal;

/**
 *  DTO que va a representar la información que se muestra al recuperar una habitación
 * @param id
 * @param tipo
 * @param numero
 * @param precio
 * @param numeroCamas
 * @param disponible
 */

public record CuartoDTO(
        Long id,
        String tipo,
        int numero,
        BigDecimal precio,
        int numeroCamas,
        boolean disponible
) {
}

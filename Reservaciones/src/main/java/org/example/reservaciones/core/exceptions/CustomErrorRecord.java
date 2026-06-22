package org.example.reservaciones.core.exceptions;

import java.time.LocalDateTime;

public record CustomErrorRecord(
        LocalDateTime dateTime,
        String messaege,
        String details
) {
}

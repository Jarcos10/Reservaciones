package org.example.reservaciones.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BussinesValidationException extends RuntimeException {
    public BussinesValidationException(String message) {
        super(message);
    }
}

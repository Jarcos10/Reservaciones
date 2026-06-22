package org.example.reservaciones.core.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<CustomErrorRecord> handleResourceNotFound(EntityNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(
                new CustomErrorRecord(LocalDateTime.now(), ex.getMessage(), request.getDescription(false)),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(value = {BussinesValidationException.class})
    public ResponseEntity<CustomErrorRecord> handleBussinesValidationException(BussinesValidationException ex, WebRequest request) {
        return new ResponseEntity<>(
                new CustomErrorRecord(LocalDateTime.now(), ex.getMessage(), request.getDescription(false)),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<CustomErrorRecord> handleGeneralException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(
                new CustomErrorRecord(LocalDateTime.now(), ex.getMessage(), request.getDescription(false)),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String detalle = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .collect(Collectors.joining(" , "));

        CustomErrorRecord errorRecord = new CustomErrorRecord(
                LocalDateTime.now(),
                "Hubo errores en la validación de uno o más campos",
                detalle
        );

        return new ResponseEntity<>(errorRecord, headers, HttpStatus.BAD_REQUEST);
    }
}

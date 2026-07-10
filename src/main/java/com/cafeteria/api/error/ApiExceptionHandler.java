package com.cafeteria.api.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/**
 * Manejo global de errores. Da un cuerpo JSON consistente para:
 *  - {@link ResponseStatusException}: los mensajes de negocio (en español)
 *    que lanzan los servicios llegan al cliente.
 *  - errores de validación de Bean Validation, con el detalle por campo.
 *
 * Nunca se expone el stack trace ni detalles internos. El resto de
 * excepciones las maneja Spring Boot por defecto (con
 * server.error.include-stacktrace=never, tampoco filtran la traza).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Cuerpo de error uniforme para el frontend. */
    public record ErrorResponse(
            String timestamp,
            int status,
            String error,
            String message,
            String path,
            List<CampoError> errors) {

        public record CampoError(String campo, String mensaje) {
        }
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> manejarResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String mensaje = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return construir(status, mensaje, request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.CampoError> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.CampoError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos de entrada no válidos", request, errores);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje,
                                                    HttpServletRequest request,
                                                    List<ErrorResponse.CampoError> errores) {
        ErrorResponse cuerpo = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI(),
                errores);
        return ResponseEntity.status(status).body(cuerpo);
    }
}

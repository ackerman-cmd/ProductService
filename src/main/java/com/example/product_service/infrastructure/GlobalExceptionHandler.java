package com.example.product_service.infrastructure;

import com.example.product_service.infrastructure.exception.DataAccessException;
import com.example.product_service.infrastructure.exception.DataValidationException;
import jakarta.validation.ConstraintViolationException;
import org.apache.iceberg.exceptions.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation error"
                ));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed for request",
                errors,
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage()
                ));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Constraint violation",
                errors,
                request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(
            NotFoundException ex,
            WebRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                null,
                request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccessException(
            DataAccessException ex,
            WebRequest request) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Data access error occurred",
                Map.of("error", ex.getMessage()),
                request);
    }

    @ExceptionHandler(DataValidationException.class)
    public ResponseEntity<Object> handleDataValidationException(
            DataValidationException ex,
            WebRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Data validation failed",
                Map.of("error", ex.getMessage()),
                request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid argument provided",
                Map.of("error", ex.getMessage()),
                request);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(
            IOException ex,
            WebRequest request) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "I/O operation failed",
                Map.of("error", ex.getMessage()),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(
            Exception ex,
            WebRequest request) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                Map.of("error", ex.getMessage()),
                request);
    }

    private ResponseEntity<Object> buildErrorResponse(
            HttpStatus status,
            String message,
            Map<String, String> details,
            WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        if (details != null && !details.isEmpty()) {
            body.put("details", details);
        }

        return new ResponseEntity<>(body, status);
    }
}
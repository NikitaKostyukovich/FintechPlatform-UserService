package com.innowise.userservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, CardNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUserNotFound(RuntimeException e) {
        log.warn("User not found: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CardLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleCardLimitExceeded(CardLimitExceededException e) {
        log.warn("Card limit exceeded: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data integrity violation (possible duplicate): {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), "Data conflict: resource with such unique fields already exists");
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception e) {
        log.error("Unexpected internal server error occurred", e);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error. Please try again later.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
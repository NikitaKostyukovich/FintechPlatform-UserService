package com.innowise.userservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleExceptionsDirectly() {
        ResponseEntity<ErrorResponse> r1 = handler.handleCardLimitExceeded(new CardLimitExceededException("Limit reached"));
        assertEquals(HttpStatus.CONFLICT, r1.getStatusCode());
        assertNotNull(r1.getBody());

        ResponseEntity<ErrorResponse> r2 = handler.handleDataIntegrityViolation(new DataIntegrityViolationException("Duplicate"));
        assertEquals(HttpStatus.CONFLICT, r2.getStatusCode());
        assertNotNull(r2.getBody());

        ResponseEntity<ErrorResponse> r3 = handler.handleAllOtherExceptions(new RuntimeException("Unexpected error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r3.getStatusCode());
        assertNotNull(r3.getBody());
    }
}
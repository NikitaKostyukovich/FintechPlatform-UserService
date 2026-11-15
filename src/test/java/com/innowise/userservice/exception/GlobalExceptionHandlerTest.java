package com.innowise.userservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test")
    static class FakeController {
        @PostMapping("/json")
        public void testJson(@RequestBody DummyDto dto) {}

        @GetMapping("/type/{id}")
        public void testTypeMismatch(@PathVariable Long id) {}

        static class DummyDto {
            public String name;
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(handler)
                .build();
    }

    @Test
    void shouldHandleExceptionsDirectly() {
        ResponseEntity<ErrorResponse> r1 = handler.handleCardLimitExceeded(new CardLimitExceededException("Limit reached"));
        assertEquals(HttpStatus.BAD_REQUEST, r1.getStatusCode());
        assertNotNull(r1.getBody());

        ResponseEntity<ErrorResponse> r2 = handler.handleDataIntegrityViolation(new DataIntegrityViolationException("Duplicate"));
        assertEquals(HttpStatus.BAD_REQUEST, r2.getStatusCode());
        assertNotNull(r2.getBody());

        ResponseEntity<ErrorResponse> r3 = handler.handleAllOtherExceptions(new RuntimeException("Unexpected error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r3.getStatusCode());
        assertNotNull(r3.getBody());
    }

    @Test
    void shouldReturn400_whenJsonIsMalformed() throws Exception {
        String malformedJson = "{ \"name\": \"John\", \"surname\": ";

        mockMvc.perform(post("/test/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/test/type/not-a-number"))
                .andExpect(status().isBadRequest());
    }
}
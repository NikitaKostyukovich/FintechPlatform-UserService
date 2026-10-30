package com.innowise.userservice.dto.card;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CardRequestDTO(
        @NotBlank @Pattern(regexp = "\\d{16}") String number,
        @NotBlank @Size(max = 100) String holder,
        @NotNull @Future LocalDate expirationDate
) {}


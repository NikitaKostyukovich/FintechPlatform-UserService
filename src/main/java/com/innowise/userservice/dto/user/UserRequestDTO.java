package com.innowise.userservice.dto.user;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRequestDTO(
        @NotBlank @Size(max = 25) String name,
        @NotBlank @Size(max = 50) String surname,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Email @Size(max = 255) String email
        ) {

}

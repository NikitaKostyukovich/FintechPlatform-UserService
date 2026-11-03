package com.innowise.userservice.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserRequestDTO(
        @NotBlank @Size(max = 25) String name,
        @NotBlank @Size(max = 50) String surname,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Email @Size(max = 255) String email
        ) {

}

package com.innowise.userservice.dto.user;

import com.innowise.userservice.dto.card.CardResponseDTO;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record UserResponseDTO(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        boolean active,
        List<CardResponseDTO> cards
) implements Serializable {
}
